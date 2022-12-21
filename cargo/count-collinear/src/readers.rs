pub mod args_reader;
pub mod stdin_reader;

use crate::settings::Config;
use chrono::{Duration, Utc};
use std::fmt;
use std::fmt::{Display, Formatter};
use std::num::ParseIntError;

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
