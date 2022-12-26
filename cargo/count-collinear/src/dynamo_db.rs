use crate::aws_request::{get_authorization_header, get_base_headers};
use crate::utilities::split_top_level_comma_separated;
use http::HeaderMap;
use regex::Regex;
use std::collections::{BTreeMap, HashMap};
use std::convert::TryInto;
use std::fmt;
use std::fmt::Formatter;
use std::str::FromStr;

const API_VERSION: &str = "2012-08-10";
const SERVICE_NAME: &str = "dynamodb";
type Result<T> = std::result::Result<T, DynamoDbError>;

#[derive(Debug, Clone)]
pub struct DynamoDbError {
    msg: String,
}

impl fmt::Display for DynamoDbError {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        write!(f, "DynamoDB Error: {}", self.msg)
    }
}

#[derive(Clone, Debug, PartialEq)]
#[allow(dead_code)]
pub enum AttributeValue {
    Binary(Vec<u8>),
    BinarySet(Vec<Vec<u8>>),
    Boolean(bool),
    List(Vec<AttributeValue>),
    Map(BTreeMap<String, AttributeValue>),
    Null(bool),
    Number(String),
    NumberSet(Vec<String>),
    String(String),
    StringSet(Vec<String>),
}

impl AttributeValue {
    fn name(&self) -> &str {
        match self {
            AttributeValue::Binary(_) => "B",
            AttributeValue::BinarySet(_) => "BS",
            AttributeValue::Boolean(_) => "BOOL",
            AttributeValue::List(_) => "L",
            AttributeValue::Map(_) => "M",
            AttributeValue::Null(_) => "NULL",
            AttributeValue::Number(_) => "N",
            AttributeValue::NumberSet(_) => "NS",
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

    fn decode_binary_value_to_bytes(value: &str) -> Result<Vec<u8>> {
        let trimmed_value = value.trim();
        let stripped_value = trimmed_value.strip_prefix('"').unwrap_or(trimmed_value);
        let stripped_value = stripped_value.strip_suffix('"').unwrap_or(stripped_value);
        base64::decode(stripped_value).map_err(|err| DynamoDbError {
            msg: format!("Could not decode binary Attribute Value: {}", err),
        })
    }

    fn decode_binary(value: &str) -> Result<AttributeValue> {
        Ok(AttributeValue::Binary(
            AttributeValue::decode_binary_value_to_bytes(value)?,
        ))
    }

    fn decode_binary_set(value: &str) -> Result<AttributeValue> {
        let trimmed_value = value.trim();
        let stripped_value = trimmed_value.strip_prefix('[').unwrap_or(trimmed_value);
        let stripped_value = stripped_value.strip_suffix(']').unwrap_or(stripped_value);
        if stripped_value.trim().is_empty() {
            return Ok(AttributeValue::BinarySet(vec![]));
        }
        let binary_values = stripped_value.split(',');
        let mut binary_vecs: Vec<Vec<u8>> = Vec::new();
        for binary_value in binary_values {
            binary_vecs.push(AttributeValue::decode_binary_value_to_bytes(binary_value)?);
        }
        Ok(AttributeValue::BinarySet(binary_vecs))
    }

    fn decode_bool(value: &str) -> Result<AttributeValue> {
        let trimmed_value = value.trim();
        match trimmed_value {
            "true" => Ok(AttributeValue::Boolean(true)),
            "false" => Ok(AttributeValue::Boolean(false)),
            _ => Err(DynamoDbError {
                msg: format!("Error decoding {} to a BOOL value.", value),
            }),
        }
    }

    fn decode_list(value: &str) -> Result<AttributeValue> {
        let trimmed_value = value.trim();
        let stripped_value = value.strip_prefix('[').unwrap_or(trimmed_value);
        let stripped_value = stripped_value.strip_suffix(']').unwrap_or(stripped_value);
        let attr_value_strs = split_top_level_comma_separated(stripped_value);
        let attr_values: Result<Vec<AttributeValue>> = attr_value_strs
            .iter()
            .map(|val| AttributeValue::from_str(*val))
            .collect();
        Ok(AttributeValue::List(attr_values?))
    }

    fn decode_map(value: &str) -> Result<AttributeValue> {
        let trimmed_value = value.trim();
        let stripped_value = value.strip_prefix('{').unwrap_or(trimmed_value);
        let stripped_value = stripped_value.strip_suffix('}').unwrap_or(stripped_value);
        let named_attr_values = split_top_level_comma_separated(stripped_value);
        let name_matcher = Regex::new(r#""([^"]*)":"#).unwrap();
        let mut map: BTreeMap<String, AttributeValue> = BTreeMap::new();
        for named_attr_value in named_attr_values.into_iter() {
            let name = &name_matcher
                .captures(named_attr_value)
                .ok_or(DynamoDbError {
                    msg: format!(
                        "Could not parse attribute value name from {} when decoding map from {}",
                        named_attr_value, value
                    ),
                })?[1];
            let name_match = name_matcher.find(named_attr_value).unwrap();
            let remainder = &named_attr_value[name_match.end() + 1..];
            let attr_value = AttributeValue::from_str(remainder)?;
            map.insert(name.to_string(), attr_value);
        }
        Ok(AttributeValue::Map(map))
    }

    fn decode_null(value: &str) -> Result<AttributeValue> {
        let trimmed_value = value.trim();
        match trimmed_value {
            "true" => Ok(AttributeValue::Null(true)),
            "false" => Ok(AttributeValue::Null(false)),
            _ => Err(DynamoDbError {
                msg: format!("Error decoding {} to a NULL value.", value),
            }),
        }
    }

    fn decode_quoted_number(value: &str) -> Result<String> {
        let trimmed_value = value.trim();
        let stripped_value = trimmed_value.strip_prefix('"').unwrap_or(trimmed_value);
        let stripped_value = stripped_value.strip_suffix('"').unwrap_or(stripped_value);
        // Assume that the value should be able to be decoded into f64.
        let _number: f64 = stripped_value.parse().map_err(|_| DynamoDbError {
            msg: format!("Could not parse number value '{}'", stripped_value),
        })?;
        Ok(stripped_value.to_string())
    }

    fn decode_number(value: &str) -> Result<AttributeValue> {
        Ok(AttributeValue::Number(
            AttributeValue::decode_quoted_number(value)?,
        ))
    }

    fn decode_number_set(value: &str) -> Result<AttributeValue> {
        let trimmed_value = value.trim();
        let stripped_value = trimmed_value.strip_prefix('[').unwrap_or(trimmed_value);
        let stripped_value = stripped_value.strip_suffix(']').unwrap_or(stripped_value);
        if stripped_value.trim().is_empty() {
            return Ok(AttributeValue::NumberSet(vec![]));
        }
        let number_values = stripped_value.split(',');
        let mut numbers: Vec<String> = Vec::new();
        for number in number_values {
            numbers.push(AttributeValue::decode_quoted_number(number)?);
        }
        Ok(AttributeValue::NumberSet(numbers))
    }

    fn decode_quoted_string(value: &str) -> String {
        let trimmed_value = value.trim();
        let stripped_value = trimmed_value.strip_prefix('"').unwrap_or(trimmed_value);
        let stripped_value = stripped_value.strip_suffix('"').unwrap_or(stripped_value);
        stripped_value.to_string()
    }

    fn decode_string(value: &str) -> Result<AttributeValue> {
        Ok(AttributeValue::String(
            AttributeValue::decode_quoted_string(value),
        ))
    }

    fn decode_string_set(value: &str) -> Result<AttributeValue> {
        let trimmed_value = value.trim();
        let stripped_value = trimmed_value.strip_prefix('[').unwrap_or(trimmed_value);
        let stripped_value = stripped_value.strip_suffix(']').unwrap_or(stripped_value);
        if stripped_value.trim().is_empty() {
            return Ok(AttributeValue::StringSet(vec![]));
        }
        let string_values = stripped_value.split(',');
        let mut strings: Vec<String> = Vec::new();
        for string in string_values {
            strings.push(AttributeValue::decode_quoted_string(string));
        }
        Ok(AttributeValue::StringSet(strings))
    }
}

impl FromStr for AttributeValue {
    type Err = DynamoDbError;

    fn from_str(s: &str) -> std::result::Result<Self, Self::Err> {
        let trimmed = s.trim();
        let unbraced = trimmed.strip_prefix('{').unwrap_or(trimmed);
        let unbraced = unbraced.strip_suffix('}').unwrap_or(unbraced);
        let name_matcher = Regex::new(r#""([A-Z]+)":"#).unwrap();
        let captures = name_matcher.captures(unbraced).ok_or(DynamoDbError {
            msg: format!("Could not decode AttributeValue name from {}", s),
        })?;
        let name = &captures[1];
        let colon_index = unbraced.find(':').unwrap();
        let value = (unbraced[colon_index + 1..]).trim();
        let attr_value = match name {
            "B" => AttributeValue::decode_binary(value)?,
            "BS" => AttributeValue::decode_binary_set(value)?,
            "BOOL" => AttributeValue::decode_bool(value)?,
            "L" => AttributeValue::decode_list(value)?,
            "M" => AttributeValue::decode_map(value)?,
            "N" => AttributeValue::decode_number(value)?,
            "NS" => AttributeValue::decode_number_set(value)?,
            "NULL" => AttributeValue::decode_null(value)?,
            "S" => AttributeValue::decode_string(value)?,
            "SS" => AttributeValue::decode_string_set(value)?,
            _ => {
                return Err(DynamoDbError {
                    msg: format!("Unexpected AttributeValue name '{}'", name),
                })
            }
        };
        Ok(attr_value)
    }
}

fn encode_parameters(parameters: &[AttributeValue]) -> String {
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

    pub fn get_payload(
        partiql_statement: &str,
        parameters: &[AttributeValue],
        next_token: Option<String>,
    ) -> String {
        let encoded_parameters = encode_parameters(parameters);
        let next_token_str = match next_token {
            Some(next_token) => format!(", \"NextToken\": {}", next_token),
            None => String::from(""),
        };
        let payload = format!(
            "{{\"Statement\": \"{}\", \"Parameters\": {}{}}}",
            partiql_statement, encoded_parameters, next_token_str,
        );
        payload
    }

    pub fn execute_statement(
        &self,
        partiql_statement: &str,
        parameters: Vec<AttributeValue>,
        next_token: Option<String>,
    ) -> Result<DynamoDbExecuteStatementResponse> {
        let query_params: Vec<(&str, &str)> = Vec::new();
        let method = "POST";
        let endpoint = self.get_endpoint();
        let endpoint = endpoint.as_str();
        let base_headers = get_base_headers(SERVICE_NAME, self.region.as_str());
        let mut headers: HashMap<&str, &str> = base_headers
            .iter()
            .map(|(key, value)| (*key, value.as_str()))
            .collect();
        let target_string = format!("DynamoDB_{}.ExecuteStatement", API_VERSION.replace('-', ""));
        headers.insert("X-Amz-Target", target_string.as_str());
        let content_type = "application/x-amz-json-1.0";
        headers.insert("Content-Type", content_type);
        let payload = DynamoDbController::get_payload(partiql_statement, &parameters, next_token);
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
            msg: format!(
                "Failed to convert headers into a HeaderMap due to '{}'",
                err
            ),
        })?;
        request = request.headers(header_map);
        request = request.query(&query_params);
        request = request.body(payload);
        // println!("Making query with {}", partiql_statement);
        let result = request.send().map_err(|err| DynamoDbError {
            msg: format!("Request to {} failed due to '{}'", endpoint, err),
        })?;
        // println!("Finished query.");
        // println!("Text response: {}", result.text().unwrap());
        // println!("Response ^");
        // Err(DynamoDbError{msg: "Error".to_string()})

        result.error_for_status_ref().map_err(|err| DynamoDbError {
            msg: format!("Request to {} failed due to '{}'", endpoint, err),
        })?;
        let mut response = DynamoDbExecuteStatementResponse::from_str(
            result
                .text()
                .map_err(|_| DynamoDbError {
                    msg: "Bad text response from DynamoDb ExecuteStatement request. \
                Unable to parse response body."
                        .to_string(),
                })?
                .as_str(),
        )?;
        match response.next_token {
            Some(ref next_token) => {
                let next_response = self.execute_statement(
                    partiql_statement,
                    parameters,
                    Some(next_token.to_string()),
                )?;
                response.items.extend(next_response.items);
                Ok(response)
            }
            None => Ok(response),
        }
    }
}

#[derive(Debug, PartialEq)]
pub struct DynamoDbExecuteStatementResponse {
    pub items: Vec<BTreeMap<String, AttributeValue>>,
    next_token: Option<String>,
}

impl DynamoDbExecuteStatementResponse {
    fn parse_items_from_string(items_str: &str) -> Result<Vec<BTreeMap<String, AttributeValue>>> {
        let trimmed = items_str.trim();
        let stripped = trimmed.strip_prefix('[').unwrap_or(trimmed);
        let stripped = stripped.strip_suffix(']').unwrap_or(stripped);
        if stripped.trim().is_empty() {
            return Ok(vec![]);
        }
        let item_strs = split_top_level_comma_separated(stripped);
        let mut items: Vec<BTreeMap<String, AttributeValue>> = Vec::new();
        for item_str in item_strs {
            let trimmed_item_str = item_str.trim();
            let stripped_item_str = trimmed_item_str
                .strip_prefix('{')
                .unwrap_or(trimmed_item_str);
            let stripped_item_str = stripped_item_str
                .strip_suffix('}')
                .unwrap_or(stripped_item_str);
            let named_attr_value_strs = split_top_level_comma_separated(stripped_item_str);
            let name_matcher = Regex::new(r#""(.*)"\s*:"#).unwrap();
            let mut item: BTreeMap<String, AttributeValue> = BTreeMap::new();
            for named_attr_value in named_attr_value_strs {
                let name = &name_matcher
                    .captures(named_attr_value)
                    .ok_or(DynamoDbError {
                        msg: format!(
                            "Could not parse attribute value name from '{}' in '{}'",
                            named_attr_value, item_str
                        ),
                    })?[1];
                let colon_index = named_attr_value.find(':').unwrap();
                let remainder = &named_attr_value[colon_index + 1..];
                let attr_value = AttributeValue::from_str(remainder.trim())?;
                item.insert(name.to_string(), attr_value);
            }
            items.push(item)
        }
        Ok(items)
    }
}

impl FromStr for DynamoDbExecuteStatementResponse {
    type Err = DynamoDbError;

    fn from_str(s: &str) -> std::result::Result<Self, Self::Err> {
        let trimmed = s.trim();
        let stripped = trimmed.strip_prefix('{').unwrap_or(trimmed);
        let stripped = stripped.strip_suffix('}').unwrap_or(stripped);
        let top_level_strs = split_top_level_comma_separated(stripped);
        for some_str in top_level_strs.iter() {
            println!("{}", some_str);
        }
        let mut items_str = "[]";
        let mut next_token_str: Option<&str> = None;
        let items_name_regex = Regex::new(r#"^"Items"\s*:"#).unwrap();
        let next_token_name_regex = Regex::new(r#"^"NextToken"\s*:"#).unwrap();
        for value in top_level_strs {
            let trimmed_value = value.trim();
            if let Some(items_match) = items_name_regex.find(trimmed_value) {
                items_str = (&trimmed_value[items_match.end() + 1..]).trim();
            } else if let Some(token_match) = next_token_name_regex.find(trimmed_value) {
                next_token_str = Some(&trimmed_value[token_match.end() + 1..]);
            }
        }
        let items = DynamoDbExecuteStatementResponse::parse_items_from_string(items_str)?;
        let next_token = match next_token_str {
            Some(next_token_str) => {
                let trimmed_next_token = next_token_str.trim();
                let stripped_next_token = trimmed_next_token
                    .strip_prefix('"')
                    .unwrap_or(trimmed_next_token);
                let stripped_next_token = stripped_next_token
                    .strip_suffix('"')
                    .unwrap_or(stripped_next_token);
                Some(stripped_next_token.to_string())
            }
            None => None,
        };
        Ok(DynamoDbExecuteStatementResponse { items, next_token })
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_encode_number() {
        let item_1 = AttributeValue::Number("1".to_string());
        assert_eq!(item_1.encode(), r#"{"N": "1"}"#);

        let item_2 = AttributeValue::Number("0".to_string());
        assert_eq!(item_2.encode(), r#"{"N": "0"}"#);

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
        assert_eq!(true_item.encode(), r#"{"BOOL": true}"#);

        let false_item = AttributeValue::Boolean(false);
        assert_eq!(false_item.encode(), r#"{"BOOL": false}"#);
    }

    #[test]
    fn test_encode_number_set() {
        let values: Vec<String> = vec!["0", "-1", "1", "1.0", "-9.432"]
            .iter()
            .map(|value| value.to_string())
            .collect();

        let item = AttributeValue::NumberSet(values);
        let expected = r#"{"NS": ["0", "-1", "1", "1.0", "-9.432"]}"#;
        assert_eq!(item.encode(), expected);

        let empty_number_set = AttributeValue::NumberSet(vec![]);
        let expected = r#"{"NS": []}"#;
        assert_eq!(empty_number_set.encode(), expected);
    }

    #[test]
    fn test_encode_null() {
        let true_null = AttributeValue::Null(true);
        assert_eq!(true_null.encode(), r#"{"NULL": true}"#);

        let false_null = AttributeValue::Null(false);
        assert_eq!(false_null.encode(), r#"{"NULL": false}"#);
    }

    #[test]
    fn test_encode_string() {
        let item_1 = AttributeValue::String("Something".to_string());
        assert_eq!(item_1.encode(), r#"{"S": "Something"}"#);

        let item_2 = AttributeValue::String("a 😁".to_string());
        assert_eq!(item_2.encode(), r#"{"S": "a 😁"}"#);
    }

    #[test]
    fn test_encode_string_set() {
        let strings: Vec<String> = vec!["", "abc", "Cat"]
            .iter()
            .map(|value| value.to_string())
            .collect();
        let string_set_item = AttributeValue::StringSet(strings);
        let expected = r#"{"SS": ["", "abc", "Cat"]}"#;
        assert_eq!(string_set_item.encode(), expected);

        let empty_string_set = AttributeValue::StringSet(vec![]);
        let expected = r#"{"SS": []}"#;
        assert_eq!(empty_string_set.encode(), expected);
    }

    #[test]
    fn test_encode_binary() {
        let data: Vec<u8> = vec![0, 1, 3, 255];
        let data_b64 = base64::encode(&data);
        assert_eq!(data_b64, "AAED/w==");
        let item = AttributeValue::Binary(data);
        let expected = r#"{"B": "AAED/w=="}"#;
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
        let expected = r#"{"BS": ["FSp7", "Bw==", "gG8="]}"#;
        assert_eq!(item.encode(), expected);

        let empty_binary_set = AttributeValue::BinarySet(vec![]);
        let expected = r#"{"BS": []}"#;
        assert_eq!(empty_binary_set.encode(), expected);
    }

    #[test]
    fn test_encode_list() {
        let number_item = AttributeValue::Number("0".to_string());
        let true_item = AttributeValue::Boolean(true);
        let nested_empty_list = AttributeValue::List(vec![]);
        assert_eq!(nested_empty_list.encode(), r#"{"L": []}"#);

        let list_item = AttributeValue::List(vec![number_item, true_item, nested_empty_list]);
        let expected = r#"{"L": [{"N": "0"}, {"BOOL": true}, {"L": []}]}"#;
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
        let expected = r#"{"M": {"boolean thing": {"BOOL": false}, "number Thing": {"N": "-1"}, "some_sort_of_list": {"L": [{"N": "-1"}, {"BOOL": false}]}}}"#;
        let map_item = AttributeValue::Map(items_map);
        assert_eq!(map_item.encode(), expected);
    }

    #[test]
    fn test_encode_parameters() {
        let empty_list: Vec<AttributeValue> = Vec::new();
        assert_eq!(encode_parameters(&empty_list), "[]");
        let unit_list = vec![AttributeValue::Number("-1".to_string())];
        assert_eq!(encode_parameters(&unit_list), r#"[{"N": "-1"}]"#);
        let two_list = vec![
            AttributeValue::Number("-1".to_string()),
            AttributeValue::String("something".to_string()),
        ];
        assert_eq!(
            encode_parameters(&two_list),
            r#"[{"N": "-1"},{"S": "something"}]"#
        );
        let three_list = vec![
            AttributeValue::Number("42".to_string()),
            AttributeValue::Boolean(true),
            AttributeValue::Boolean(false),
        ];
        assert_eq!(
            encode_parameters(&three_list),
            r#"[{"N": "42"},{"BOOL": true},{"BOOL": false}]"#
        );
    }

    #[test]
    fn test_get_payload() {
        let partiql_statement = "SELECT * FROM collinearity WHERE sequence_length=?";
        let parameters = vec![AttributeValue::Number(100.to_string())];
        let payload = DynamoDbController::get_payload(partiql_statement, &parameters, None);
        assert_eq!(
            payload,
            r#"{"Statement": "SELECT * FROM collinearity WHERE sequence_length=?", "Parameters": [{"N": "100"}]}"#
        );
    }

    #[test]
    fn test_decode_binary() {
        let data: Vec<u8> = vec![0, 1, 3, 255];
        let data_b64 = base64::encode(&data);
        assert_eq!(data_b64, "AAED/w==");
        let item = AttributeValue::Binary(data);
        let decoded = AttributeValue::from_str(item.encode().as_str()).unwrap();
        assert_eq!(decoded, item);
        let expected = r#"{"B": "AAED/w=="}"#;
        assert_eq!(item.encode(), expected);
        assert_eq!(AttributeValue::from_str(expected).unwrap(), item);
        let extra_spaces = r#"{ "B":   "AAED/w=="    }"#;
        assert_eq!(AttributeValue::from_str(extra_spaces).unwrap(), item);
        let no_braces = r#""B": "AAED/w==""#;
        assert_eq!(AttributeValue::from_str(no_braces).unwrap(), item);
    }

    #[test]
    fn test_decode_binary_set() {
        let data_0: Vec<u8> = vec![21, 42, 123];
        let data_1: Vec<u8> = vec![7];
        let data_2: Vec<u8> = vec![128, 111];
        let item = AttributeValue::BinarySet(vec![data_0, data_1, data_2]);
        let expected = r#"{"BS": ["FSp7", "Bw==", "gG8="]}"#;
        assert_eq!(item.encode(), expected);
        assert_eq!(
            AttributeValue::from_str(item.encode().as_str()).unwrap(),
            item
        );

        let empty_binary_set = AttributeValue::BinarySet(vec![]);
        let expected = r#"{"BS": []}"#;
        assert_eq!(empty_binary_set.encode(), expected);
        assert_eq!(
            AttributeValue::from_str(empty_binary_set.encode().as_str()).unwrap(),
            empty_binary_set
        );

        let unit_binary_set = AttributeValue::BinarySet(vec![vec![21, 42, 123, 24, 12]]);
        assert_eq!(
            AttributeValue::from_str(unit_binary_set.encode().as_str()).unwrap(),
            unit_binary_set
        );
    }

    #[test]
    fn test_decode_number() {
        let numbers = ["1", "0", "-2", "-3.1234", "2139.412"];
        for number in numbers.iter() {
            let item = AttributeValue::Number(format!("{}", number));
            assert_eq!(
                AttributeValue::from_str(item.encode().as_str()).unwrap(),
                item
            );
        }
    }

    #[test]
    fn test_decode_number_set() {
        let values: Vec<String> = vec!["0", "-1", "1", "1.0", "-9.432"]
            .iter()
            .map(|value| value.to_string())
            .collect();

        let item = AttributeValue::NumberSet(values);
        let expected = r#"{"NS": ["0", "-1", "1", "1.0", "-9.432"]}"#;
        assert_eq!(item.encode(), expected);
        assert_eq!(AttributeValue::from_str(expected).unwrap(), item);

        let empty_number_set = AttributeValue::NumberSet(vec![]);
        let expected = r#"{"NS": []}"#;
        assert_eq!(empty_number_set.encode(), expected);
        assert_eq!(
            AttributeValue::from_str(expected).unwrap(),
            empty_number_set
        );
    }

    #[test]
    fn test_decode_null() {
        let true_null = AttributeValue::Null(true);
        assert_eq!(
            AttributeValue::from_str(r#"{"NULL": true}"#).unwrap(),
            true_null
        );
        let false_null = AttributeValue::Null(false);
        assert_eq!(
            AttributeValue::from_str(r#"{"NULL": false}"#).unwrap(),
            false_null
        );
    }

    #[test]
    fn test_decode_string() {
        let item_1 = AttributeValue::String("Something".to_string());
        assert_eq!(
            AttributeValue::from_str(r#"{"S": "Something"}"#).unwrap(),
            item_1
        );
        assert_eq!(
            AttributeValue::from_str(item_1.encode().as_str()).unwrap(),
            item_1
        );

        let item_2 = AttributeValue::String("a 😁".to_string());
        assert_eq!(
            AttributeValue::from_str(r#"{"S": "a 😁"}"#).unwrap(),
            item_2
        );
        assert_eq!(
            AttributeValue::from_str(item_2.encode().as_str()).unwrap(),
            item_2
        );
    }

    #[test]
    fn test_decode_string_set() {
        let strings: Vec<String> = vec!["", "abc", "Cat"]
            .iter()
            .map(|value| value.to_string())
            .collect();
        let string_set_item = AttributeValue::StringSet(strings);
        assert_eq!(
            AttributeValue::from_str(r#"{"SS": ["", "abc", "Cat"]}"#).unwrap(),
            string_set_item
        );
        assert_eq!(
            AttributeValue::from_str(string_set_item.encode().as_str()).unwrap(),
            string_set_item
        );

        let empty_string_set = AttributeValue::StringSet(vec![]);
        assert_eq!(
            AttributeValue::from_str(r#"{"SS": []}"#).unwrap(),
            empty_string_set
        );
        assert_eq!(
            AttributeValue::from_str(empty_string_set.encode().as_str()).unwrap(),
            empty_string_set
        );
    }

    #[test]
    fn test_decode_bool() {
        let true_item = AttributeValue::Boolean(true);
        assert_eq!(
            AttributeValue::from_str(r#"{"BOOL": true}"#).unwrap(),
            true_item
        );

        let false_item = AttributeValue::Boolean(false);
        assert_eq!(
            AttributeValue::from_str(r#"{"BOOL": false}"#).unwrap(),
            false_item
        );
    }

    #[test]
    fn test_decode_list() {
        let number_item = AttributeValue::Number("0".to_string());
        let true_item = AttributeValue::Boolean(true);
        let nested_empty_list = AttributeValue::List(vec![]);
        assert_eq!(
            AttributeValue::from_str(nested_empty_list.encode().as_str()).unwrap(),
            nested_empty_list
        );

        let unit_list_item = AttributeValue::List(vec![true_item.clone()]);
        assert_eq!(
            AttributeValue::from_str(unit_list_item.encode().as_str()).unwrap(),
            unit_list_item
        );

        let list_item = AttributeValue::List(vec![number_item, true_item, nested_empty_list]);
        assert_eq!(
            AttributeValue::from_str(list_item.encode().as_str()).unwrap(),
            list_item
        );
    }

    #[test]
    fn test_decode_map() {
        let number_item = AttributeValue::Number("-1".to_string());
        let false_item = AttributeValue::Boolean(false);
        let list_item = AttributeValue::List(vec![number_item.clone(), false_item.clone()]);
        let mut items_map = BTreeMap::new();
        items_map.insert("number Thing".to_string(), number_item);
        items_map.insert("boolean thing".to_string(), false_item);
        items_map.insert("some_sort_of_list".to_string(), list_item);
        let map_item = AttributeValue::Map(items_map);
        assert_eq!(
            AttributeValue::from_str(map_item.encode().as_str()).unwrap(),
            map_item
        );
    }

    #[test]
    fn test_decode_execute_statement_items() {
        let response = r#"{
            "ConsumedCapacity": {
                "CapacityUnits": 1,
                "GlobalSecondaryIndexes": {
                    "string" : {
                        "CapacityUnits": 1,
                        "ReadCapacityUnits": 1,
                        "WriteCapacityUnits": 1
                    }
                },
                "LocalSecondaryIndexes": {
                    "string" : {
                        "CapacityUnits": 1,
                        "ReadCapacityUnits": 1,
                        "WriteCapacityUnits": 1
                    }
                },
                "ReadCapacityUnits": 1,
                "Table": {
                    "CapacityUnits": 1,
                    "ReadCapacityUnits": 1,
                    "WriteCapacityUnits": 1
                },
                "TableName": "string",
                "WriteCapacityUnits": 1
            },
            "Items": [
                {
                    "string_1" : {
                        "BOOL": true
                    },
                    "string_2": {
                        "S": "some_string"
                    }
                },
                {
                    "another_key": {
                        "L": [
                            {"N": "0"}, {"S": "string"}
                        ]
                    }
                }
            ],
            "LastEvaluatedKey": {
                "string" : {
                    "N": "1"
                },
                "other_string": {
                    "S": "something"
                }
            },
            "NextToken": "some_token"
        }"#;
        let decoded_response = DynamoDbExecuteStatementResponse::from_str(response).unwrap();
        let expected_items = vec![
            BTreeMap::from([
                ("string_1".to_string(), AttributeValue::Boolean(true)),
                (
                    "string_2".to_string(),
                    AttributeValue::String("some_string".to_string()),
                ),
            ]),
            BTreeMap::from([(
                "another_key".to_string(),
                AttributeValue::List(vec![
                    AttributeValue::Number("0".to_string()),
                    AttributeValue::String("string".to_string()),
                ]),
            )]),
        ];
        let expected = DynamoDbExecuteStatementResponse {
            items: expected_items,
            next_token: Some(String::from("some_token")),
        };
        assert_eq!(decoded_response, expected)
    }
}
