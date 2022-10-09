use log::{debug, info};
use std::collections::HashMap;
use std::env;

#[derive(Debug, PartialEq, Eq, Hash)]
struct Point3D {
    x: i128,
    y: i128,
    z: i128,
}

#[derive(Debug, PartialEq, Eq, Hash)]
struct Fraction {
    num: i128,
    denom: i128,
}

impl Fraction {
    pub fn new_normalised(numerator: i128, denominator: i128) -> Self {
        let gcf = gcd(numerator, denominator);
        let normalised_numerator = numerator / gcf;
        let normalised_denominator = denominator / gcf;
        if normalised_denominator < 0 {
            Self {
                num: -normalised_numerator,
                denom: -normalised_denominator,
            }
        } else {
            Self {
                num: normalised_numerator,
                denom: normalised_denominator,
            }
        }
    }
}

#[derive(PartialEq, Eq, Hash)]
struct Line3D {
    point: (Fraction, Fraction, Fraction),
    direction: (Fraction, Fraction, Fraction),
}

impl Line3D {
    /// Get a normalised 3-dimensional line object from two points.
    ///
    /// Normalise the line by getting a normalised direction and normalised point.
    pub fn new_normalised(point_1: &Point3D, point_2: &Point3D) -> Self {
        let mut point_numerators: [i128; 3] = [point_1.x, point_1.y, point_1.z];
        let mut point_denominators: [i128; 3] = [1, 1, 1];
        let mut direction_numerators: [i128; 3] = [
            point_2.x - point_1.x,
            point_2.y - point_1.y,
            point_2.z - point_1.z,
        ];
        let mut direction_denominators: [i128; 3] = [1, 1, 1];

        let mut normalisation_coordinate_index = 0;
        while direction_numerators[normalisation_coordinate_index] == 0 {
            normalisation_coordinate_index += 1;
        }
        let (direction_x, direction_y, direction_z) = Line3D::normalise_direction(
            &mut direction_numerators,
            &mut direction_denominators,
            normalisation_coordinate_index,
        );
        let (point_x, point_y, point_z) = Line3D::normalise_point(
            &mut point_numerators,
            &mut point_denominators,
            &direction_numerators,
            &direction_denominators,
            normalisation_coordinate_index,
        );
        Self {
            point: (point_x, point_y, point_z),
            direction: (direction_x, direction_y, direction_z),
        }
    }

    /// The direction is normalised by making the first nonzero coordinate (first
    /// out of x, y, z) for the direction vector equal to one and representing the
    /// remaining coordinates in reduced fractions of integers.
    fn normalise_direction(
        direction_numerators: &mut [i128; 3],
        direction_denominators: &mut [i128; 3],
        normalisation_coordinate_index: usize,
    ) -> (Fraction, Fraction, Fraction) {
        let normalisation_multiplier = direction_numerators[normalisation_coordinate_index];
        for index in normalisation_coordinate_index..3 {
            direction_denominators[index] *= normalisation_multiplier;
            if direction_denominators[index] < 0 {
                direction_denominators[index] *= -1;
                direction_denominators[index] *= -1;
            }
        }
        for index in 0..3 {
            if direction_numerators[index] == 0 {
                direction_denominators[index] = 1;
                continue;
            }
            let div = gcd(direction_numerators[index], direction_denominators[index]);
            direction_numerators[index] /= div;
            direction_denominators[index] /= div;
        }
        let direction_x = Fraction {
            num: direction_numerators[0],
            denom: direction_denominators[0],
        };
        let direction_y = Fraction {
            num: direction_numerators[1],
            denom: direction_denominators[1],
        };
        let direction_z = Fraction {
            num: direction_numerators[2],
            denom: direction_denominators[2],
        };
        (direction_x, direction_y, direction_z)
    }

