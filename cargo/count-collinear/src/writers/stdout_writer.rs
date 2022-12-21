use crate::writers::CollinearWriterError;
use crate::{CollinearWriter, Config, CountCollinearResult};

pub struct StdOutWriter;

impl StdOutWriter {
    pub fn new(config: &Config) -> Result<StdOutWriter, CollinearWriterError> {
        Ok(StdOutWriter {})
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
