use std::collections::HashMap;
use std::fmt;
use std::fmt::Formatter;

const RECEIVE_MESSAGES_MAX: i32 = 10;
const DEFAULT_VISIBILITY_TIMEOUT: i32 = 12 * 60 * 60 - 10;
const API_VERSION: &str = "2012-11-05";
type Result<T> = std::result::Result<T, SqsError>;

#[derive(Debug, Clone)]
pub struct SqsError {
    msg: String,
}

impl fmt::Display for SqsError {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        write!(f, "{}", format!("SqsError: {}", self.msg))
    }
}

pub struct SqsQueue {
    access_key: String,
    secret_access_key: String,
    account_number: String,
    region: String,
    queue_name: String,
}

impl SqsQueue {
    pub fn receive_message(&self, visibility_timeout: Option<i32>) -> Result<Option<SqsMessage>> {
        let message = self.receive_messages(1, visibility_timeout)?;
        Ok(message.get(0).cloned())
    }

    pub fn receive_messages(
        &self,
        num_messages: i32,
        visibility_timeout: Option<i32>,
    ) -> Result<Vec<SqsMessage>> {
        SqsQueue::check_num_messages_bounds(num_messages)?;
        let visibility_timeout = &visibility_timeout
            .unwrap_or(DEFAULT_VISIBILITY_TIMEOUT)
            .to_string();
        let params = HashMap::from([
            ("Action", "ReceiveMessage"),
            ("Version", API_VERSION),
            ("VisibilityTimeout", visibility_timeout),
            ("AttributeName", "All"),
            ("MaxNumberOfMessages", &num_messages.to_string()),
        ]);
        let action = "ReceiveMessage";
        Ok(vec![])
    }

    fn check_num_messages_bounds(num_messages: i32) -> Result<()> {
        if num_messages < 1 {
            return Err(SqsError {
                msg: format!(
                    "Requested {} messages. At least 1 is required.",
                    num_messages,
                ),
            });
        } else if num_messages > RECEIVE_MESSAGES_MAX {
            return Err(SqsError {
                msg: format!(
                    "Requested {} messages. At most {} are allowed.",
                    num_messages, RECEIVE_MESSAGES_MAX,
                ),
            });
        }
        Ok(())
    }

    fn get_endpoint(&self) -> String {
        format!(
            "https://sqs.{}.amazonaws.com/{}/{}",
            self.region, self.account_number, self.queue_name
        )
    }

    /// Delete a message from the queue.
    pub fn delete_message(&self, receipt_handle: &str) -> Result<()> {
        Ok(())
    }
}

#[derive(Debug, Clone)]
pub struct SqsMessage {
    message_id: String,
    receipt_handle: String,
    body: String,
}
