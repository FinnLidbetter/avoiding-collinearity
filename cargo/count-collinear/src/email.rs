use crate::settings::Config;
use lettre::{
    message::Mailbox, transport::smtp::authentication::Credentials, Message, SmtpTransport,
    Transport,
};
use log::error;
use std::error;

pub fn send_result(
    sequence_length: u32,
    start_index: usize,
    end_index: usize,
    max_count: i32,
    config: &Config,
) -> Result<(), Box<dyn error::Error>> {
    let email_settings = config
        .email_settings
        .as_ref()
        .ok_or("Email settings are not configured.")?;
    let mailer = SmtpTransport::starttls_relay(email_settings.smtp_url.as_str())?
        // Add credentials for authentication
        .credentials(Credentials::new(
            email_settings.smtp_username.clone(),
            email_settings.smtp_password.clone(),
        ))
        .build();
    let sender_mailbox: Mailbox = format!("<{}>", email_settings.from_email).parse()?;
    let receiver_mailbox: Mailbox = format!("<{}>", email_settings.to_email).parse()?;
    let subject = "Count Collinear Results";
    let body = format!(
        "Considering all lines with at least one point with an index in [{}, {}], \
        the largest number of collinear points in the first {} indices of \
        the sequence is {}.",
        start_index, end_index, sequence_length, max_count
    );
    let email = match Message::builder()
        .from(sender_mailbox)
        .to(receiver_mailbox)
        .subject(subject)
        .body(body)
    {
        Ok(email) => email,
        Err(err) => {
            error!("Failed to construct email: {}", err);
            return Ok(());
        }
    };
    match mailer.send(&email) {
        Ok(_) => Ok(()),
        Err(err) => {
            error!("Failed to send email: {}", err);
            Ok(())
        }
    }
}
