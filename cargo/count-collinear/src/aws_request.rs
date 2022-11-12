use crate::utilities::{hex_encode, percent_encode, trim_whitespace};
use sha2::{Digest, Sha256};
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
    let query_string_start_byte_index = match endpoint.find("?") {
        Some(index) => index,
        None => endpoint.len(),
    };
    let mut canonical_uri =
        &endpoint[host_end_start_byte_index + host_end.len()..query_string_start_byte_index];
    if canonical_uri.len() == 0 {
        canonical_uri = "/";
    }
    let fragments: Vec<&str> = canonical_uri
        .split("/")
        .filter(|fragment| *fragment != "")
        .collect();
    let mut encoded_fragments: Vec<String> = vec![];
    for fragment in fragments {
        let encoded_fragment = uri_encode_string(uri_encode_string(fragment)?.as_str())?;
        encoded_fragments.push(encoded_fragment);
    }
    let mut result = String::from("/");
    result.push_str(encoded_fragments.join("/").as_str());
    if canonical_uri.len() > 1 && &canonical_uri[canonical_uri.len() - 1..] == "/" {
        result.push_str("/");
    }
    Ok(result)
}

/// URI encode a character.
///
/// This is according to:
/// https://docs.aws.amazon.com/general/latest/gr/sigv4-create-canonical-request.html
fn uri_encode(character: char) -> Result<String> {
    if character.is_ascii_alphanumeric() {
        return Ok(character.to_string());
    } else if character == '_' || character == '-' || character == '~' || character == '.' {
        return Ok(character.to_string());
    } else if character == '=' {
        return Ok(String::from("%253D"));
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

fn get_canonical_headers(headers: HashMap<&str, &str>) -> Result<String> {
    let mut sorted_headers = headers
        .iter()
        .map(|(key, value)| ((*key).to_lowercase(), *value))
        .collect::<Vec<(String, &str)>>();
    sorted_headers.sort();
    let mut canonical_headers = String::new();
    for (key, value) in sorted_headers {
        canonical_headers.push_str(key.as_str());
        canonical_headers.push(':');
        let trimmed_value = trim_whitespace(value);
        canonical_headers.push_str(trimmed_value.as_str());
        canonical_headers.push_str("\n");
    }
    Ok(canonical_headers)
}

fn get_canonical_header_names(header_names: Vec<&str>) -> String {
    let mut sorted_headers: Vec<String> = header_names
        .iter()
        .map(|name| (*name).to_lowercase())
        .collect::<Vec<String>>();
    sorted_headers.sort();
    let canonical_header_names = sorted_headers.join(";");
    canonical_header_names
}

fn get_payload_hash(payload: &str) -> String {
    let mut hasher = Sha256::new();
    hasher.update(payload.as_bytes());
    let result = hasher.finalize();
    let bytes: [u8; 32] = result
        .as_slice()
        .try_into()
        .expect("Hash produced slice of incorrect length");
    hex_encode(&bytes)
}

/// Construct a canonical AWS request.
///
/// This is according to:
/// https://docs.aws.amazon.com/general/latest/gr/sigv4-create-canonical-request.html
fn canonical_request(
    method: &str,
    endpoint: &str,
    params: HashMap<&str, &str>,
    signing_headers: HashMap<&str, &str>,
    payload: Option<&str>,
) -> Result<String> {
    let mut canonical_request = String::new();
    canonical_request.push_str(method.to_uppercase().as_str());
    canonical_request.push_str("\n");

    let canonical_uri = get_canonical_uri(endpoint)?;
    canonical_request.push_str(canonical_uri.as_str());
    canonical_request.push_str("\n");

    let canonical_query_string = get_canonical_query_string(params)?;
    canonical_request.push_str(canonical_query_string.as_str());
    canonical_request.push_str("\n");

    let signing_header_names = signing_headers.keys().map(|key| *key).collect();

    let canonical_headers = get_canonical_headers(signing_headers)?;
    canonical_request.push_str(canonical_headers.as_str());
    canonical_request.push_str("\n");

    let canonical_signed_header_names = get_canonical_header_names(signing_header_names);
    canonical_request.push_str(canonical_signed_header_names.as_str());
    canonical_request.push_str("\n");

    let hash_payload = get_payload_hash(payload.unwrap_or(""));
    canonical_request.push_str(hash_payload.as_str());
    //canonical_request.push_str("\n");

    Ok(canonical_request)
}

#[cfg(test)]
mod tests {
    use crate::aws_request::{canonical_request, get_canonical_uri, get_payload_hash};
    use std::collections::HashMap;

    /// Test SHA256 hash of empty string.
    #[test]
    fn test_payload_hash() {
        assert_eq!(
            get_payload_hash(""),
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        );
    }

    /// The canonical uri can be extracted from a url.
    #[test]
    fn test_canonical_uri() {
        // It works without a trailing slash.
        assert_eq!(get_canonical_uri("https://iam.amazonaws.com").unwrap(), "/");
        // It works without a path.
        assert_eq!(
            get_canonical_uri("https://iam.amazonaws.com/").unwrap(),
            "/"
        );
        // It works with a query string.
        assert_eq!(
            get_canonical_uri("https://iam.amazonaws.com/?Action=ListUsers&Version=2010-05-08")
                .unwrap(),
            "/"
        );
        // It works with a path and no query string.
        assert_eq!(
            get_canonical_uri("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue").unwrap(),
            "/123456789012/MyQueue"
        );
        // It works with a path with trailing slash and no query string.
        assert_eq!(
            get_canonical_uri("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue/").unwrap(),
            "/123456789012/MyQueue/"
        );
        // It works with a path and a query string.
        assert_eq!(
            get_canonical_uri(
                "https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue?Action=ReceiveMessage"
            )
            .unwrap(),
            "/123456789012/MyQueue"
        );
        // It works with a path with trailing slash and a query string.
        assert_eq!(
            get_canonical_uri(
                "https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue/?Action=ReceiveMessage"
            )
            .unwrap(),
            "/123456789012/MyQueue/"
        );
    }

    /// Test constructing a canonical request.
    ///
    /// This test uses the example request
    ///     GET https://iam.amazonaws.com/?Action=ListUsers&Version=2010-05-08 HTTP/1.1
    ///     Host: iam.amazonaws.com
    ///     Content-Type: application/x-www-form-urlencoded; charset=utf-8
    ///     X-Amz-Date: 20150830T123600Z
    /// provided at https://docs.aws.amazon.com/general/latest/gr/sigv4-create-canonical-request.html
    #[test]
    fn test_canonical_request() {
        let method = "GET";
        let endpoint = "https://iam.amazonaws.com/?Action=ListUsers&Version=2010-05-08";
        let params = HashMap::from([("Action", "ListUsers"), ("Version", "2010-05-08")]);
        let headers = HashMap::from([
            (
                "Content-Type",
                "application/x-www-form-urlencoded; charset=utf-8",
            ),
            ("Host", "iam.amazonaws.com"),
            ("X-Amz-Date", "20150830T123600Z"),
        ]);
        let payload = None;
        let canonical_request =
            canonical_request(method, endpoint, params, headers, payload).unwrap();
        let expected_canonical_request = "GET
/
Action=ListUsers&Version=2010-05-08
content-type:application/x-www-form-urlencoded; charset=utf-8
host:iam.amazonaws.com
x-amz-date:20150830T123600Z

content-type;host;x-amz-date
e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        assert_eq!(canonical_request, expected_canonical_request);
        let hashed_canonical_request = get_payload_hash(canonical_request.as_str());
        let expected_hash = "f536975d06c0309214f805bb90ccff089219ecd68b2577efef23edd43b7e1a59";
        assert_eq!(hashed_canonical_request, expected_hash);
    }
}
