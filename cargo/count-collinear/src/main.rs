mod aws_request;
mod aws_signing;
mod compute;
mod dynamo_db;
mod readers;
mod settings;
mod sqs;
mod utilities;
mod writers;

use crate::compute::Point3D;
use crate::readers::args_reader::ArgsReader;
use crate::readers::stdin_reader::StdInReader;
use crate::readers::{CollinearReader, CollinearReaderError, CountCollinearArgs};
use crate::settings::{Config, Destination, Source};
use crate::writers::email_writer::EmailController;
use crate::writers::stdout_writer::StdOutWriter;
use crate::writers::{CollinearCountResult, CollinearWriter};
use crate::Source::Args;
use chrono::{Duration, Utc};
use compute::{build_point_sequence, count_collinear_points};
use log::{debug, error, info};
use std::collections::HashMap;
use std::error;
use std::fmt::Formatter;
use std::time::Instant;
use std::{env, fmt};

fn get_reader(config: &Config) -> impl CollinearReader {
    match config.input_source {
        //Source::Args => *ArgsReader::new(config).unwrap(),
        Source::StdIn => *StdInReader::new(config).unwrap(),
        _ => *StdInReader::new(config).unwrap(),
    }
}

fn get_writer(config: &Config) -> impl CollinearWriter {
    match config.output_destination {
        //Destination::DynamoDb => {}
        // TODO: add error handling for failed initialisation, instead of unwrap.
        //Destination::Email => *EmailController::new(config).unwrap(),
        Destination::StdOut => *StdOutWriter::new(config).unwrap(),
        _ => *StdOutWriter::new(config).unwrap(),
    }
}

fn main() {
    let config = settings::read_config();

    let log_level_var = format!("count_collinear={}", &config.log_level);
    env::set_var("RUST_LOG", log_level_var);
    env_logger::init();

    let mut point_sequence: Vec<Point3D> = Vec::new();

    let mut reader = get_reader(&config);
    let writer = get_writer(&config);
    while !reader.is_finished_reading() {
        let count_collinear_args = reader.read_count_collinear_args();
        match count_collinear_args {
            Ok(count_collinear_args) => {
                let sequence_length = count_collinear_args.sequence_length;
                let start_index = count_collinear_args.start_index;
                let end_index = count_collinear_args.end_index;
                let build_sequence_start_time = Instant::now();
                let mut build_sequence_end_time = build_sequence_start_time;
                if sequence_length > point_sequence.len().try_into().unwrap() {
                    point_sequence = build_point_sequence(sequence_length);
                    build_sequence_end_time = Instant::now();
                }
                let build_duration = build_sequence_end_time - build_sequence_start_time;
                let count_start_time = Instant::now();
                let max_count = count_collinear_points(&point_sequence, start_index, end_index);
                let count_end_time = Instant::now();
                let count_duration = count_end_time - count_start_time;
                let count_collinear_result = CollinearCountResult {
                    sequence_length,
                    start_index,
                    end_index,
                    max_count,
                    build_duration,
                    count_duration,
                };
                match writer
                    .write_count_collinear_result(count_collinear_result)
                    .err()
                {
                    Some(err) => error!("{}", err),
                    None => (),
                }
            }
            Err(reader_error) => {
                error!("{}", reader_error)
            }
        }
    }
    reader.stop_reading();
}
