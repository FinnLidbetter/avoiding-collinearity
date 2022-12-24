use lettre::address::AddressError;
use lettre::message::Mailbox;
use lettre::transport::smtp::authentication::Credentials;
use lettre::{Message, SmtpTransport, Transport};
use std::fmt;
use std::fmt::Formatter;

type Result<T> = std::result::Result<T, EmailError>;

#[derive(Debug, Clone)]
pub struct EmailError {
    msg: String,
}

impl fmt::Display for EmailError {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        write!(f, "Email Error: {}", self.msg)
    }
}

pub struct EmailController {
    mailer: SmtpTransport,
    from_email_default: Option<String>,
    to_email_default: Option<String>,
}

impl EmailController {
    pub fn new(
        smtp_url: &str,
        smtp_username: &str,
        smtp_password: &str,
        from_email_default: Option<String>,
        to_email_default: Option<String>,
    ) -> Result<EmailController> {
        let mailer = SmtpTransport::starttls_relay(smtp_url)
            .map_err(|err| EmailError {
                msg: err.to_string(),
            })?
            // Add credentials for authentication
            .credentials(Credentials::new(
                smtp_username.to_string(),
                smtp_password.to_string(),
            ))
            .build();
        Ok(EmailController {
            mailer,
            from_email_default,
            to_email_default,
        })
    }

    pub fn send_email_with_default_from_to(&self, subject: &str, body: &str) -> Result<()> {
        let from_email = self.from_email_default.as_ref().ok_or(EmailError {
            msg: "No configured default sender.".to_string(),
        })?;
        let to_email = self.to_email_default.as_ref().ok_or(EmailError {
            msg: "No configured default receiver.".to_string(),
        })?;
        self.send_email(from_email.as_str(), to_email.as_str(), subject, body)
    }

    pub fn send_email(
        &self,
        from_email: &str,
        to_email: &str,
        subject: &str,
        body: &str,
    ) -> Result<()> {
        let sender_mailbox: Mailbox =
            format!("<{}>", from_email)
                .parse()
                .map_err(|err: AddressError| EmailError {
                    msg: err.to_string(),
                })?;
        let receiver_mailbox: Mailbox =
            format!("<{}>", to_email)
                .parse()
                .map_err(|err: AddressError| EmailError {
                    msg: err.to_string(),
                })?;
        let email = Message::builder()
            .from(sender_mailbox)
            .to(receiver_mailbox)
            .subject(subject)
            .body(body.to_string())
            .map_err(|err| EmailError {
                msg: format!("Failed to construct email: {}", err),
            })?;
        self.mailer.send(&email).map_err(|err| EmailError {
            msg: format!("Failed to send email: {}", err),
        })?;
        Ok(())
    }
}
