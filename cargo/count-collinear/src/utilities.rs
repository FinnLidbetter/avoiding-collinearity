use regex::Regex;
use std::cmp::Ordering;

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

#[derive(Debug, Eq, PartialEq)]
struct JsonEvent {
    event_type: JsonEventType,
    position: usize,
}
impl PartialOrd for JsonEvent {
    fn partial_cmp(&self, other: &Self) -> Option<Ordering> {
        Some(self.cmp(other))
    }
}

impl Ord for JsonEvent {
    fn cmp(&self, other: &Self) -> Ordering {
        self.position.cmp(&other.position)
    }
}

#[derive(Debug, Eq, PartialEq)]
enum JsonEventType {
    LeftBracket(char),
    RightBracket(char),
    Comma,
    DoubleQuote,
}

pub fn split_top_level_comma_separated(value: &str) -> Vec<&str> {
    let value = value.trim();
    let left_brackets = Regex::new(r#"[{(\[]"#).unwrap();
    let right_brackets = Regex::new(r#"[]})]"#).unwrap();
    let left_bracket_matches = left_brackets.find_iter(value);
    let right_bracket_matches = right_brackets.find_iter(value);
    let comma_indices = value.match_indices(',');
    let double_quote_indices = value.match_indices('"').filter(|quote_match| {
        quote_match.0 == 0 || &value[quote_match.0 - 1..quote_match.0] != r"\"
    });
    let mut json_events: Vec<JsonEvent> = left_bracket_matches
        .map(|left_bracket_match| JsonEvent {
            event_type: JsonEventType::LeftBracket(
                value.as_bytes()[left_bracket_match.start()] as char,
            ),
            position: left_bracket_match.start(),
        })
        .collect();
    json_events.extend(right_bracket_matches.map(|right_bracket_match| JsonEvent {
        event_type: JsonEventType::RightBracket(
            value.as_bytes()[right_bracket_match.start()] as char,
        ),
        position: right_bracket_match.start(),
    }));
    json_events.extend(comma_indices.map(|comma_match| JsonEvent {
        event_type: JsonEventType::Comma,
        position: comma_match.0,
    }));
    json_events.extend(double_quote_indices.map(|quote_match| JsonEvent {
        event_type: JsonEventType::DoubleQuote,
        position: quote_match.0,
    }));
    json_events.sort();
    let mut split_indices: Vec<usize> = Vec::new();
    let mut inside_quotes = false;
    let mut nesting = 0;
    for event in json_events {
        match event.event_type {
            JsonEventType::LeftBracket(_) => {
                if !inside_quotes {
                    nesting += 1;
                }
            }
            JsonEventType::RightBracket(_) => {
                if !inside_quotes {
                    nesting -= 1;
                }
            }
            JsonEventType::Comma => {
                if !inside_quotes && nesting == 0 {
                    split_indices.push(event.position);
                }
            }
            JsonEventType::DoubleQuote => {
                inside_quotes = !inside_quotes;
            }
        }
    }
    let mut top_level_strs: Vec<&str> = Vec::new();
    let mut remaining_str = value;
    let mut index_offset = 0;
    for split_index in split_indices.iter() {
        let (left, right) = remaining_str.split_at(split_index - index_offset);
        top_level_strs.push(left);
        if right.is_empty() {
            remaining_str = "";
        } else {
            remaining_str = &right[1..];
        }
        index_offset = split_index + 1;
    }
    if !remaining_str.is_empty() {
        top_level_strs.push(remaining_str);
    }
    top_level_strs
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
