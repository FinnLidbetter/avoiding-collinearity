use hmac::{Hmac, Mac};
use log::error;
use regex::Regex;
use sha2::Sha256;

type HmacSha256 = Hmac<Sha256>;

fn hmacsha256(data: String, key: &[u8]) -> [u8; 32] {
    let mut mac = HmacSha256::new_from_slice(key).expect("HMAC can take key of any size");
    mac.update(data.as_bytes());
    let result = mac.finalize();
    result
        .into_bytes()
        .as_slice()
        .try_into()
        .expect("HMAC produced slice of incorrect length")
}

fn get_signature_key(
    key: String,
    date_stamp: String,
    region_name: String,
    service_name: String,
) -> [u8; 32] {
    let date_stamp_matcher = Regex::new(r"^\d{4}(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])$").unwrap();
    if !date_stamp_matcher.is_match(&date_stamp) {
        error!("{} is not a valid YYYYMMDD date.", &date_stamp);
    }
    let initial_key = format!("{}{}", "AWS4".to_string(), key);
    let mut signature_key = hmacsha256(date_stamp, initial_key.as_bytes());
    signature_key = hmacsha256(region_name, &signature_key);
    signature_key = hmacsha256(service_name, &signature_key);
    signature_key = hmacsha256(String::from("aws4_request"), &signature_key);
    signature_key
}

/// Encode an array of bytes as a hexadecimal String.
fn hex_encode(bytes: &[u8]) -> String {
    let mut hex = String::new();
    for byte in bytes.iter() {
        let left = byte / 16;
        let right = byte % 16;
        hex.push(char::from_digit(left as u32, 16).unwrap());
        hex.push(char::from_digit(right as u32, 16).unwrap());
    }
    hex.to_string()
}

#[cfg(test)]
mod tests {
    use crate::aws_signing::{get_signature_key, hex_encode};

    /// Test constructing a signing key.
    ///
    /// This example is taken from
    /// https://docs.aws.amazon.com/general/latest/gr/signature-v4-examples.html#signature-v4-examples-other
    #[test]
    fn test_get_signature_key() {
        let test_key = String::from("wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY");
        let date_stamp = String::from("20120215");
        let region_name = String::from("us-east-1");
        let service_name = String::from("iam");
        let signature_key = get_signature_key(test_key, date_stamp, region_name, service_name);
        assert_eq!(
            hex_encode(&signature_key),
            "f4780e2d9f65fa895f9c67b32ce1baf0b0d8a43505a000a1a9e090d414db404d"
        )
    }
}
