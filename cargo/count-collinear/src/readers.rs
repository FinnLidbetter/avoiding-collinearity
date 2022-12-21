pub mod args_reader;
pub mod sqs_reader;
pub mod stdin_reader;

use crate::settings::Config;
use chrono::{Duration, Utc};
use std::fmt;
use std::fmt::{Display, Formatter};
use std::num::ParseIntError;
use std::str::FromStr;

#[derive(Debug, Clone)]
pub struct CollinearReaderError {
    msg: String,
}

impl fmt::Display for CollinearReaderError {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        write!(f, "ReaderError: {}", self.msg)
    }
}

pub fn parse_args_from_strings(
    values: Vec<String>,
) -> Result<Option<CountCollinearArgs>, CollinearReaderError> {
    let sequence_length = values.get(0).ok_or(CollinearReaderError {
        msg: String::from("Supply at least one argument, the sequence length"),
    })?;
    let sequence_length: u32 =
        sequence_length
            .to_string()
            .trim()
            .parse()
            .map_err(|err: ParseIntError| CollinearReaderError {
                msg: format!(
                    "The first argument should be a positive integer for a sequence length. {}",
                    err.to_string()
                ),
            })?;
    let start_index: usize = match values.get(1) {
        Some(element) => element
            .to_string()
            .trim()
            .parse()
            .map_err(|err: ParseIntError| CollinearReaderError {
                msg: format!(
                    "The second argument should be a positive integer for the start index. {}",
                    err.to_string()
                ),
            })?,
        None => 0,
    };
    let mut end_index: usize = match values.get(2) {
        Some(element) => element
            .to_string()
            .trim()
            .parse()
            .map_err(|err: ParseIntError| CollinearReaderError {
                msg: format!(
                    "The third argument should be a positive integer for the end index. {}",
                    err.to_string()
                ),
            })?,
        None => sequence_length.try_into().unwrap(),
    };
    if end_index > sequence_length.try_into().unwrap() {
        end_index = sequence_length.try_into().unwrap();
    }
    Ok(Some(CountCollinearArgs {
        sequence_length,
        start_index,
        end_index,
    }))
}

pub struct CountCollinearArgs {
    pub(crate) sequence_length: u32,
    pub(crate) start_index: usize,
    pub(crate) end_index: usize,
}

/// Parse the digits that come after a particular search pattern.
///
/// This assumes that base_str is ascii and that only the digits
/// should be parsed. As such, this only works for nonnegative integers.
/// Parsing will stop when a comma ',' is reached.
fn parse_digits_after_pattern<T: FromStr>(
    text: &str,
    pattern: &str,
) -> Result<T, ParseCountCollinearArgsErr> {
    let pattern_start = text.find(pattern);
    let text_chars: Vec<char> = text.chars().collect();
    match pattern_start {
        Some(sequence_length_start) => {
            let mut sequence_length_digits = String::new();
            let mut char_index = sequence_length_start;
            while text_chars[char_index] as char != ',' {
                if text_chars[char_index].is_digit(10) {
                    sequence_length_digits.push(text_chars[char_index]);
                }
                char_index += 1;
            }
            Ok(sequence_length_digits
                .parse::<T>()
                .map_err(|_| ParseCountCollinearArgsErr {
                    msg: format!("Could not parse {}", sequence_length_digits.as_str()),
                })?)
        }
        None => {
            return Err(ParseCountCollinearArgsErr {
                msg: format!(
                    "{} not found in {} when trying to parse CountCollinearArgs.",
                    pattern, text
                ),
            })
        }
    }
}

#[derive(Debug, PartialEq, Eq)]
pub struct ParseCountCollinearArgsErr {
    msg: String,
}

impl fmt::Display for ParseCountCollinearArgsErr {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        write!(f, "{}", self.msg)
    }
}

impl FromStr for CountCollinearArgs {
    type Err = ParseCountCollinearArgsErr;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        let trimmed = s.trim();
        if !trimmed.is_ascii() {
            return Err(ParseCountCollinearArgsErr {
                msg: String::from("Non-ascii characters when trying to parse CountCollinearArgs."),
            });
        }
        let sequence_length = parse_digits_after_pattern::<u32>(trimmed, "sequence_length")?;
        let start_index = parse_digits_after_pattern::<usize>(trimmed, "start_index")?;
        let end_index = parse_digits_after_pattern::<usize>(trimmed, "end_index")?;
        Ok(CountCollinearArgs {
            sequence_length,
            start_index,
            end_index,
        })
    }
}

pub trait CollinearReader
where
    Self: Display,
{
    fn read_count_collinear_args(
        &mut self,
    ) -> Result<Option<CountCollinearArgs>, CollinearReaderError>;

    fn is_finished_reading(&self) -> bool;

    fn stop_reading(&self) -> ();
}