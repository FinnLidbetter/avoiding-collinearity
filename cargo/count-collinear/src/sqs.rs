use crate::aws_request::{get_authorization_header, get_base_headers};
use crate::{CollinearReader, CollinearReaderError, CountCollinearArgs};
use http::HeaderMap;
use quick_xml::events::Event;
use quick_xml::reader::Reader;
use std::collections::HashMap;
use std::convert::TryInto;
use std::fmt;
use std::fmt::Formatter;

const RECEIVE_MESSAGES_MAX: i32 = 10;
const DEFAULT_VISIBILITY_TIMEOUT: i32 = 12 * 60 * 60 - 10;
const API_VERSION: &str = "2012-11-05";
const SERVICE_NAME: &str = "sqs";
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

pub struct SqsController {
    access_key: String,
    secret_access_key: String,
    account_number: String,
    region: String,
    client: reqwest::blocking::Client,
}

impl SqsController {
    pub fn new(
        access_key: &str,
        secret_access_key: &str,
        account_number: &str,
        region: &str,
    ) -> SqsController {
        let access_key = String::from(access_key);
        let secret_access_key = String::from(secret_access_key);
        let account_number = String::from(account_number);
        let region = String::from(region);
        let client = reqwest::blocking::Client::new();
        SqsController {
            access_key,
            secret_access_key,
            account_number,
            region,
            client,
        }
    }

    fn get_endpoint(&self, queue_name: Option<&str>) -> String {
        match queue_name {
            Some(queue) => format!(
                "https://sqs.{}.amazonaws.com/{}/{}",
                self.region, self.account_number, queue
            ),
            None => format!("https://sqs.{}.amazonaws.com", self.region),
        }
    }

    /// Get a single message from the queue.
    pub fn receive_message(
        &self,
        queue_name: &str,
        visibility_timeout: Option<i32>,
    ) -> Result<Option<SqsMessage>> {
        let message = self.receive_messages(queue_name, 1, visibility_timeout)?;
        Ok(message.get(0).cloned())
    }

    /// Get the specified number of messages from the queue.
    pub fn receive_messages(
        &self,
        queue_name: &str,
        num_messages: i32,
        visibility_timeout: Option<i32>,
    ) -> Result<Vec<SqsMessage>> {
        SqsController::check_num_messages_bounds(num_messages)?;
        let visibility_timeout = visibility_timeout
            .unwrap_or(DEFAULT_VISIBILITY_TIMEOUT)
            .to_string();
        let visibility_timeout = visibility_timeout.as_str();
        let num_messages = num_messages.to_string();
        let num_messages = num_messages.as_str();
        let params: HashMap<&str, &str> = HashMap::from([
            ("VisibilityTimeout", visibility_timeout),
            ("AttributeName", "All"),
            ("MaxNumberOfMessages", num_messages),
        ]);
        let xml_result = self.get_xml_result("ReceiveMessage", Some(queue_name), params)?;
        Ok(SqsController::parse_receive_message_xml(&xml_result)?)
    }

    /// List the Sqs Queues available.
    pub fn list_queues(&self) -> Result<Vec<String>> {
        let params: HashMap<&str, &str> = HashMap::new();
        let xml_result = self.get_xml_result("ListQueues", None, params)?;
        Ok(SqsController::parse_list_queues_xml(&xml_result)?)
    }

    /// Delete a message from the queue.
    pub fn delete_message(&self, queue_name: &str, receipt_handle: &str) -> Result<()> {
        let params: HashMap<&str, &str> = HashMap::from([("ReceiptHandle", receipt_handle)]);
        let xml_result = self.get_xml_result("DeleteMessage", Some(queue_name), params)?;
        Ok(SqsController::parse_delete_message_xml(&xml_result)?)
    }

