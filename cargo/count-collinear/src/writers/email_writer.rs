use crate::email::EmailController;
use crate::settings::Config;
use crate::writers::{CollinearWriter, CollinearWriterError, CountCollinearResult};
use std::fmt;
use std::fmt::Formatter;

pub struct EmailWriter {
    email_controller: EmailController,
}

impl EmailWriter {
    pub fn new(config: &Config) -> Result<EmailWriter, CollinearWriterError> {
        let email_settings = config.email_settings.as_ref().ok_or(CollinearWriterError {
            msg: String::from("Missing email configuration settings"),
        })?;
        let smtp_url = email_settings.smtp_url.as_str();
        let smtp_username = email_settings.smtp_username.as_str();
        let smtp_password = email_settings.smtp_password.as_str();
        let from_email_default = Some(email_settings.from_email.clone());
        let to_email_default = Some(email_settings.to_email.clone());
        let email_controller = EmailController::new(
            smtp_url,
            smtp_username,
            smtp_password,
            from_email_default,
            to_email_default,
        )
        .map_err(|err| CollinearWriterError {
            msg: err.to_string(),
        })?;

        Ok(EmailWriter { email_controller })
    }
}

impl fmt::Display for EmailWriter {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        write!(f, "Email Writer")
    }
}

impl CollinearWriter for EmailWriter {
    fn write_count_collinear_result(
        &mut self,
        count_collinear_result: CountCollinearResult,
    ) -> Result<(), CollinearWriterError> {
        let subject = "Count Collinear Results";
        let body = count_collinear_result.verbose_string();
        self.email_controller
            .send_email_with_default_from_to(subject, body.as_str())
            .map_err(|err| CollinearWriterError {
                msg: format!(
                    "Failed to write count collinear result {} due to {}",
                    count_collinear_result, err
                ),
            })?;
        Ok(())
    }
}
