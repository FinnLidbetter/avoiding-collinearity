use crate::settings::Config;
use crate::writers::{CollinearWriter, CollinearWriterError, CountCollinearResult};
use lettre::address::AddressError;
use lettre::message::Mailbox;
use lettre::transport::smtp::authentication::Credentials;
use lettre::{Message, SmtpTransport, Transport};
use log::error;
use std::fmt::Formatter;
use std::{error, fmt};

pub struct EmailController {
    mailer: SmtpTransport,
    sender_mailbox: Mailbox,
    receiver_mailbox: Mailbox,
}

impl EmailController {
    pub fn new(config: &Config) -> Result<EmailController, CollinearWriterError> {
        let email_settings = config.email_settings.as_ref().ok_or(CollinearWriterError {
            msg: String::from("Missing email configuration settings"),
        })?;
        let mailer = SmtpTransport::starttls_relay(email_settings.smtp_url.as_str())
            .map_err(|err| CollinearWriterError {
                msg: err.to_string(),
            })?
            // Add credentials for authentication
            .credentials(Credentials::new(
                email_settings.smtp_username.clone(),
                email_settings.smtp_password.clone(),
            ))
            .build();
        let sender_mailbox: Mailbox =
            format!("<{}>", email_settings.from_email)
                .parse()
                .map_err(|err: AddressError| CollinearWriterError {
                    msg: err.to_string(),
                })?;
        let receiver_mailbox: Mailbox =
            format!("<{}>", email_settings.to_email)
                .parse()
                .map_err(|err: AddressError| CollinearWriterError {
                    msg: err.to_string(),
                })?;
        Ok(EmailController {
            mailer,
            sender_mailbox,
            receiver_mailbox,
        })
    }
}

impl fmt::Display for EmailController {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        write!(f, "Email Writer")
    }
}

impl CollinearWriter for EmailController {
    fn write_count_collinear_result(
        &self,
        count_collinear_result: CountCollinearResult,
    ) -> Result<(), CollinearWriterError> {
        let subject = "Count Collinear Results";
        let body = count_collinear_result.verbose_string();
        let email = Message::builder()
            .from(self.sender_mailbox.clone())
            .to(self.receiver_mailbox.clone())
            .subject(subject)
            .body(body)
            .map_err(|err| CollinearWriterError {
                msg: format!("Failed to construct email: {}", err),
            })?;
        self.mailer
            .send(&email)
            .map_err(|err| CollinearWriterError {
                msg: format!("Failed to send email: {}", err),
            })?;
        Ok(())
    }
}
