use crate::aws_request::{get_authorization_header, get_base_headers};
use base64;
use http::HeaderMap;
use std::collections::{BTreeMap, HashMap};
use std::convert::TryInto;
use std::fmt;
use std::fmt::Formatter;

const API_VERSION: &str = "2012-08-10";
const SERVICE_NAME: &str = "dynamodb";
type Result<T> = std::result::Result<T, DynamoDbError>;

#[derive(Debug, Clone)]
pub struct DynamoDbError {
    msg: String,
}

impl fmt::Display for DynamoDbError {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        write!(f, "{}", format!("DynamoDB Error: {}", self.msg))
    }
}

#[derive(Clone)]
pub enum AttributeValue {
    Binary(Vec<u8>),
    BinarySet(Vec<Vec<u8>>),
    Boolean(bool),
    List(Vec<AttributeValue>),
    Map(BTreeMap<String, AttributeValue>),
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
            AttributeValue::Binary(value) => format!("\"{}\"", base64::encode(value)),
            AttributeValue::Boolean(value) => format!("{}", value),
            AttributeValue::BinarySet(values) => {
                let encoded_values: Vec<String> = values
                    .iter()
                    .map(|data| format!("\"{}\"", base64::encode(data)))
                    .collect();
                format!("[{}]", encoded_values.join(", "))
            }
            AttributeValue::List(values) => {
                let encoded_values: Vec<String> =
                    values.iter().map(|value| value.encode()).collect();
                format!("[{}]", encoded_values.join(", "))
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
                format!("[{}]", quoted_values.join(", "))
            }
        };
        format!("{{\"{}\": {}}}", self.name(), value_string)
    }
}

fn encode_parameters(parameters: Vec<AttributeValue>) -> String {
    let parameter_strs: Vec<String> = parameters
        .iter()
        .map(|attr_val| attr_val.encode())
        .collect();
    format!("[{}]", parameter_strs.join(","))
}

pub struct DynamoDbController {
    access_key: String,
    secret_access_key: String,
    region: String,
    client: reqwest::blocking::Client,
}

impl DynamoDbController {
    pub fn new(access_key: &str, secret_access_key: &str, region: &str) -> DynamoDbController {
        let access_key = String::from(access_key);
        let secret_access_key = String::from(secret_access_key);
        let region = String::from(region);
        let client = reqwest::blocking::Client::new();
        DynamoDbController {
            access_key,
            secret_access_key,
            region,
            client,
        }
    }

    fn get_endpoint(&self) -> String {
        format!("https://dynamodb.{}.amazonaws.com", self.region)
    }

    pub fn get_payload(partiql_statement: &str, parameters: Vec<AttributeValue>) -> String {
        let encoded_parameters = encode_parameters(parameters);
        format!(
            "{{\"Statement\": \"{}\", \"Parameters\": {}}}",
            partiql_statement, encoded_parameters,
        )
    }

    pub fn execute_statement(
        &self,
        partiql_statement: &str,
        parameters: Vec<AttributeValue>,
    ) -> Result<u16> {
        let query_params: Vec<(&str, &str)> = Vec::new();
        let method = "POST";
        let endpoint = self.get_endpoint();
        let endpoint = endpoint.as_str();
        let base_headers = get_base_headers(SERVICE_NAME, self.region.as_str());
        let mut headers: HashMap<&str, &str> = base_headers
            .iter()
            .map(|(key, value)| (*key, value.as_str()))
            .collect();
        let target_string = format!("DynamoDB_{}.ExecuteStatement", API_VERSION.replace("-", ""));
        headers.insert("X-Amz-Target", target_string.as_str());
        let content_type = "application/x-amz-json-1.0";
        headers.insert("Content-Type", content_type);
        let payload = DynamoDbController::get_payload(partiql_statement, parameters);
        let payload_bytes = payload.as_bytes().len();
        let payload_bytes = payload_bytes.to_string();
        let payload_bytes = payload_bytes.as_str();
        headers.insert("Content-Length", payload_bytes);
        headers.insert("User-Agent", "collinearity");
        headers.insert("Accept-Encoding", "identity");
        let authorization_header = get_authorization_header(
            method,
            endpoint,
            HashMap::new(),
            &headers,
            Some(payload.as_str()),
            self.region.as_str(),
            SERVICE_NAME,
            self.access_key.as_str(),
            self.secret_access_key.as_str(),
        )
        .map_err(|err| DynamoDbError {
            msg: format!("Failed to get authorization header due to '{}'", err.msg),
        })?;
        headers.insert("Authorization", authorization_header.as_str());
        let headers: HashMap<String, String> = headers
            .iter()
            .map(|(key, value)| ((*key).to_string(), (*value).to_string()))
            .collect();

        let mut request = self.client.post(endpoint);
        let header_map: HeaderMap = (&headers).try_into().map_err(|err| DynamoDbError {
            msg: format!("Failed to convert headers into a HeaderMap due to {}", err),
        })?;
        request = request.headers(header_map);
        request = request.query(&query_params);
        request = request.body(payload);
        let result = request.send().map_err(|err| DynamoDbError {
            msg: format!("Request to {} failed due to {}", endpoint, err),
        })?;
        result.error_for_status_ref().map_err(|err| DynamoDbError {
            msg: format!("Request to {} failed due to {}", endpoint, err),
        })?;
        Ok(result.status().as_u16())
    }
}

#[cfg(test)]
mod tests {
    use super::*;

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