    /// Make a GET request with the specified action and parameters.
    ///
    /// The xml text response is returned or an SqsError.
    fn get_xml_result<'a>(
        &self,
        action: &'a str,
        queue_name: Option<&'a str>,
        mut custom_params: HashMap<&str, &'a str>,
    ) -> Result<String> {
        custom_params.insert("Action", action);
        custom_params.insert("Version", API_VERSION);
        let query_params: Vec<(&str, &str)> = custom_params
            .iter()
            .map(|(key, value)| (*key, *value))
            .collect();
        let method = "GET";
        let endpoint = self.get_endpoint(queue_name);
        let endpoint = endpoint.as_str();
        let base_headers = get_base_headers(SERVICE_NAME, self.region.as_str());
        let mut headers: HashMap<&str, &str> = base_headers
            .iter()
            .map(|(key, value)| (*key, value.as_str()))
            .collect();
        let authorization_header = get_authorization_header(
            method,
            endpoint,
            custom_params,
            &headers,
            None,
            self.region.as_str(),
            SERVICE_NAME,
            self.access_key.as_str(),
            self.secret_access_key.as_str(),
        )
        .map_err(|err| SqsError {
            msg: format!("Failed to get authorization header due to '{}'", err.msg),
        })?;
        headers.insert("Authorization", authorization_header.as_str());
        let headers: HashMap<String, String> = headers
            .iter()
            .map(|(key, value)| ((*key).to_string(), (*value).to_string()))
            .collect();

        let mut request = self.client.get(endpoint);
        let header_map: HeaderMap = (&headers).try_into().map_err(|err| SqsError {
            msg: format!("Failed to convert headers into a HeaderMap due to {}", err),
        })?;
        request = request.headers(header_map);
        request = request.query(&query_params);
        let result = request.send().map_err(|err| SqsError {
            msg: format!("Request to {} failed due to {}", endpoint, err),
        })?;
        result.error_for_status_ref().map_err(|err| SqsError {
            msg: format!("Request to {} failed due to {}", endpoint, err),
        })?;
        let xml_result = result.text().map_err(|err| SqsError {
            msg: format!("Failed to parse text from response: {}", err),
        })?;
        Ok(xml_result)
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

    /// Parse a vector of SqsMessage structs from an XML response.
    fn parse_receive_message_xml(xml_text: &str) -> Result<Vec<SqsMessage>> {
        let mut reader = Reader::from_str(xml_text);
        reader.trim_text(true);

        let mut messages: Vec<SqsMessage> = Vec::new();

        SqsController::seek_result_start_event(&mut reader, "ReceiveMessageResult")?;
        loop {
            let result = SqsController::parse_message(&mut reader)?;
            match result {
                Some(message) => messages.push(message),
                None => break,
            }
        }
        Ok(messages)
    }

    /// Get the queue urls from a ListQueues xml response.
    fn parse_list_queues_xml(xml_text: &str) -> Result<Vec<String>> {
        let mut reader = Reader::from_str(xml_text);
        reader.trim_text(true);
        let mut queues: Vec<String> = Vec::new();
        SqsController::seek_result_start_event(&mut reader, "ListQueuesResult")?;
        loop {
            let result = SqsController::parse_queue(&mut reader)?;
            match result {
                Some(queue_name) => queues.push(queue_name),
                None => break,
            }
        }
        Ok(queues)
    }

    fn parse_delete_message_xml(_xml_text: &str) -> Result<()> {
        Ok(())
    }

    /// Read events until the tag with name result_start is reached.
    fn seek_result_start_event(reader: &mut Reader<&[u8]>, result_start: &str) -> Result<()> {
        loop {
            match reader.read_event() {
                Err(e) => {
                    return Err(SqsError {
                        msg: format!(
                            "Error reading xml response at position {}: {:?}",
                            reader.buffer_position(),
                            e
                        ),
                    })
                }
                Ok(Event::Eof) => {
                    return Err(SqsError {
                        msg: format!(
                        "Unexpectedly reached EOF seeking result start event {} in XML response.",
                        result_start
                    ),
                    })
                }
                Ok(Event::Start(start_event)) => {
                    if start_event.name().as_ref() == result_start.as_bytes() {
                        return Ok(());
                    }
                }
                _ => (),
            }
        }
    }

    /// Parse a message.
    ///
    /// This assumes that the reader has just read the ReceiveMessageResult start event.
    fn parse_message(reader: &mut Reader<&[u8]>) -> Result<Option<SqsMessage>> {
        loop {
            match reader.read_event() {
                Err(e) => {
                    return Err(SqsError {
                        msg: format!(
                            "Error reading xml response at position {}: {:?}",
                            reader.buffer_position(),
                            e
                        ),
                    })
                }
                Ok(Event::Eof) => {
                    return Err(SqsError {
                        msg: String::from("Unexpectedly reached EOF parsing message result."),
                    })
                }
                Ok(Event::Start(start_event)) => {
                    return match start_event.name().as_ref() {
                        b"Message" => {
                            let mut body = String::new();
                            let mut receipt_handle = String::new();
                            let mut message_id = String::new();
                            loop {
                                match reader.read_event() {
                                    Err(e) => {
                                        return Err(SqsError {
                                            msg: format!(
                                                "Error reading xml response at position {}: {:?}",
                                                reader.buffer_position(),
                                                e
                                            ),
                                        })
                                    }
                                    Ok(Event::Eof) => {
                                        return Err(SqsError {
                                            msg: format!(
                                                "Unexpectedly reached EOF while parsing a Message."
                                            ),
                                        })
                                    }
                                    Ok(Event::Start(message_start_event)) => {
                                        match message_start_event.name().as_ref() {
                                            b"MessageId" => {
                                                message_id =
                                                    SqsController::parse_text(reader, "MessageId")?
                                            }
                                            b"ReceiptHandle" => {
                                                receipt_handle = SqsController::parse_text(
                                                    reader,
                                                    "ReceiptHandle",
                                                )?
                                            }
                                            b"Body" => {
                                                body = SqsController::parse_text(reader, "Body")?
                                            }
                                            _ => (),
                                        }
                                    }
                                    Ok(Event::End(message_end_event)) => {
                                        match message_end_event.name().as_ref() {
                                            b"Message" => break,
                                            _ => (),
                                        }
                                    }
                                    _ => (),
                                }
                            }
                            Ok(Some(SqsMessage {
                                message_id,
                                receipt_handle,
                                body,
                            }))
                        }
                        _ => Err(SqsError {
                            msg: format!("Encountered unexpected start event parsing message"),
                        }),
                    }
                }
                Ok(Event::End(end_event)) => return Ok(None),
                _ => (),
            };
        }
    }

    /// Parse a queue url.
    ///
    /// This assumes that the reader has just read the ListQueuesResult start event.
    fn parse_queue(reader: &mut Reader<&[u8]>) -> Result<Option<String>> {
        loop {
            match reader.read_event() {
                Err(e) => {
                    return Err(SqsError {
                        msg: format!(
                            "Error reading xml response at position {} parsing queue names: {:?}",
                            reader.buffer_position(),
                            e
                        ),
                    })
                }
                Ok(Event::Eof) => {
                    return Err(SqsError {
                        msg: String::from("Unexpectedly reached EOF parsing queue names."),
                    })
                }
                Ok(Event::Start(start_event)) => {
                    return match start_event.name().as_ref() {
                        b"QueueUrl" => {
                            let body = SqsController::parse_text(reader, "QueueUrl")?;
                            return Ok(Some(body));
                        }
                        _ => Err(SqsError {
                            msg: format!("Encountered unexpected start event parsing queue name."),
                        }),
                    }
                }
                Ok(Event::End(end_event)) => return Ok(None),
                _ => (),
            };
        }
    }

    /// Given a reader that has just read a start Event extract the value.
    ///
    /// The reader will keep reading until it encounters the end Event for
    /// the tag.
    fn parse_text(reader: &mut Reader<&[u8]>, tag: &str) -> Result<String> {
        let mut parsed_text = false;
        let mut result = String::new();
        let tag_bytes = tag.as_bytes();
        loop {
            match reader.read_event() {
                Err(e) => {
                    return Err(SqsError {
                        msg: format!(
                            "Error reading xml response at position {}: {:?}",
                            reader.buffer_position(),
                            e
                        ),
                    })
                }
                Ok(Event::Eof) => {
                    return Err(SqsError {
                        msg: format!("Unexpectedly reached EOF while parsing a Message."),
                    })
                }
                Ok(Event::Text(text)) => {
                    parsed_text = true;
                    result = text
                        .unescape()
                        .map_err(|err| SqsError {
                            msg: format!("Failed to decode {}", tag),
                        })?
                        .to_string();
                }
                Ok(Event::End(body_end)) => {
                    return match body_end.name().as_ref() {
                        _value @ tag_bytes => match parsed_text {
                            true => Ok(result),
                            false => Err(SqsError {
                                msg: format!("No value found for tag {}", tag),
                            }),
                        },
                        _ => Err(SqsError {
                            msg: String::from("Unexpected nesting in Body."),
                        }),
                    }
                }
                _ => (),
            }
        }
    }
}

