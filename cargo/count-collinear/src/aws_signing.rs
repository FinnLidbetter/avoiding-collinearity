use hmac::{Hmac, Mac};
use log::error;
use regex::Regex;
use sha2::Sha256;

pub const SIGNING_ALGORITHM: &str = "AWS4-HMAC-SHA256";

type HmacSha256 = Hmac<Sha256>;

pub fn hmacsha256(data: &str, key: &[u8]) -> [u8; 32] {
    let mut mac = HmacSha256::new_from_slice(key).expect("HMAC can take key of any size");
    mac.update(data.as_bytes());
    let result = mac.finalize();
    result
        .into_bytes()
        .as_slice()
        .try_into()
        .expect("HMAC produced slice of incorrect length")
}

pub fn get_signature_key(
    key: &str,
    date_stamp: &str,
    region_name: &str,
    service_name: &str,
) -> [u8; 32] {
    let date_stamp_matcher = Regex::new(r"^\d{4}(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])$").unwrap();
    if !date_stamp_matcher.is_match(date_stamp) {
        error!("{} is not a valid YYYYMMDD date.", date_stamp);
    }
    let initial_key = format!("AWS4{}", key);
    let mut signature_key = hmacsha256(date_stamp, initial_key.as_bytes());
    signature_key = hmacsha256(region_name, &signature_key);
    signature_key = hmacsha256(service_name, &signature_key);
    signature_key = hmacsha256("aws4_request", &signature_key);
    signature_key
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::utilities::hex_encode;

    /// Test constructing a signing key.
    ///
    /// The first example is taken from
    ///   https://docs.aws.amazon.com/general/latest/gr/signature-v4-examples.html#signature-v4-examples-other
    /// The second example is from
    ///   https://docs.aws.amazon.com/general/latest/gr/sigv4-calculate-signature.html
    #[test]
    fn test_get_signature_key() {
        let test_key = "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY";
        let date_stamp = "20120215";
        let region_name = "us-east-1";
        let service_name = "iam";
        let signature_key = get_signature_key(test_key, date_stamp, region_name, service_name);
        assert_eq!(
            hex_encode(&signature_key),
            "f4780e2d9f65fa895f9c67b32ce1baf0b0d8a43505a000a1a9e090d414db404d"
        );
        let date_stamp_2 = "20150830";
        assert_eq!(
            hex_encode(&get_signature_key(
                test_key,
                date_stamp_2,
                region_name,
                service_name
            )),
            "c4afb1cc5771d871763a393e44b703571b55cc28424d1a5e86da6ed3c154a4b9"
        );
    }
}
