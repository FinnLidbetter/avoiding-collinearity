use std::collections::HashMap;
use std::fmt;
use std::fmt::Formatter;

type Result<T> = std::result::Result<T, AWSRequestError>;

#[derive(Debug, Clone)]
pub struct AWSRequestError {
    msg: String,
}

impl fmt::Display for AWSRequestError {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        write!(f, "{}", format!("SqsError: {}", self.msg))
    }
}

fn get_canonical_uri(endpoint: &str) -> Result<String> {
    let host_end = "amazonaws.com";
    let host_end_start_byte_index = match endpoint.find(host_end) {
        Some(index) => index,
        None => {
            return Err(AWSRequestError {
                msg: format!("Endpoint {} does not contain {}", endpoint, host_end),
            })
        }
    };
    let mut canonical_uri = &endpoint[host_end_start_byte_index + host_end.len()..];
    if canonical_uri.len() == 0 {
        canonical_uri = "/";
    }
    Ok(canonical_uri.to_string())
}

fn percent_encode(bytes: &[u8]) -> String {
    let mut hex = String::new();
    for byte in bytes.iter() {
        let left = byte / 16;
        let right = byte % 16;
        hex.push('%');
        hex.push(char::from_digit(left as u32, 16).unwrap());
        hex.push(char::from_digit(right as u32, 16).unwrap());
    }
    hex.to_string()
}

/// URI encode a character.
///
/// This is according to:
/// https://docs.aws.amazon.com/general/latest/gr/sigv4-create-canonical-request.html
/// Note that the case where the character is an '=' is not handled.
fn uri_encode(character: char) -> Result<String> {
    if character.is_ascii_alphanumeric() {
        return Ok(character.to_string());
    } else if character == '_' || character == '-' || character == '~' || character == '.' {
        return Ok(character.to_string());
    } else if character == '=' {
        return Err(AWSRequestError {
            msg: String::from("URI encoding '=' is not supported"),
        });
    }
    let mut buffer: [u8; 4] = [0; 4];
    let character_bytes = character.encode_utf8(&mut buffer);
    return Ok(percent_encode(character_bytes.as_bytes()));
}

/// URI encode a string.
fn uri_encode_string(string: &str) -> Result<String> {
    let mut uri_encoded_string = String::new();
    for ch in string.chars() {
        uri_encoded_string.push_str(uri_encode(ch)?.as_str());
    }
    Ok(uri_encoded_string)
}

/// Construct a canonical query string.
///
/// This is according to:
/// https://docs.aws.amazon.com/general/latest/gr/sigv4-create-canonical-request.html
fn get_canonical_query_string(params: HashMap<&str, &str>) -> Result<String> {
    let mut params_vec: Vec<(&str, &str)> = vec![];
    for (key, value) in params.iter() {
        params_vec.push((*key, *value));
    }
    params_vec.sort();
    let mut canonical_query_string = String::new();
    let mut is_first = true;
    for (key, value) in params_vec.iter() {
        if !is_first {
            canonical_query_string.push('&');
        } else {
            is_first = false;
        }
        let encoded_key = uri_encode_string(key).map_err(|err| AWSRequestError {
            msg: format!("Failed to encode key '{}' due to {}", key, err.msg),
        })?;
        canonical_query_string.push_str(encoded_key.as_str());
        canonical_query_string.push('=');
        let encoded_value = uri_encode_string(value).map_err(|err| AWSRequestError {
            msg: format!("Failed to encode value '{}' due to {}", value, err.msg),
        })?;
        canonical_query_string.push_str(encoded_value.as_str());
    }
    Ok(canonical_query_string)
}

/// Construct a canonical AWS request.
///
/// This is according to:
/// https://docs.aws.amazon.com/general/latest/gr/sigv4-create-canonical-request.html
fn canonical_request(method: &str, endpoint: &str, params: HashMap<&str, &str>) -> Result<String> {
    let mut canonical_request = String::new();
    canonical_request.push_str(method);
    canonical_request.push_str("\n");

    let canonical_uri = get_canonical_uri(endpoint)?;
    canonical_request.push_str(canonical_uri.as_str());
    canonical_request.push_str("\n");

    let canonical_query_string = get_canonical_query_string(params)?;
    canonical_request.push_str(canonical_query_string.as_str());
    canonical_request.push_str("\n");



    Ok(canonical_request)
}
