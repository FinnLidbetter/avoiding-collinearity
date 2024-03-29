use crate::readers::{
    parse_args_from_strings, CollinearReader, CollinearReaderError, CountCollinearArgs,
};
use crate::settings::Config;
use std::fmt::{Display, Formatter};
use std::io;

pub struct StdInReader {
    has_read_blank_line: bool,
}

impl StdInReader {
    pub fn new(_config: &Config) -> Result<StdInReader, CollinearReaderError> {
        Ok(StdInReader {
            has_read_blank_line: false,
        })
    }
}

impl Display for StdInReader {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "StdInReader")
    }
}

impl CollinearReader for StdInReader {
    fn initialize_reading(&self) {
        println! {"Enter input in format:\n\tsequence_length start_index end_index window_size"};
    }

    fn read_count_collinear_args(
        &mut self,
    ) -> Result<Option<CountCollinearArgs>, CollinearReaderError> {
        let mut line = String::new();
        io::stdin()
            .read_line(&mut line)
            .map_err(|err| CollinearReaderError {
                msg: err.to_string(),
            })?;
        if line.trim().is_empty() {
            self.has_read_blank_line = true;
            return Ok(None);
        }
        let tokens: Vec<String> = line.split_whitespace().map(String::from).collect();
        parse_args_from_strings(tokens)
    }

    fn post_process_args_read(&self) -> Result<(), CollinearReaderError> {
        Ok(())
    }

    fn is_finished_reading(&self) -> bool {
        self.has_read_blank_line
    }

    fn stop_reading(&self) {}
}
