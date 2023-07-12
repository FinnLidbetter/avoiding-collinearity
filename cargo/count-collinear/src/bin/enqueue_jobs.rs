extern crate count_collinear;

use log::{error, info};
use std::cmp::{max, min};
use std::collections::BTreeMap;
use std::env;
use std::str::FromStr;

use count_collinear::dynamo_db::{AttributeValue, DynamoDbController};
use count_collinear::readers::sqs_reader::QUEUE_NAME;
use count_collinear::readers::CountCollinearArgs;
use count_collinear::settings;
use count_collinear::sqs::SqsController;
use count_collinear::writers::dynamo_db_writer::TABLE_NAME;

const JOBS_MAX: u32 = 10000;
const HELP: &str = "\
Usage:
enqueue_jobs sequence-length job-size [--start-index N] [--end-index M] [--dry-run] [--query-db]
       sequence-length (int): The length of the sequence for which to queue jobs.
              job-size (int): The maximum number of indices to include in each job.
--start-index (optional int): The start index for the jobs. Defaults to 0.
    --end-index (option int): The end index for the jobs. Defaults to the sequence-length.
  --window-size (option int): The size of the window to use for checking collinearity
                              in each job. Defaults to the sequence-length.
   --dry-run (optional bool): If set then only print the actions that would be
                              taken. If this is used in conjunction with --query-db,
                              then a real database query will occur, but jobs will
                              not be enqueued.
  --query-db (optional bool): If set then query the database to determine which jobs
                              are already complete and do not need to be enqueued.
";

struct EnqueueJobsArgs {
    sequence_length: u32,
    job_size_max: u32,
    start_index: usize,
    end_index: usize,
    window_size: usize,
    dry_run: bool,
    query_db: bool,
}

fn parse_args(args: Vec<String>) -> EnqueueJobsArgs {
    let mut dry_run = false;
    let mut query_db = false;
    let mut sequence_length: Option<u32> = None;
    let mut job_size_max: Option<u32> = None;
    let mut start_index: Option<usize> = None;
    let mut end_index: Option<usize> = None;
    let mut window_size: Option<usize> = None;
    let mut first_arg = true;
    let mut expect_start_index = false;
    let mut expect_end_index = false;
    let mut expect_window_size = false;
    for arg in args {
        if first_arg {
            first_arg = false;
            continue;
        }
        match arg.as_str() {
            "--dry-run" => dry_run = true,
            "--query-db" => query_db = true,
            "--start-index" => expect_start_index = true,
            "--end-index" => expect_end_index = true,
            "--window-size" => expect_window_size = true,
            _ => {
                if expect_start_index {
                    let value = arg.parse::<usize>().expect(HELP);
                    start_index = Some(value);
                    expect_start_index = false;
                    continue;
                }
                if expect_end_index {
                    let value = arg.parse::<usize>().expect(HELP);
                    end_index = Some(value);
                    expect_end_index = false;
                    continue;
                }
                if expect_window_size {
                    let value = arg.parse::<usize>().expect(HELP);
                    window_size = Some(value);
                    expect_window_size = false;
                    continue;
                }
                let value = arg.parse::<u32>().expect(HELP);
                if sequence_length.is_none() {
                    sequence_length = Some(value);
                } else if job_size_max.is_none() {
                    job_size_max = Some(value);
                } else {
                    panic!("{}", HELP);
                }
            }
        }
    }
    EnqueueJobsArgs {
        sequence_length: sequence_length.expect(HELP),
        job_size_max: job_size_max.expect(HELP),
        start_index: start_index.unwrap_or(0),
        end_index: end_index.unwrap_or_else(|| sequence_length.unwrap().try_into().unwrap()),
        window_size: window_size.unwrap_or_else(|| sequence_length.unwrap().try_into().unwrap()),
        dry_run,
        query_db,
    }
}

#[derive(Debug)]
struct IndexParsingErr;

fn parse_indices(
    item: &BTreeMap<String, AttributeValue>,
) -> Result<(usize, usize), IndexParsingErr> {
    let start_index =
        parse_number_field::<usize>(item, "start_index").map_err(|_| IndexParsingErr)?;
    let end_index = parse_number_field::<usize>(item, "end_index").map_err(|_| IndexParsingErr)?;
    Ok((start_index, end_index))
}

