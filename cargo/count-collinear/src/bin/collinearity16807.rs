extern crate count_collinear;

use count_collinear::compute::Point3D;
use count_collinear::readers::CountCollinearArgs;
use count_collinear::runner::process_count_collinear_args;
use count_collinear::settings;
use log::info;
use std::env;
use std::time::Instant;

fn main() {
    let config = settings::read_config();

    let log_level_var = format!("collinearity16807={}", &config.log_level);
    env::set_var("RUST_LOG", log_level_var);
    env_logger::init();

    let mut point_sequence: Vec<Point3D> = Vec::new();
    let sequence_length = 10000000;
    let window_size = 16807;
    let intervals = [
        (0, 98440),
        (117650, 216089),
        (470597, 537823),
        (537825, 585843),
        (1294140, 1361366),
        (1361368, 1409386),
        (3529471, 3627910),
        (9294272, 9392711),
    ];
    let start_time = Instant::now();
    for interval in intervals {
        let start_index = interval.0;
        let end_index = interval.1;
        let count_collinear_args = CountCollinearArgs {
            sequence_length,
            start_index,
            end_index,
            window_size,
        };
        info!("Processing: {}", count_collinear_args);
        let result = process_count_collinear_args(&mut point_sequence, count_collinear_args);
        info!("{}", result);
    }
    let end_time = Instant::now();
    let duration = end_time - start_time;
    info!(
        "collinearity16807 completed in {} seconds.",
        duration.as_secs_f32()
    );
}