    /// The point is normalised by shifting it along the line such that the coordinate
    /// corresponding to the first nonzero coordinate of the direction vector is 0.
    /// The remaining coordinates of the point are represented as reduced fractions
    /// of integers.
    fn normalise_point(
        point_numerators: &mut [i128; 3],
        point_denominators: &mut [i128; 3],
        direction_numerators: &[i128; 3],
        direction_denominators: &[i128; 3],
        normalisation_coordinate_index: usize,
    ) -> (Fraction, Fraction, Fraction) {
        let normalisation_multiplier = point_numerators[normalisation_coordinate_index];
        for index in 0..3 {
            point_numerators[index] *= direction_denominators[index];
            point_denominators[index] *= direction_denominators[index];
            point_numerators[index] -= normalisation_multiplier * direction_numerators[index];
        }
        let point_x = Fraction::new_normalised(point_numerators[0], point_denominators[0]);
        let point_y = Fraction::new_normalised(point_numerators[1], point_denominators[1]);
        let point_z = Fraction::new_normalised(point_numerators[2], point_denominators[2]);
        (point_x, point_y, point_z)
    }
}

/// Gets the greatest common divisor of two integers.
///
/// The result returned will always be a nonnegative integer. If
/// one of the arguments is 0, then the result will be the absolute
/// value of the other argument.
pub fn gcd(a: i128, b: i128) -> i128 {
    if a < 0 {
        gcd(-a, b)
    } else {
        if b == 0 {
            a
        } else {
            gcd(b, a % b)
        }
    }
}

/// Parse the arguments.
///
/// Get the sequence length, start index, and end index.
fn parse_args(args: Vec<String>) -> (u32, usize, usize) {
    let sequence_length = &args[1];
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
    (sequence_length, start_index, end_index)
}

fn build_symbol_sequence(sequence_length: u32) -> Vec<u8> {
    let morphism: [[u8; 7]; 12] = [
        [0, 4, 9, 0, 8, 9, 0],
        [1, 5, 10, 1, 6, 10, 1],
        [2, 3, 11, 2, 7, 11, 2],
        [3, 2, 6, 3, 10, 6, 3],
        [4, 0, 7, 4, 11, 7, 4],
        [5, 1, 8, 5, 9, 8, 5],
        [6, 3, 2, 6, 3, 10, 6],
        [7, 4, 0, 7, 4, 11, 7],
        [8, 5, 1, 8, 5, 9, 8],
        [9, 0, 4, 9, 0, 8, 9],
        [10, 1, 5, 10, 1, 6, 10],
        [11, 2, 3, 11, 2, 7, 11],
    ];
    let mut symbol_sequence: Vec<u8> = Vec::new();
    let mut index = 0;
    while symbol_sequence.len() < sequence_length.try_into().unwrap() {
        let mut curr_morphism_rule: usize = 0;
        if index != 0 {
            curr_morphism_rule = usize::from(symbol_sequence[index]);
        }
        let mut rule_index = 0;
        while symbol_sequence.len() < sequence_length.try_into().unwrap()
            && rule_index < morphism[curr_morphism_rule].len()
        {
            symbol_sequence.push(morphism[curr_morphism_rule][rule_index]);
            rule_index += 1;
        }
        index += 1;
    }
    symbol_sequence
}

fn build_point_sequence(sequence_length: u32) -> Vec<Point3D> {
    let symbol_sequence = build_symbol_sequence(sequence_length);
    let output_map: [(i128, i128, i128); 12] = [
        (1, 0, 0),
        (0, 1, 0),
        (0, 0, 1),
        (1, 0, 0),
        (0, 1, 0),
        (0, 0, 1),
        (1, 0, 0),
        (0, 1, 0),
        (0, 0, 1),
        (1, 0, 0),
        (0, 1, 0),
        (0, 0, 1),
    ];
    let mut point_sequence: Vec<Point3D> = Vec::new();
    point_sequence.push(Point3D { x: 0, y: 0, z: 0 });
    for symbol in &symbol_sequence {
        let prev = match point_sequence.len() {
            0 => &Point3D { x: 0, y: 0, z: 0 },
            n => &point_sequence[n - 1],
        };
        let add: (i128, i128, i128) = output_map[usize::from(*symbol)];
        let next_z = Point3D {
            x: prev.x + add.0,
            y: prev.y + add.1,
            z: prev.z + add.2,
        };
        point_sequence.push(next_z);
    }
    point_sequence
}