        let empty_number_set = AttributeValue::NumberSet(vec![]);
        let expected = "{\"NS\": []}";
        assert_eq!(empty_number_set.encode(), expected);
    }

    #[test]
    fn test_encode_null() {
        let true_null = AttributeValue::Null(true);
        assert_eq!(true_null.encode(), "{\"NULL\": true}");

        let false_null = AttributeValue::Null(false);
        assert_eq!(false_null.encode(), "{\"NULL\": false}");
    }

    #[test]
    fn test_encode_string() {
        let item_1 = AttributeValue::String("Something".to_string());
        assert_eq!(item_1.encode(), "{\"S\": \"Something\"}");

        let item_2 = AttributeValue::String("a 😁".to_string());
        assert_eq!(item_2.encode(), "{\"S\": \"a 😁\"}");
    }

    #[test]
    fn test_encode_string_set() {
        let strings: Vec<String> = vec!["", "abc", "Cat"]
            .iter()
            .map(|value| value.to_string())
            .collect();
        let string_set_item = AttributeValue::StringSet(strings);
        let expected = "{\"SS\": [\"\", \"abc\", \"Cat\"]}";
        assert_eq!(string_set_item.encode(), expected);

        let empty_string_set = AttributeValue::StringSet(vec![]);
        let expected = "{\"SS\": []}";
        assert_eq!(empty_string_set.encode(), expected);
    }

    #[test]
    fn test_encode_binary() {
        let data: Vec<u8> = vec![0, 1, 3, 255];
        let data_b64 = base64::encode(&data);
        assert_eq!(data_b64, "AAED/w==");
        let item = AttributeValue::Binary(data);
        let expected = "{\"B\": \"AAED/w==\"}";
        assert_eq!(item.encode(), expected);
    }

    #[test]
    fn test_encode_binary_set() {
        let data_0: Vec<u8> = vec![21, 42, 123];
        let data_1: Vec<u8> = vec![7];
        let data_2: Vec<u8> = vec![128, 111];
        let data_0_b64 = base64::encode(&data_0);
        let data_1_b64 = base64::encode(&data_1);
        let data_2_b64 = base64::encode(&data_2);
        assert_eq!(data_0_b64, "FSp7");
        assert_eq!(data_1_b64, "Bw==");
        assert_eq!(data_2_b64, "gG8=");
        let item = AttributeValue::BinarySet(vec![data_0, data_1, data_2]);
        let expected = "{\"BS\": [\"FSp7\", \"Bw==\", \"gG8=\"]}";
        assert_eq!(item.encode(), expected);

        let empty_binary_set = AttributeValue::BinarySet(vec![]);
        let expected = "{\"BS\": []}";
        assert_eq!(empty_binary_set.encode(), expected);
    }

    #[test]
    fn test_encode_list() {
        let number_item = AttributeValue::Number("0".to_string());
        let true_item = AttributeValue::Boolean(true);
        let nested_empty_list = AttributeValue::List(vec![]);

        let list_item = AttributeValue::List(vec![number_item, true_item, nested_empty_list]);
        let expected = "{\"L\": [{\"N\": \"0\"}, {\"BOOL\": true}, {\"L\": []}]}";
        assert_eq!(list_item.encode(), expected);
    }

    #[test]
    fn test_encode_map() {
        let number_item = AttributeValue::Number("-1".to_string());
        let false_item = AttributeValue::Boolean(false);
        let list_item = AttributeValue::List(vec![number_item.clone(), false_item.clone()]);
        let mut items_map = BTreeMap::new();
        items_map.insert("number Thing".to_string(), number_item);
        items_map.insert("boolean thing".to_string(), false_item);
        items_map.insert("some_sort_of_list".to_string(), list_item);
        let expected = "{\"M\": {\"boolean thing\": {\"BOOL\": false}, \"number Thing\": {\"N\": \"-1\"}, \"some_sort_of_list\": {\"L\": [{\"N\": \"-1\"}, {\"BOOL\": false}]}}}";
        let map_item = AttributeValue::Map(items_map);
        assert_eq!(map_item.encode(), expected);
    }

    #[test]
    fn test_encode_parameters() {
        let empty_list: Vec<AttributeValue> = Vec::new();
        assert_eq!(encode_parameters(empty_list), "[]");
        let unit_list = vec![AttributeValue::Number("-1".to_string())];
        assert_eq!(encode_parameters(unit_list), "[{\"N\": \"-1\"}]");
        let two_list = vec![
            AttributeValue::Number("-1".to_string()),
            AttributeValue::String("something".to_string()),
        ];
        assert_eq!(
            encode_parameters(two_list),
            "[{\"N\": \"-1\"},{\"S\": \"something\"}]"
        );
        let three_list = vec![
            AttributeValue::Number("42".to_string()),
            AttributeValue::Boolean(true),
            AttributeValue::Boolean(false),
        ];
        assert_eq!(
            encode_parameters(three_list),
            "[{\"N\": \"42\"},{\"BOOL\": true},{\"BOOL\": false}]"
        );
    }

    #[test]
    fn test_get_payload() {
        let partiql_statement = "SELECT * FROM collinearity WHERE sequence_length=?";
        let parameters = vec![AttributeValue::Number(100.to_string())];
        let payload = DynamoDbController::get_payload(partiql_statement, parameters);
        assert_eq!(
            payload,
            "{\"Statement\": \"SELECT * FROM collinearity WHERE sequence_length=?\", \"Parameters\": [{\"N\": \"100\"}]}"
        );
    }
}
