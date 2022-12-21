use crate::sqs::SqsController;
use crate::{CollinearReader, CollinearReaderError, Config, CountCollinearArgs};
use std::fmt;
use std::fmt::Formatter;
use std::str::FromStr;

const QUEUE_NAME: &str = "collinearity";
const NO_JOBS_POLLS_MAX: i32 = 5;

pub struct SqsReader {
    sqs_controller: SqsController,
    queue_name: String,
    consecutive_no_jobs_polls: i32,
    no_jobs_polls_max: i32,
}
impl SqsReader {
    pub fn new(config: &Config) -> Result<SqsReader, CollinearReaderError> {
        let aws_auth_settings = config
            .aws_auth_settings
            .as_ref()
            .ok_or(CollinearReaderError {
                msg: String::from("AWS Auth settings are none. Cannot initialise SQS Reader."),
            })?;
        let sqs_controller = SqsController::new(
            aws_auth_settings.access_key.as_str(),
            aws_auth_settings.secret_key.as_str(),
            aws_auth_settings.account_number.as_str(),
            aws_auth_settings.region.as_str(),
        );
        let consecutive_no_jobs_polls = 0;
        let no_jobs_polls_max = NO_JOBS_POLLS_MAX;
        let queue_name = QUEUE_NAME.to_string();
        Ok(SqsReader {
            sqs_controller,
            queue_name,
            consecutive_no_jobs_polls,
            no_jobs_polls_max,
        })
    }
}

impl CollinearReader for SqsReader {
    fn read_count_collinear_args(
        &mut self,
    ) -> std::result::Result<Option<CountCollinearArgs>, CollinearReaderError> {
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
                Ok(Some(count_collinear_args))
            }
            None => {
                self.consecutive_no_jobs_polls += 1;
                Ok(None)
            }
        };
    }

    fn is_finished_reading(&self) -> bool {
        self.consecutive_no_jobs_polls >= self.no_jobs_polls_max
    }

    fn stop_reading(&self) -> () {
        ()
    }
}

impl fmt::Display for SqsReader {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        write!(f, "SQS Reader")
    }
}
