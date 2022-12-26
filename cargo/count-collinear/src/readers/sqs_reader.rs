use crate::ec2::EC2Controller;
use crate::email::EmailController;
use crate::readers::{CollinearReader, CollinearReaderError, CountCollinearArgs};
use crate::settings::Config;
use crate::sqs::SqsController;
use log::{error, info};
use std::fmt;
use std::fmt::Formatter;
use std::str::FromStr;
use std::thread::sleep;
use std::time::{Duration, Instant};

pub const QUEUE_NAME: &str = "collinearity";
const NO_JOBS_POLLS_MAX: i32 = 5;
const POLL_INTERVAL_SECONDS: u64 = 60;
const SHUTDOWN_ATTEMPTS_MAX: i32 = 5;
const SHUTDOWN_INTERVAL_SECONDS: u64 = 60;

pub struct SqsReader {
    sqs_controller: SqsController,
    queue_name: String,
    consecutive_no_jobs_polls: i32,
    no_jobs_polls_max: i32,
    last_poll_time: Option<Instant>,
    processed_message_receipt_handle: Option<String>,
    email_controller: Option<EmailController>,
    ec2_controller: EC2Controller,
    shutdown_on_polling_end: bool,
}
impl SqsReader {
    fn build_sqs_controller(config: &Config) -> Result<SqsController, CollinearReaderError> {
        let aws_auth_settings = config
            .aws_auth_settings
            .as_ref()
            .ok_or(CollinearReaderError {
                msg: String::from("AWS Auth settings are none. Cannot initialise SQS Reader."),
            })?;
        Ok(SqsController::new(
            aws_auth_settings.access_key.as_str(),
            aws_auth_settings.secret_key.as_str(),
            aws_auth_settings.account_number.as_str(),
            aws_auth_settings.region.as_str(),
        ))
    }

    fn build_email_controller(
        config: &Config,
    ) -> Result<Option<EmailController>, CollinearReaderError> {
        let email_settings = config.email_settings.as_ref().ok_or(CollinearReaderError {
            msg: String::from("Missing email configuration settings"),
        })?;
        let smtp_url = email_settings.smtp_url.as_str();
        let smtp_username = email_settings.smtp_username.as_str();
        let smtp_password = email_settings.smtp_password.as_str();
        let from_email_default = Some(email_settings.from_email.clone());
        let to_email_default = Some(email_settings.to_email.clone());
        Ok(EmailController::new(
            smtp_url,
            smtp_username,
            smtp_password,
            from_email_default,
            to_email_default,
        )
        .map_err(|err| CollinearReaderError {
            msg: err.to_string(),
        })
        .ok())
    }

    fn build_and_check_queue_name(
        sqs_controller: &SqsController,
    ) -> Result<String, CollinearReaderError> {
        let queue_name = QUEUE_NAME.to_string();
        let expected_queue_url = format!(
            "https://sqs.{}.amazonaws.com/{}/{}",
            sqs_controller.region(),
            sqs_controller.account_number(),
            queue_name.as_str()
        );
        let existing_queues = sqs_controller
            .list_queues()
            .map_err(|err| CollinearReaderError {
                msg: format!("{}", err),
            })?;
        if !existing_queues.contains(&expected_queue_url) {
            return Err(CollinearReaderError {
                msg: format!(
                    "No queue named '{}' configured in AWS.",
                    queue_name.as_str()
                ),
            });
        }
        Ok(queue_name)
    }

    fn build_ec2_controller(config: &Config) -> Result<EC2Controller, CollinearReaderError> {
        let aws_auth_settings = config
            .aws_auth_settings
            .as_ref()
            .ok_or(CollinearReaderError {
                msg: String::from("AWS Auth settings are none. Cannot initialise SQS Reader."),
            })?;
        Ok(EC2Controller::new(
            aws_auth_settings.access_key.as_str(),
            aws_auth_settings.secret_key.as_str(),
            aws_auth_settings.region.as_str(),
        ))
    }