fn parse_number_field<T: FromStr>(
    item: &BTreeMap<String, AttributeValue>,
    field_name: &str,
) -> Result<T, IndexParsingErr> {
    if let Some(value) = item.get(field_name) {
        return match value {
            AttributeValue::Number(value_str) => {
                value_str.parse::<T>().map_err(|_| IndexParsingErr)
            }
            _ => Err(IndexParsingErr),
        };
    }
    Err(IndexParsingErr)
}

fn merge_intervals(
    interval_1: (usize, usize),
    interval_2: (usize, usize),
) -> Option<(usize, usize)> {
    let left_1 = interval_1.0;
    let right_1 = interval_1.1;
    let left_2 = interval_2.0;
    let right_2 = interval_2.1;
    if left_1 <= left_2 && left_2 <= right_1 {
        return Some((left_1, max(right_1, right_2)));
    }
    if left_2 <= left_1 && left_1 <= right_2 {
        return Some((left_2, max(right_1, right_2)));
    }
    None
}

fn split_jobs(
    start_index: usize,
    end_index: usize,
    window_size: usize,
    sequence_length: u32,
    job_size_max: u32,
) -> Vec<CountCollinearArgs> {
    let start_index = max(0, start_index);
    let end_index = min(end_index, sequence_length as usize);
    let mut jobs: Vec<CountCollinearArgs> = Vec::new();
    let mut curr_start = start_index;
    let mut curr_end = curr_start + job_size_max as usize;
    while curr_start < end_index {
        if curr_end > end_index {
            curr_end = end_index;
        }
        jobs.push(CountCollinearArgs {
            sequence_length,
            start_index: curr_start,
            end_index: curr_end,
            window_size,
        });
        curr_start = curr_end;
        curr_end = curr_start + job_size_max as usize;
    }
    jobs
}