#[derive(Debug, Clone, Eq, PartialEq)]
pub struct SqsMessage {
    message_id: String,
    pub(crate) receipt_handle: String,
    pub(crate) body: String,
}

#[cfg(test)]
mod tests {
    use crate::sqs::{SqsController, SqsMessage};

    #[test]
    fn test_parse_messages() {
        let response_text = "<ReceiveMessageResponse>
  <ReceiveMessageResult>
    <Message>
      <MessageId>5fea7756-0ea4-451a-a703-a558b933e274</MessageId>
      <ReceiptHandle>
        MbZj6wDWli+JvwwJaBV+3dcjk2YW2vA3+STFFljTM8tJJg6HRG6PYSasuWXPJB+Cw
        Lj1FjgXUv1uSj1gUPAWV66FU/WeR4mq2OKpEGYWbnLmpRCJVAyeMjeU5ZBdtcQ+QE
        auMZc8ZRv37sIW2iJKq3M9MFx1YvV11A2x/KSbkJ0=
      </ReceiptHandle>
      <MD5OfBody>fafb00f5732ab283681e124bf8747ed1</MD5OfBody>
      <Body>This is a test message</Body>
      <Attribute>
        <Name>SenderId</Name>
        <Value>195004372649</Value>
      </Attribute>
      <Attribute>
        <Name>SentTimestamp</Name>
        <Value>1238099229000</Value>
      </Attribute>
      <Attribute>
        <Name>ApproximateReceiveCount</Name>
        <Value>5</Value>
      </Attribute>
      <Attribute>
        <Name>ApproximateFirstReceiveTimestamp</Name>
        <Value>1250700979248</Value>
      </Attribute>
    </Message>
    <Message>
      <ReceiptHandle>Fake receipt handle</ReceiptHandle>
      <MessageId>Fake message id</MessageId>
      <Body>Fake body</Body>
    </Message>
  </ReceiveMessageResult>
  <ResponseMetadata>
    <RequestId>b6633655-283d-45b4-aee4-4e84e0ae6afa</RequestId>
  </ResponseMetadata>
</ReceiveMessageResponse>";
        let messages = SqsController::parse_receive_message_xml(response_text);
        let expected: Vec<SqsMessage> = vec![
            SqsMessage {
                message_id: "5fea7756-0ea4-451a-a703-a558b933e274".to_string(),
                receipt_handle: "MbZj6wDWli+JvwwJaBV+3dcjk2YW2vA3+STFFljTM8tJJg6HRG6PYSasuWXPJB+Cw
        Lj1FjgXUv1uSj1gUPAWV66FU/WeR4mq2OKpEGYWbnLmpRCJVAyeMjeU5ZBdtcQ+QE
        auMZc8ZRv37sIW2iJKq3M9MFx1YvV11A2x/KSbkJ0="
                    .to_string(),
                body: "This is a test message".to_string(),
            },
            SqsMessage {
                message_id: "Fake message id".to_string(),

                receipt_handle: "Fake receipt handle".to_string(),
                body: "Fake body".to_string(),
            },
        ];
        assert_eq!(messages.unwrap(), expected);
        let no_messages_response = "<ReceiveMessageResponse>
  <ReceiveMessageResult>
  </ReceiveMessageResult>
  <ResponseMetadata>
    <RequestId>b6633655-283d-45b4-aee4-4e84e0ae6afa</RequestId>
  </ResponseMetadata>
</ReceiveMessageResponse>";
        assert_eq!(
            SqsController::parse_receive_message_xml(no_messages_response).unwrap(),
            vec![]
        );
    }