    pub fn new(config: &Config) -> Result<SqsReader, CollinearReaderError> {
        let sqs_controller = SqsReader::build_sqs_controller(config)?;
        let email_controller = SqsReader::build_email_controller(config)?;
        let queue_name = SqsReader::build_and_check_queue_name(&sqs_controller)?;
        let consecutive_no_jobs_polls = 0;
        let no_jobs_polls_max = NO_JOBS_POLLS_MAX;
        let last_poll_time = None;
        let processed_message_receipt_handle = None;
        let ec2_controller = SqsReader::build_ec2_controller(config)?;
        let shutdown_on_polling_end = config
            .sqs_settings
            .as_ref()
            .ok_or(CollinearReaderError {
                msg: "SQS settings unexpectedly missing.".to_string(),
            })?
            .shutdown_on_polling_end;

        Ok(SqsReader {
            sqs_controller,
            queue_name,
            consecutive_no_jobs_polls,
            no_jobs_polls_max,
            last_poll_time,
            processed_message_receipt_handle,
            email_controller,
            ec2_controller,
            shutdown_on_polling_end,
        })
    }
}

impl CollinearReader for SqsReader {
    fn read_count_collinear_args(
        &mut self,
    ) -> std::result::Result<Option<CountCollinearArgs>, CollinearReaderError> {
        match self.last_poll_time {
            Some(last_poll_time) => {
                let seconds_since_poll = last_poll_time.elapsed().as_secs();
                if seconds_since_poll < POLL_INTERVAL_SECONDS {
                    sleep(Duration::from_secs(
                        POLL_INTERVAL_SECONDS - seconds_since_poll,
                    ));
                }
            }
            None => (),
        }
        self.last_poll_time = Some(Instant::now());
        let message = self
            .sqs_controller
            .receive_message(self.queue_name.as_str(), None)
            .map_err(|err| CollinearReaderError {
                msg: err.to_string(),
            })?;
        return match message {
            Some(message) => {
                let body = message.body;
                let count_collinear_args =
                    CountCollinearArgs::from_str(body.as_str()).map_err(|err| {
                        CollinearReaderError {
                            msg: err.to_string(),
                        }
                    })?;
                self.processed_message_receipt_handle = Some(message.receipt_handle);
                Ok(Some(count_collinear_args))
            }
            None => {
                self.consecutive_no_jobs_polls += 1;
                info!(
                    "No jobs to process. Consecutive no jobs polls count is at {}/{}",
                    self.consecutive_no_jobs_polls, self.no_jobs_polls_max,
                );
                Ok(None)
            }
        };
    }

    fn post_process_args_read(&self) -> Result<(), CollinearReaderError> {
        match &self.processed_message_receipt_handle {
            Some(receipt_handle) => self
                .sqs_controller
                .delete_message(self.queue_name.as_str(), receipt_handle.as_str())
                .map_err(|err| CollinearReaderError {
                    msg: err.to_string(),
                }),
            None => Ok(()),
        }
    }

    fn is_finished_reading(&self) -> bool {
        self.consecutive_no_jobs_polls >= self.no_jobs_polls_max
    }

    fn stop_reading(&self) {
        info!("Maximum number of queue polls with no jobs available reached. Stopping reading.");
        if self.shutdown_on_polling_end {
            let mut terminate_result = self.ec2_controller.terminate();
            let mut shutdown_attempts = 1;
            while terminate_result.is_err() && shutdown_attempts < SHUTDOWN_ATTEMPTS_MAX {
                info!(
                    "Shutdown attempt {}/{} failed. Retrying in {} seconds...",
                    shutdown_attempts, SHUTDOWN_ATTEMPTS_MAX, SHUTDOWN_INTERVAL_SECONDS
                );
                sleep(Duration::from_secs(SHUTDOWN_INTERVAL_SECONDS));
                terminate_result = self.ec2_controller.terminate();
                shutdown_attempts += 1;
            }
            if terminate_result.is_ok() {
                info!("Shutting down");
            } else {
                error!("Shutdown failed after exhausting retry attempts.");
                match self.email_controller.as_ref() {
                    Some(email_controller) => {
                        let send_email_result = email_controller
                            .send_email_with_default_from_to("Instance failed to shut down", "");
                        if send_email_result.is_err() {
                            error!("Email to notify of 'failed shutdown' failed.");
                        }
                    }
                    None => (),
                }
            }
        }
    }
}

impl fmt::Display for SqsReader {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        write!(f, "SQS Reader")
    }
}
