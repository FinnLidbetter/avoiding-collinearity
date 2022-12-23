pub mod dynamo_db_writer;
pub mod email_writer;
pub mod stdout_writer;

use std::fmt;
use std::fmt::{Display, Formatter};
use std::time::Duration;

#[derive(Debug, Clone)]
pub struct CollinearWriterError {
    msg: String,
}

impl fmt::Display for CollinearWriterError {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        write!(f, "WriterError: {}", self.msg)
    }
}

pub struct CountCollinearResult {
    pub(crate) sequence_length: u32,
    pub(crate) start_index: usize,
    pub(crate) end_index: usize,
    pub(crate) count_max: i32,
    pub(crate) build_duration: Duration,
    pub(crate) count_duration: Duration,
}
impl CountCollinearResult {
    fn build_duration(&self) -> Duration {
        self.build_duration
    }
    pub fn build_duration_seconds(&self) -> f32 {
        self.build_duration().as_secs_f32()
    }
    fn count_duration(&self) -> Duration {
        self.count_duration
    }
    pub fn count_duration_seconds(&self) -> f32 {
        self.count_duration().as_secs_f32()
    }
    pub fn verbose_string(&self) -> String {
        format!(
            "Considering all lines with at least one point with an index in [{}, {}], \
        the largest number of collinear points in the first {} indices of \
        the sequence is {}. This took {} seconds to build the sequence and {} seconds \
        to count the collinearity.",
            self.start_index,
            self.end_index,
            self.sequence_length,
            self.count_max,
            self.build_duration_seconds(),
            self.count_duration_seconds(),
        )
    }
}

impl fmt::Display for CountCollinearResult {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        write!(
            f,
            "Sequence length: {}, start index: {}, end index: {}, max collinear count: {}, \
            sequence build seconds: {}, collinear count seconds: {}",
            self.sequence_length,
            self.start_index,
            self.end_index,
            self.count_max,
            self.build_duration_seconds(),
            self.count_duration_seconds(),
        )
    }
}

pub trait CollinearWriter
where
    Self: Display,
{
    fn write_count_collinear_result(
        &mut self,
        count_collinear_result: CountCollinearResult,
    ) -> Result<(), CollinearWriterError>;
}
