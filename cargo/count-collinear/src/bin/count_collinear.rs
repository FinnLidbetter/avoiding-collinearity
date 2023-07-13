extern crate count_collinear;

use count_collinear::compute::Point3D;
use count_collinear::readers::args_reader::ArgsReader;
use count_collinear::readers::sqs_reader::SqsReader;
use count_collinear::readers::stdin_reader::StdInReader;
use count_collinear::readers::CollinearReader;
use count_collinear::runner::process_count_collinear_args;
use count_collinear::settings;
use count_collinear::settings::{Config, Destination, Source};
use count_collinear::writers::dynamo_db_writer::DynamoDbWriter;
use count_collinear::writers::email_writer::EmailWriter;
use count_collinear::writers::stdout_writer::StdOutWriter;
use count_collinear::writers::CollinearWriter;
use log::{debug, error, info};
use std::env;

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
                        match writer
                            .write_count_collinear_result(count_collinear_result)
                            .err()
                        {
                            Some(err) => error!("{}", err),
                            None => {
                                if let Result::Err(err) = reader.post_process_args_read() {
                                    error!("{}", err);
                                }
                            }
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
