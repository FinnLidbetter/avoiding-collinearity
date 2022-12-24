mod aws_request;
mod aws_signing;
mod compute;
mod dynamo_db;
mod ec2;
mod email;
mod readers;
mod settings;
mod sqs;
mod utilities;
mod writers;

use crate::compute::Point3D;
use crate::readers::args_reader::ArgsReader;
use crate::readers::sqs_reader::SqsReader;
use crate::readers::stdin_reader::StdInReader;
use crate::readers::{CollinearReader, CollinearReaderError, CountCollinearArgs};
use crate::settings::{Config, Destination, Source};
use crate::writers::dynamo_db_writer::DynamoDbWriter;
use crate::writers::email_writer::EmailWriter;
use crate::writers::stdout_writer::StdOutWriter;
use crate::writers::{CollinearWriter, CountCollinearResult};
use compute::{build_point_sequence, count_collinear_points};
use log::{debug, error, info};
use std::env;
use std::time::Instant;

fn get_reader(config: &Config) -> Box<dyn CollinearReader> {
    match config.input_source {
        Source::Args => Box::new(ArgsReader::new(config).unwrap()),
        Source::Sqs => Box::new(SqsReader::new(config).unwrap()),
        Source::StdIn => Box::new(StdInReader::new(config).unwrap()),
    }
}

fn get_writer(config: &Config) -> Box<dyn CollinearWriter> {
    match config.output_destination {
        Destination::DynamoDb => Box::new(DynamoDbWriter::new(config).unwrap()),
        Destination::Email => Box::new(EmailWriter::new(config).unwrap()),
        Destination::StdOut => Box::new(StdOutWriter::new(config).unwrap()),
    }
}

fn process_count_collinear_args(
    point_sequence: &mut Vec<Point3D>,
    count_collinear_args: CountCollinearArgs,
) -> CountCollinearResult {
    let sequence_length = count_collinear_args.sequence_length;
    let start_index = count_collinear_args.start_index;
    let end_index = count_collinear_args.end_index;
    let build_sequence_start_time = Instant::now();
    let mut build_sequence_end_time = build_sequence_start_time;
    if sequence_length > point_sequence.len().try_into().unwrap() {
        *point_sequence = build_point_sequence(sequence_length);
        build_sequence_end_time = Instant::now();
    }
    let build_duration = build_sequence_end_time - build_sequence_start_time;
    let count_start_time = Instant::now();
    let count_max = count_collinear_points(&point_sequence, start_index, end_index);
    let count_end_time = Instant::now();
    let count_duration = count_end_time - count_start_time;
    CountCollinearResult {
        sequence_length,
        start_index,
        end_index,
        count_max,
        build_duration,
        count_duration,
    }
}

fn main() {
    let config = settings::read_config();

    let log_level_var = format!("count_collinear={}", &config.log_level);
    env::set_var("RUST_LOG", log_level_var);
    env_logger::init();

    let mut point_sequence: Vec<Point3D> = Vec::new();

    let mut reader = get_reader(&config);
    debug!("Using reader: {}", reader);
    let mut writer = get_writer(&config);
    debug!("Using writer: {}", writer);
    while !reader.is_finished_reading() {
        let count_collinear_args = reader.read_count_collinear_args();
        match count_collinear_args {
            Ok(count_collinear_args) => {
                match count_collinear_args {
                    Some(count_collinear_args) => {
                        info!("Processing: {}", count_collinear_args);
                        let count_collinear_result =
                            process_count_collinear_args(&mut point_sequence, count_collinear_args);
                        info!("{}", count_collinear_result);
                        if let Result::Err(err) = reader.post_process_args_read() {
                            error!("{}", err);
                        }
                        match writer
                            .write_count_collinear_result(count_collinear_result)
                            .err()
                        {
                            Some(err) => error!("{}", err),
                            None => (),
                        }
                    }
                    None => continue,
                };
            }
            Err(reader_error) => {
                error!("{}", reader_error)
            }
        }
    }
    reader.stop_reading();
}
