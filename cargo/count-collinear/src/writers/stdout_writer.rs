use crate::settings::Config;
use crate::writers::{CollinearWriter, CollinearWriterError, CountCollinearResult};
use std::fmt;
use std::fmt::Formatter;

pub struct StdOutWriter;

impl StdOutWriter {
    pub fn new(_config: &Config) -> Result<StdOutWriter, CollinearWriterError> {
        Ok(StdOutWriter {})
    }
}

impl fmt::Display for StdOutWriter {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        write!(f, "StdOutWriter")
    }
}

impl CollinearWriter for StdOutWriter {
    fn write_count_collinear_result(
        &mut self,
        count_collinear_result: CountCollinearResult,
    ) -> Result<(), CollinearWriterError> {
        println!("{}", count_collinear_result);
        Ok(())
    }
}