    #[test]
    fn test_parse_no_queues() {
        let response_text = "<ListQueuesResponse>
    <ListQueuesResult>
    </ListQueuesResult>
    <ResponseMetadata>
        <RequestId>725275ae-0b9b-4762-b238-436d7c65a1ac</RequestId>
    </ResponseMetadata>
</ListQueuesResponse>";
        let parsed_queues = SqsController::parse_list_queues_xml(response_text);
        let empty_vec: Vec<String> = vec![];
        assert_eq!(parsed_queues.unwrap(), empty_vec);
    }

    #[test]
    fn test_parse_one_queue() {
        let response_text = "<ListQueuesResponse>
    <ListQueuesResult>
        <QueueUrl>https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue</QueueUrl>
    </ListQueuesResult>
    <ResponseMetadata>
        <RequestId>725275ae-0b9b-4762-b238-436d7c65a1ac</RequestId>
    </ResponseMetadata>
</ListQueuesResponse>";
        let parsed_queue = SqsController::parse_list_queues_xml(response_text);
        assert_eq!(
            parsed_queue.unwrap(),
            vec!("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue")
        );
    }

    #[test]
    fn test_multiple_queues() {
        let response_text = "<ListQueuesResponse>
    <ListQueuesResult>
        <QueueUrl>https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue</QueueUrl>
        <QueueUrl>https://sqs.us-east-1.amazonaws.com/123456789012/MyOtherQueue</QueueUrl>
        <QueueUrl>https://sqs.us-east-2.amazonaws.com/987654321/MySecretQueue</QueueUrl>
    </ListQueuesResult>
    <ResponseMetadata>
        <RequestId>725275ae-0b9b-4762-b238-436d7c65a1ac</RequestId>
    </ResponseMetadata>
</ListQueuesResponse>";
        let expected: Vec<String> = vec![
            String::from("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue"),
            String::from("https://sqs.us-east-1.amazonaws.com/123456789012/MyOtherQueue"),
            String::from("https://sqs.us-east-2.amazonaws.com/987654321/MySecretQueue"),
        ];
        assert_eq!(
            SqsController::parse_list_queues_xml(response_text).unwrap(),
            expected
        );
    }
}
