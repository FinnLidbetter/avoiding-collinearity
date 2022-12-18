extern crate core;

mod aws_request;
mod aws_signing;
mod compute;
mod dynamo_db;
mod email;
mod settings;
mod sqs;
mod utilities;

use chrono::{Duration, Utc};
use compute::{build_point_sequence, count_collinear_points};
use email::send_result;
use log::{debug, error, info};
use std::collections::HashMap;
use std::error;
use std::fmt::Formatter;
use std::{env, fmt};

/// Parse the arguments.
///
/// Get the sequence length, start index, end index, and whether to notify
/// results via email.
fn parse_args(mut args: Vec<String>) -> (u32, usize, usize, bool) {
    let notify = match args.iter().position(|x| String::from("--notify").eq(x)) {
        Some(position) => {
            args.remove(position);
            true
        }
        None => false,
    };
    let sequence_length = &args
        .get(1)
        .expect("Supply at least one argument, the sequence length.");
    let sequence_length: u32 = sequence_length
        .to_string()
        .trim()
        .parse()
        .expect("The first argument should be a positive integer for a sequence length.");
    let start_index: usize = match args.get(2) {
        Some(element) => element
            .to_string()
            .trim()
            .parse()
            .expect("The second argument should be a positive integer for the start index."),
        None => 0,
    };
    let mut end_index: usize = match args.get(3) {
        Some(element) => element
            .to_string()
            .trim()
            .parse()
            .expect("The third argument should be a positive integer for the end index."),
        None => sequence_length.try_into().unwrap(),
    };
    if end_index > sequence_length.try_into().unwrap() {
        end_index = sequence_length.try_into().unwrap();
    }
    (sequence_length, start_index, end_index, notify)
}

struct CollinearCountResult {
    sequence_length: u32,
    start_index: usize,
    end_index: usize,
    max_count: i32,
    build_start_time: chrono::DateTime<Utc>,
    build_end_time: chrono::DateTime<Utc>,
    count_start_time: chrono::DateTime<Utc>,
    count_end_time: chrono::DateTime<Utc>,
}
impl CollinearCountResult {
    fn build_duration(&self) -> Duration {
        self.build_end_time - self.build_start_time
    }
    pub fn build_duration_seconds(&self) -> i64 {
        self.build_duration().num_seconds()
    }
    fn count_duration(&self) -> Duration {
        self.count_end_time - self.count_start_time
    }
    pub fn count_duration_seconds(&self) -> i64 {
        self.count_duration().num_seconds()
    }
    pub fn verbose_string(&self) -> String {
        format!(
            "Considering all lines with at least one point with an index in [{}, {}], \
        the largest number of collinear points in the first {} indices of \
        the sequence is {}. This took {} seconds to build the sequence and {} to count \
        the collinearity.",
            self.start_index,
            self.end_index,
            self.sequence_length,
            self.max_count,
            self.build_duration_seconds(),
            self.count_duration_seconds(),
        )
    }
}

impl fmt::Display for CollinearCountResult {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        write!(
            f,
            "Sequence length: {}, start index: {}, end index: {}, max collinear count: {}, \
            sequence build seconds: {}, collinear count seconds: {}",
            self.sequence_length,
            self.start_index,
            self.end_index,
            self.max_count,
            self.build_duration_seconds(),
            self.count_duration_seconds(),
        )
    }
}

fn main() {
    let config = settings::read_config();
    let log_level_var = format!("count_collinear={}", &config.log_level);
    env::set_var("RUST_LOG", log_level_var);

    env_logger::init();
    let args: Vec<String> = env::args().collect();
    let (sequence_length, start_index, end_index, notify) = parse_args(args);

    let point_sequence = build_point_sequence(sequence_length);
    let max_count = count_collinear_points(point_sequence, start_index, end_index);
    info!(
        "Considering all lines with at least one point with an index in [{}, {}], \
        the largest number of collinear points in the first {} indices of \
        the sequence is {}.",
        start_index, end_index, sequence_length, max_count
    );
    if notify {
        match send_result(sequence_length, start_index, end_index, max_count, &config) {
            Ok(_) => (),
            Err(err) => {
                error!("Error sending the email {}", err);
                ()
            }
        }
    }
}
