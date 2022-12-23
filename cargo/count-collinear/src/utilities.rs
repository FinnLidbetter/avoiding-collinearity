/// Encode an array of bytes as a lowercase hexadecimal String.
pub fn hex_encode(bytes: &[u8]) -> String {
    let mut hex = String::new();
    for byte in bytes.iter() {
        let left = byte / 16;
        let right = byte % 16;
        hex.push(char::from_digit(left as u32, 16).unwrap());
        hex.push(char::from_digit(right as u32, 16).unwrap());
    }
    hex.to_string()
}

/// Remove leading, trailing, and consecutive whitespace.
pub fn trim_whitespace(value: &str) -> String {
    let words: Vec<&str> = value.trim().split_whitespace().collect();
    words.join(" ")
}

/// Percent encode bytes as uppercase hexadecimal.
pub fn percent_encode(bytes: &[u8]) -> String {
    let mut hex = String::new();
    for byte in bytes.iter() {
        let left = byte / 16;
        let right = byte % 16;
        hex.push('%');
        hex.push(
            char::from_digit(left as u32, 16)
                .unwrap()
                .to_ascii_uppercase(),
        );
        hex.push(
            char::from_digit(right as u32, 16)
                .unwrap()
                .to_ascii_uppercase(),
        );
    }
    hex.to_string()
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_percent_encode() {
        let bytes = "=".as_bytes();
        assert_eq!(percent_encode(bytes), "%3D");
    }
}
