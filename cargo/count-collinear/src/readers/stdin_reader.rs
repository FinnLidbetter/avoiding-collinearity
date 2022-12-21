use crate::readers::parse_args_from_strings;
use crate::{CollinearReader, CollinearReaderError, Config, CountCollinearArgs};
use std::io;

pub struct StdInReader {
    has_read_blank_line: bool,
}

impl StdInReader {
    pub fn new(config: &Config) -> Result<Box<Self>, CollinearReaderError> {
        Ok(Box::new(StdInReader {
            has_read_blank_line: false,
        }))
    }
}

impl CollinearReader for StdInReader {
    fn read_count_collinear_args(&mut self) -> Result<CountCollinearArgs, CollinearReaderError> {
        let mut line = String::new();
        io::stdin().read_line(&mut line);
        if line.trim().is_empty() {
            self.has_read_blank_line = true;
            // TODO: change this such that end of input is not an error. Maybe
            //  the return type should be Result<Option<CountCollinearArgs>, CollinearReaderError>
            return Err(CollinearReaderError {
                msg: String::from("End of input reached."),
            });
        }
        let tokens: Vec<String> = line
            .split_whitespace()
            .map(|value| String::from(value))
            .collect();
        parse_args_from_strings(tokens)
    }

    fn is_finished_reading(&self) -> bool {
        self.has_read_blank_line
    }

    fn stop_reading(&self) -> () {
        ()
    }
}
