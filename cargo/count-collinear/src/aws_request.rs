use crate::aws_signing::{get_signature_key, hmacsha256, SIGNING_ALGORITHM};
use crate::utilities::{hex_encode, percent_encode, trim_whitespace};
use chrono::{SecondsFormat, Utc};
use sha2::{Digest, Sha256};
use std::collections::HashMap;
use std::fmt;
use std::fmt::Formatter;

type Result<T> = std::result::Result<T, AWSRequestError>;

#[derive(Debug, Clone)]
pub struct AWSRequestError {
    pub msg: String,
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
/// There used to be a note about double-encoding the '=' sign in parameter values
/// but I do not see that any more at the new URL and double-encoding '=' also appeared
/// to give the incorrect result.
fn uri_encode(character: char) -> Result<String> {
    if character.is_ascii_alphanumeric() {
        return Ok(character.to_string());
    } else if character == '_' || character == '-' || character == '~' || character == '.' {
        return Ok(character.to_string());
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

fn get_canonical_headers(headers: &HashMap<&str, &str>) -> Result<String> {
    if !headers.contains_key("Host") {
        return Err(AWSRequestError {
            msg: String::from("Missing 'Host' header."),
        });
    }
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

fn get_hash(payload: &str) -> String {
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
    signing_headers: &HashMap<&str, &str>,
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

    let hash_payload = get_hash(payload.unwrap_or(""));
    canonical_request.push_str(hash_payload.as_str());

    Ok(canonical_request)
}

fn get_credential_scope(header_date: &str, region: &str, service: &str) -> String {
    format!("{}/{}/{}/aws4_request", header_date, region, service)
}

fn request_string_to_sign(
    method: &str,
    endpoint: &str,
    params: HashMap<&str, &str>,
    signing_headers: &HashMap<&str, &str>,
    payload: Option<&str>,
    region: &str,
    service: &str,
) -> Result<String> {
    let header_date_time = *signing_headers.get("X-Amz-Date").ok_or(AWSRequestError {
        msg: String::from("Missing header 'X-Amz-Date'"),
    })?;
    let header_date = &header_date_time[..8];
    let canonical_request = canonical_request(method, endpoint, params, signing_headers, payload)?;
    let canonical_request_hash = get_hash(canonical_request.as_str());
    let credential_scope = get_credential_scope(header_date, region, service);
    let mut string_to_sign = String::new();
    string_to_sign.push_str(SIGNING_ALGORITHM);
    string_to_sign.push_str("\n");
    string_to_sign.push_str(header_date_time);
    string_to_sign.push_str("\n");
    string_to_sign.push_str(credential_scope.as_str());
    string_to_sign.push_str("\n");
    string_to_sign.push_str(canonical_request_hash.as_str());
    Ok(string_to_sign)
}

fn get_request_signature(
    method: &str,
    endpoint: &str,
    params: HashMap<&str, &str>,
    signing_headers: &HashMap<&str, &str>,
    payload: Option<&str>,
    region: &str,
    service: &str,
    secret_key: &str,
) -> Result<String> {
    let header_date_time = *signing_headers.get("X-Amz-Date").ok_or(AWSRequestError {
        msg: String::from("Missing header 'X-Amz-Date'"),
    })?;
    let header_date = &header_date_time[..8];
    let string_to_sign = request_string_to_sign(
        method,
        endpoint,
        params,
        signing_headers,
        payload,
        region,
        service,
    )?;
    let signing_key = get_signature_key(secret_key, header_date, region, service);
    let request_signature = hmacsha256(&string_to_sign, &signing_key);
    Ok(hex_encode(&request_signature))
}

pub fn get_authorization_header(
    method: &str,
    endpoint: &str,
    params: HashMap<&str, &str>,
    signing_headers: &HashMap<&str, &str>,
    payload: Option<&str>,
    region: &str,
    service: &str,
    access_key_id: &str,
    secret_key: &str,
) -> Result<String> {
    let signing_header_names = signing_headers.keys().map(|key| *key).collect();
    let canonical_header_names = get_canonical_header_names(signing_header_names);
    let header_date_time = *signing_headers.get("X-Amz-Date").ok_or(AWSRequestError {
        msg: String::from("Missing header 'X-Amz-Date'"),
    })?;
    let header_date = &header_date_time[..8];
    let credential_scope = get_credential_scope(header_date, region, service);
    let signature = get_request_signature(
        method,
        endpoint,
        params,
        signing_headers,
        payload,
        region,
        service,
        secret_key,
    )?;
    Ok(format!(
        "{} Credential={}/{}, SignedHeaders={}, Signature={}",
        SIGNING_ALGORITHM, access_key_id, credential_scope, canonical_header_names, signature
    ))
}

pub fn get_base_headers<'a>(service: &'a str, region: &'a str) -> HashMap<&'a str, String> {
    let mut headers = HashMap::<&str, String>::new();
    headers.insert("Host", format!("{}.{}.amazonaws.com", service, region));
    let now = Utc::now();
    let iso_time = now.to_rfc3339_opts(SecondsFormat::Secs, true);
    let stripped_iso_time: String = iso_time
        .chars()
        .filter(|ch| *ch != '-' && *ch != ':')
        .collect();
    headers.insert("X-Amz-Date", stripped_iso_time);
    headers
}

#[cfg(test)]
mod tests {
    use super::*;
    use std::collections::HashMap;

    // Setup constants for use in tests. These values are from the example:
    //  https://docs.aws.amazon.com/general/latest/gr/sigv4-create-canonical-request.html
    const METHOD: &str = "GET";
    const ENDPOINT: &str = "https://iam.amazonaws.com/?Action=ListUsers&Version=2010-05-08";
    const PARAMS_ARR: [(&str, &str); 2] = [("Action", "ListUsers"), ("Version", "2010-05-08")];
    const HEADERS_ARR: [(&str, &str); 3] = [
        (
            "Content-Type",
            "application/x-www-form-urlencoded; charset=utf-8",
        ),
        ("Host", "iam.amazonaws.com"),
        ("X-Amz-Date", "20150830T123600Z"),
    ];
    const PAYLOAD: Option<&str> = None;
    const REGION: &str = "us-east-1";
    const SERVICE: &str = "iam";
    const EXAMPLE_ACCESS_KEY: &str = "AKIDEXAMPLE";
    const EXAMPLE_SECRET_KEY: &str = "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY";

    /// Test SHA256 hash of empty string.
    #[test]
    fn test_payload_hash() {
        assert_eq!(
            get_hash(""),
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
        let params = HashMap::from(PARAMS_ARR);
        let headers = HashMap::from(HEADERS_ARR);
        let canonical_request =
            canonical_request(METHOD, ENDPOINT, params, &headers, PAYLOAD).unwrap();
        let expected_canonical_request = "GET
/
Action=ListUsers&Version=2010-05-08
content-type:application/x-www-form-urlencoded; charset=utf-8
host:iam.amazonaws.com
x-amz-date:20150830T123600Z

content-type;host;x-amz-date
e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        assert_eq!(canonical_request, expected_canonical_request);
        let hashed_canonical_request = get_hash(canonical_request.as_str());
        let expected_hash = "f536975d06c0309214f805bb90ccff089219ecd68b2577efef23edd43b7e1a59";
        assert_eq!(hashed_canonical_request, expected_hash);
    }

    #[test]
    fn test_string_to_sign() {
        let params = HashMap::from(PARAMS_ARR);
        let headers = HashMap::from(HEADERS_ARR);
        let string_to_sign =
            request_string_to_sign(METHOD, ENDPOINT, params, &headers, PAYLOAD, REGION, SERVICE)
                .unwrap();
        let expected_string_to_sign = "AWS4-HMAC-SHA256
20150830T123600Z
20150830/us-east-1/iam/aws4_request
f536975d06c0309214f805bb90ccff089219ecd68b2577efef23edd43b7e1a59";
        assert_eq!(string_to_sign, expected_string_to_sign);
    }

    #[test]
    fn test_request_signature() {
        let params = HashMap::from(PARAMS_ARR);
        let headers = HashMap::from(HEADERS_ARR);
        let request_signature = get_request_signature(
            METHOD,
            ENDPOINT,
            params,
            &headers,
            PAYLOAD,
            REGION,
            SERVICE,
            EXAMPLE_SECRET_KEY,
        )
        .unwrap();
        assert_eq!(
            request_signature.as_str(),
            "5d672d79c15b13162d9279b0855cfba6789a8edb4c82c400e06b5924a6f2b5d7"
        );
    }

    /// Test constructing the authorization header for a request.
    #[test]
    fn test_get_authorization_header() {
        let params = HashMap::from(PARAMS_ARR);
        let headers = HashMap::from(HEADERS_ARR);
        let authorization_header = get_authorization_header(
            METHOD,
            ENDPOINT,
            params,
            &headers,
            PAYLOAD,
            REGION,
            SERVICE,
            EXAMPLE_ACCESS_KEY,
            EXAMPLE_SECRET_KEY,
        );
        let expected_authorization_header = "AWS4-HMAC-SHA256 Credential=AKIDEXAMPLE/20150830/us-east-1/iam/aws4_request, SignedHeaders=content-type;host;x-amz-date, Signature=5d672d79c15b13162d9279b0855cfba6789a8edb4c82c400e06b5924a6f2b5d7";
    }
}
