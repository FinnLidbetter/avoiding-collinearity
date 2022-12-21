use crate::readers::{
    parse_args_from_strings, CollinearReader, CollinearReaderError, CountCollinearArgs,
};
use crate::Config;
use std::env;
use std::fmt::{Display, Formatter};
use std::num::ParseIntError;

pub struct ArgsReader {
    is_args_used: bool,
}
impl ArgsReader {
    pub fn new(config: &Config) -> Result<Box<Self>, CollinearReaderError> {
        Ok(Box::new(ArgsReader {
            is_args_used: false,
        }))
    }
}

impl Display for ArgsReader {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "ArgsReader")
    }
}

impl CollinearReader for ArgsReader {
    /// Parse the arguments.
    ///
    /// Get the sequence length, start index, end index.
    fn read_count_collinear_args(
        &mut self,
    ) -> Result<Option<CountCollinearArgs>, CollinearReaderError> {
        let mut is_first = true;
        let args: Vec<String> = env::args()
            .filter(|_val| {
                if is_first {
                    is_first = false;
                    return false;
                }
                true
            })
            .collect();
        self.is_args_used = true;
        parse_args_from_strings(args)
    }

    fn is_finished_reading(&self) -> bool {
        self.is_args_used
    }

    fn stop_reading(&self) -> () {
        ()
    }
}