fn main() {
    let config = settings::read_config();

    let log_level_var = format!("enqueue_jobs={}", &config.log_level);
    env::set_var("RUST_LOG", log_level_var);
    env_logger::init();

    let aws_auth_settings = config
        .aws_auth_settings
        .expect("AWS auth settings must be configured in order to enqueue jobs.");
    let sqs_controller = SqsController::new(
        aws_auth_settings.access_key.as_str(),
        aws_auth_settings.secret_key.as_str(),
        aws_auth_settings.account_number.as_str(),
        aws_auth_settings.region.as_str(),
    );
    let dynamo_db_controller = DynamoDbController::new(
        aws_auth_settings.access_key.as_str(),
        aws_auth_settings.secret_key.as_str(),
        aws_auth_settings.region.as_str(),
    );
    let args: Vec<String> = env::args().collect();
    let parsed_args = parse_args(args);
    let sequence_length = parsed_args.sequence_length;
    let job_size_max = parsed_args.job_size_max;
    let window_size = parsed_args.window_size;
    if parsed_args.sequence_length / parsed_args.job_size_max > JOBS_MAX {
        panic!(
            "Too many jobs! Choose a larger job size. \
            The maximum number of allowed jobs is {}",
            JOBS_MAX
        );
    }
    let mut jobs: Vec<CountCollinearArgs> = Vec::new();
    if parsed_args.query_db {
        let partiql_statement = format!(
            "SELECT sequence_length, start_index, end_index, window_size FROM {} WHERE sequence_length=? AND window_size=?;",
            TABLE_NAME
        );
        let parameters = vec![
            AttributeValue::Number(parsed_args.sequence_length.to_string()),
            AttributeValue::Number(parsed_args.window_size.to_string()),
        ];
        let result =
            dynamo_db_controller.execute_statement(partiql_statement.as_str(), parameters, None);
        if let Err(err) = result {
            panic!("Database query failed: {}", err);
        }
        let unwrapped_items = result.unwrap().items;
        let mut intervals: Vec<(usize, usize)> = unwrapped_items
            .iter()
            .map(parse_indices)
            .filter(|result_pair| result_pair.is_ok())
            .map(|result_pair| result_pair.unwrap())
            .collect();
        intervals.sort_unstable();
        let mut merged_intervals: Vec<(usize, usize)> = Vec::new();
        if !intervals.is_empty() {
            let mut curr = intervals[0];
            for next in intervals.iter().skip(1) {
                let next = *next;
                let merge_result = merge_intervals(curr, next);
                match merge_result {
                    Some(merged) => {
                        curr = merged;
                    }
                    None => {
                        merged_intervals.push(curr);
                        curr = next;
                    }
                }
            }
            merged_intervals.push(curr);
        }
        let mut prev_end = parsed_args.start_index;
        for interval in merged_intervals {
            if prev_end < interval.0 {
                jobs.extend(split_jobs(
                    prev_end,
                    interval.0,
                    window_size,
                    sequence_length,
                    job_size_max,
                ));
            }
            prev_end = interval.1;
        }
        if prev_end < parsed_args.end_index {
            jobs.extend(split_jobs(
                prev_end,
                parsed_args.end_index,
                window_size,
                sequence_length,
                job_size_max,
            ))
        }
    } else {
        jobs.extend(split_jobs(
            parsed_args.start_index,
            parsed_args.end_index,
            window_size,
            sequence_length,
            job_size_max,
        ));
    }
    for job in jobs {
        if parsed_args.dry_run {
            info!("(Dry run) Enqueueing '{}'", job.as_json());
        } else {
            info!("Enqueueing '{}'", job.as_json());
            let result = sqs_controller.send_message(QUEUE_NAME, job.as_json().as_str());
            if result.is_err() {
                error!(
                    "Failed to enqueue message '{}' due to {}",
                    job.as_json(),
                    result.unwrap_err()
                );
            }
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_merge_intervals() {
        // Test overlap at one endpoint.
        assert_eq!(merge_intervals((5, 10), (10, 14)), Some((5, 14)));
        // Test other order overlap at one endpoint.
        assert_eq!(merge_intervals((10, 14), (5, 10)), Some((5, 14)));
        // Test non-overlap.
        assert_eq!(merge_intervals((5, 10), (11, 14)), None);
        // Test containment.
        assert_eq!(merge_intervals((4, 6), (4, 8)), Some((4, 8)));
        assert_eq!(merge_intervals((4, 6), (4, 6)), Some((4, 6)));
        assert_eq!(merge_intervals((4, 6), (2, 6)), Some((2, 6)));
        assert_eq!(merge_intervals((4, 6), (1, 20)), Some((1, 20)));
        // Test other order containment.
        assert_eq!(merge_intervals((4, 8), (4, 6)), Some((4, 8)));
        assert_eq!(merge_intervals((2, 6), (4, 6)), Some((2, 6)));
        assert_eq!(merge_intervals((1, 20), (4, 6)), Some((1, 20)));
    }

    #[test]
    fn test_split_jobs_multiple_exact() {
        let jobs = split_jobs(0, 100, 100, 100, 20);
        let expected_jobs = vec![
            CountCollinearArgs {
                sequence_length: 100,
                start_index: 0,
                end_index: 20,
                window_size: 100,
            },
            CountCollinearArgs {
                sequence_length: 100,
                start_index: 20,
                end_index: 40,
                window_size: 100,
            },
            CountCollinearArgs {
                sequence_length: 100,
                start_index: 40,
                end_index: 60,
                window_size: 100,
            },
            CountCollinearArgs {
                sequence_length: 100,
                start_index: 60,
                end_index: 80,
                window_size: 100,
            },
            CountCollinearArgs {
                sequence_length: 100,
                start_index: 80,
                end_index: 100,
                window_size: 100,
            },
        ];
        assert_eq!(jobs, expected_jobs)
    }

    #[test]
    fn test_split_jobs_one_job_smaller_than_max() {
        let jobs = split_jobs(10, 70, 1000, 1000, 100);
        let expected_jobs = vec![CountCollinearArgs {
            sequence_length: 1000,
            start_index: 10,
            end_index: 70,
            window_size: 1000,
        }];
        assert_eq!(jobs, expected_jobs);
    }

    #[test]
    fn test_split_jobs_one_job_exact() {
        let jobs = split_jobs(10, 70, 1000, 1000, 60);
        let expected_jobs = vec![CountCollinearArgs {
            sequence_length: 1000,
            start_index: 10,
            end_index: 70,
            window_size: 1000,
        }];
        assert_eq!(jobs, expected_jobs);
    }

    #[test]
    fn test_split_jobs_one_max_one_partial() {
        let jobs = split_jobs(10, 70, 1000, 1000, 59);
        let expected_jobs = vec![
            CountCollinearArgs {
                sequence_length: 1000,
                start_index: 10,
                end_index: 69,
                window_size: 1000,
            },
            CountCollinearArgs {
                sequence_length: 1000,
                start_index: 69,
                end_index: 70,
                window_size: 1000,
            },
        ];
        assert_eq!(jobs, expected_jobs);
    }

    #[test]
    fn test_split_jobs_trivial() {
        let jobs = split_jobs(10, 10, 1000, 1000, 100);
        let expected_jobs = vec![];
        assert_eq!(jobs, expected_jobs);
    }
}
