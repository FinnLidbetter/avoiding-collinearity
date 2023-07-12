use crate::readers::{
    parse_args_from_strings, CollinearReader, CollinearReaderError, CountCollinearArgs,
};
use crate::settings::Config;
use std::env;
use std::fmt::{Display, Formatter};

pub struct ArgsReader {
    is_args_used: bool,
}
impl ArgsReader {
    pub fn new(_config: &Config) -> Result<ArgsReader, CollinearReaderError> {
        Ok(ArgsReader {
            is_args_used: false,
        })
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
    /// Get the sequence length, start index, end index, and window size.
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

    fn post_process_args_read(&self) -> Result<(), CollinearReaderError> {
        Ok(())
    }

    fn is_finished_reading(&self) -> bool {
        self.is_args_used
    }

    fn stop_reading(&self) {}
}
