use crate::writers::CollinearWriterError;
use crate::{CollinearWriter, Config, CountCollinearResult};

pub struct StdOutWriter;

impl CollinearWriter for StdOutWriter {
    fn new(config: &Config) -> Result<Box<Self>, CollinearWriterError> {
        Ok(Box::new(StdOutWriter {}))
    }

    fn write_count_collinear_result(
        &self,
        count_collinear_result: CountCollinearResult,
    ) -> Result<(), CollinearWriterError> {
        print!("{}\n", count_collinear_result.to_string());
        Ok(())
    }
}
