use std::time::Instant;
use crate::compute::{build_point_sequence, count_collinear_points, Point3D};
use crate::readers::CountCollinearArgs;
use crate::writers::CountCollinearResult;

pub fn process_count_collinear_args(
    point_sequence: &mut Vec<Point3D>,
    count_collinear_args: CountCollinearArgs,
) -> CountCollinearResult {
    let sequence_length = count_collinear_args.sequence_length;
    let start_index = count_collinear_args.start_index;
    let end_index = count_collinear_args.end_index;
    let window_size = count_collinear_args.window_size;
    let build_sequence_start_time = Instant::now();
    let mut build_sequence_end_time = build_sequence_start_time;
    if sequence_length > point_sequence.len().try_into().unwrap() {
        *point_sequence = build_point_sequence(sequence_length);
        build_sequence_end_time = Instant::now();
    }
    let build_duration = build_sequence_end_time - build_sequence_start_time;
    let count_start_time = Instant::now();
    let count_max = count_collinear_points(point_sequence, start_index, end_index, window_size);
    let count_end_time = Instant::now();
    let count_duration = count_end_time - count_start_time;
    CountCollinearResult {
        sequence_length,
        window_size,
        start_index,
        end_index,
        count_max,
        build_duration,
        count_duration,
    }
}