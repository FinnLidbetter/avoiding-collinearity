use crate::writers::CollinearWriterError;
use crate::{CollinearWriter, Config, CountCollinearResult};
use std::fmt;
use std::fmt::Formatter;

pub struct StdOutWriter;

impl StdOutWriter {
    pub fn new(config: &Config) -> Result<StdOutWriter, CollinearWriterError> {
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
        &self,
        count_collinear_result: CountCollinearResult,
    ) -> Result<(), CollinearWriterError> {
        print!("{}\n", count_collinear_result.to_string());
        Ok(())
    }
}