fn main() {
    env_logger::init();
    let args: Vec<String> = env::args().collect();
    let (sequence_length, start_index, end_index) = parse_args(args);

    let point_sequence = build_point_sequence(sequence_length);

    let mut max_count = 0;
    for i in start_index..end_index {
        debug!(
            "Progress: considering lines through {}, current max collinear is: {}",
            i, max_count
        );
        let mut line_counts = HashMap::new();
        for j in i + 1..point_sequence.len() {
            let line = Line3D::new_normalised(&point_sequence[i], &point_sequence[j]);
            let count = line_counts.entry(line).or_insert(1);
            *count += 1;
            if *count > max_count {
                max_count = *count;
            }
        }
    }
    info!(
        "Considering all lines through points from index {} to index {}, \
        the largest number of collinear points in the first {} indices of \
        the sequence is {}.",
        start_index, end_index, sequence_length, max_count
    );
}

#[cfg(test)]
mod tests {
    use crate::{gcd, Fraction};

    /// The Greatest Common Divisor function works as expected.
    #[test]
    fn test_gcd() {
        assert_eq!(gcd(1, 1), 1);
        assert_eq!(gcd(2, 2), 2);
        assert_eq!(gcd(2, 4), 2);
        assert_eq!(gcd(4, 2), 2);
        assert_eq!(gcd(12, 20), 4);
        assert_eq!(gcd(20, 12), 4);
        assert_eq!(gcd(-1, 1), 1);
        assert_eq!(gcd(1, -1), 1);
        assert_eq!(gcd(-1, -1), 1);
        assert_eq!(gcd(0, 1), 1);
        assert_eq!(gcd(1, 0), 1);
        assert_eq!(gcd(0, -1), 1);
        assert_eq!(gcd(-1, 0), 1);
        assert_eq!(gcd(6, 9), 3);
        assert_eq!(gcd(9, 6), 3);
        assert_eq!(gcd(-6, 9), 3);
        assert_eq!(gcd(6, -9), 3);
        assert_eq!(gcd(537, 0), 537);
        assert_eq!(gcd(0, 537), 537);
        assert_eq!(gcd(-537, 0), 537);
        assert_eq!(gcd(0, -537), 537);
        assert_eq!(gcd(0, 0), 0);
        assert_eq!(gcd(2 * 3 * 5 * 7 * 11, 13 * 17 * 19 * 23), 1);
    }

    /// Fractions are normalised as expected.
    #[test]
    fn test_normalised_fraction() {
        assert_eq!(
            Fraction::new_normalised(2, 4),
            Fraction { num: 1, denom: 2 }
        );
        assert_eq!(
            Fraction::new_normalised(2, -4),
            Fraction { num: -1, denom: 2 }
        );
        assert_eq!(
            Fraction::new_normalised(1, -2),
            Fraction { num: -1, denom: 2 }
        );
        assert_eq!(
            Fraction::new_normalised(-1, 1),
            Fraction { num: -1, denom: 1 }
        );
        assert_eq!(
            Fraction::new_normalised(1, 1),
            Fraction { num: 1, denom: 1 }
        );
        assert_eq!(
            Fraction::new_normalised(-2, -4),
            Fraction { num: 1, denom: 2 }
        );
        assert_eq!(
            Fraction::new_normalised(-1, -2),
            Fraction { num: 1, denom: 2 }
        );
        assert_eq!(
            Fraction::new_normalised(0, 1),
            Fraction { num: 0, denom: 1}
        );
        assert_eq!(
            Fraction::new_normalised(0, 9762),
            Fraction { num: 0, denom: 1}
        );
        assert_eq!(
            Fraction::new_normalised(0, -1),
            Fraction { num: 0, denom: 1}
        );
        assert_eq!(
            Fraction::new_normalised(0, -9762),
            Fraction { num: 0, denom: 1}
        );
    }
}
