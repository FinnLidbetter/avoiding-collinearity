use crate::aws_request::{get_authorization_header, get_base_headers};
use base64;
use http::HeaderMap;
use std::collections::HashMap;
use std::convert::TryInto;
use std::fmt;
use std::fmt::Formatter;
use std::io::Bytes;

const API_VERSION: &str = "2012-08-10";
const SERVICE_NAME: &str = "dynamodb";
type Result<T> = std::result::Result<T, DynamoError>;

#[derive(Debug, Clone)]
pub struct DynamoError {
    msg: String,
}

impl fmt::Display for DynamoError {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        write!(f, "{}", format!("S3Error: {}", self.msg))
    }
}

enum AttributeValue {
    Binary(Vec<u8>),
    BinarySet(Vec<Vec<u8>>),
    Boolean(bool),
    List(Vec<AttributeValue>),
    Map(HashMap<String, AttributeValue>),
    Number(String),
    NumberSet(Vec<String>),
    Null(bool),
    String(String),
    StringSet(Vec<String>),
}

impl AttributeValue {
    fn name(&self) -> &str {
        match self {
            AttributeValue::Binary(_) => "B",
            AttributeValue::Boolean(_) => "BOOL",
            AttributeValue::BinarySet(_) => "BS",
            AttributeValue::List(_) => "L",
            AttributeValue::Map(_) => "M",
            AttributeValue::Number(_) => "N",
            AttributeValue::NumberSet(_) => "NS",
            AttributeValue::Null(_) => "NULL",
            AttributeValue::String(_) => "S",
            AttributeValue::StringSet(_) => "SS",
        }
    }

    fn encode(&self) -> String {
        let value_string = match self {
            AttributeValue::Binary(value) => base64::encode(value),
            AttributeValue::Boolean(value) => format!("{}", value),
            AttributeValue::BinarySet(values) => {
                let encoded_values: Vec<String> =
                    values.iter().map(|data| base64::encode(data)).collect();
                encoded_values.join(", ")
            }
            AttributeValue::List(values) => {
                let encoded_values: Vec<String> =
                    values.iter().map(|value| value.encode()).collect();
                encoded_values.join(", ")
            }
            AttributeValue::Map(values) => {
                let encoded_values: Vec<String> = values
                    .iter()
                    .map(|(key, value)| format!("\"{}\": {}", key, value.encode()))
                    .collect();
                format!("{{{}}}", encoded_values.join(", "))
            }
            AttributeValue::Number(value) => format!("\"{}\"", value),
            AttributeValue::NumberSet(values) => {
                let quoted_values: Vec<String> = values
                    .iter()
                    .map(|value| format!("\"{}\"", value))
                    .collect();
                format!("[{}]", quoted_values.join(", "))
            }
            AttributeValue::Null(value) => format!("{}", value),
            AttributeValue::String(value) => format!("\"{}\"", value),
            AttributeValue::StringSet(values) => {
                let quoted_values: Vec<String> = values
                    .iter()
                    .map(|value| format!("\"{}\"", value))
                    .collect();
                quoted_values.join(", ")
            }
        };
        format!("{{\"{}\": {}}}", self.name(), value_string)
    }
}

pub struct DynamoController {
    access_key: String,
    secret_access_key: String,
    account_number: String,
    region: String,
    client: reqwest::blocking::Client,
}

impl DynamoController {
    pub fn new(
        access_key: &str,
        secret_access_key: &str,
        account_number: &str,
        region: &str,
    ) -> DynamoController {
        let access_key = String::from(access_key);
        let secret_access_key = String::from(secret_access_key);
        let account_number = String::from(account_number);
        let region = String::from(region);
        let client = reqwest::blocking::Client::new();
        DynamoController {
            access_key,
            secret_access_key,
            account_number,
            region,
            client,
        }
    }
}

#[cfg(test)]
mod tests {
    use crate::dynamo_db::AttributeValue;
    use quick_xml::events::attributes::Attr;

    #[test]
    fn test_encode_number() {
        let item_1 = AttributeValue::Number("1".to_string());
        assert_eq!(item_1.encode(), "{\"N\": \"1\"}");

        let item_2 = AttributeValue::Number("0".to_string());
        assert_eq!(item_2.encode(), "{\"N\": \"0\"}");

        let numbers = ["1", "0", "-2", "-3.1234", "2139.412"];
        for number in numbers.iter() {
            let item = AttributeValue::Number(format!("{}", number));
            let expected = format!("{{\"N\": \"{}\"}}", number);
            assert_eq!(item.encode(), expected);
        }
    }

    #[test]
    fn test_encode_bool() {
        let true_item = AttributeValue::Boolean(true);
        assert_eq!(true_item.encode(), "{\"BOOL\": true}");

        let false_item = AttributeValue::Boolean(false);
        assert_eq!(false_item.encode(), "{\"BOOL\": false}");
    }

    #[test]
    fn test_encode_number_set() {
        let values: Vec<String> = vec!["0", "-1", "1", "1.0", "-9.432"]
            .iter()
            .map(|value| value.to_string())
            .collect();

        let item = AttributeValue::NumberSet(values);
        let expected = "{\"NS\": [\"0\", \"-1\", \"1\", \"1.0\", \"-9.432\"]}";
        assert_eq!(item.encode(), expected);
    }

    #[test]
    fn test_encode_null() {
        let true_null = AttributeValue::Null(true);
        assert_eq!(true_null.encode(), "{\"NULL\": true}");

        let false_null = AttributeValue::Null(false);
        assert_eq!(false_null.encode(), "{\"NULL\": false}");
    }
}
