use log::debug;
use std::cmp::min;
use std::collections::HashMap;

#[derive(Debug, PartialEq, Eq, Hash)]
pub struct Point3D {
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
    /// Get a normalised Fraction.
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

#[derive(Debug, PartialEq, Eq, Hash)]
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
                direction_numerators[index] *= -1;
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
        return gcd(-a, b);
    }
    if b == 0 {
        return a;
    }
    gcd(b, a % b)
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

pub fn build_point_sequence(sequence_length: u32) -> Vec<Point3D> {
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
    let mut point_sequence: Vec<Point3D> = vec![Point3D { x: 0, y: 0, z: 0 }];
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

/// Get the largest number of points in the sequence intersected by a single line.
///
/// Only lines going through points with indices in [`start_index`, `end_index`) are
/// considered and only points after `start_index` are considered.
/// Note that that all points after `start_index` in `point_sequence` are considered
/// for counting points on lines having some point at an index in
/// [`start_index`, `end_index`).
pub fn count_collinear_points(
    point_sequence: &[Point3D],
    start_index: usize,
    end_index: usize,
    window_size: usize,
) -> i32 {
    let mut count_max = 0;
    for i in start_index..end_index {
        debug!(
            "Progress: considering lines through {}, current max collinear is: {}",
            i, count_max
        );
        let mut line_counts = HashMap::new();
        let window_end = min(point_sequence.len(), i + window_size);
        for j in i + 1..window_end {
            let line = Line3D::new_normalised(&point_sequence[i], &point_sequence[j]);
            let count = line_counts.entry(line).or_insert(1);
            *count += 1;
            if *count > count_max {
                count_max = *count;
            }
        }
    }
    count_max
}

#[cfg(test)]
mod tests {
    use super::*;

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
            Fraction { num: 0, denom: 1 }
        );
        assert_eq!(
            Fraction::new_normalised(0, 9762),
            Fraction { num: 0, denom: 1 }
        );
        assert_eq!(
            Fraction::new_normalised(0, -1),
            Fraction { num: 0, denom: 1 }
        );
        assert_eq!(
            Fraction::new_normalised(0, -9762),
            Fraction { num: 0, denom: 1 }
        );
    }

    /// Test normalisation functions as expected.
    #[test]
    fn test_line_normalisation() {
        assert_eq!(
            Line3D::new_normalised(&Point3D { x: 1, y: 1, z: 1 }, &Point3D { x: 2, y: 2, z: 2 },),
            Line3D {
                point: (
                    Fraction { num: 0, denom: 1 },
                    Fraction { num: 0, denom: 1 },
                    Fraction { num: 0, denom: 1 }
                ),
                direction: (
                    Fraction { num: 1, denom: 1 },
                    Fraction { num: 1, denom: 1 },
                    Fraction { num: 1, denom: 1 }
                )
            }
        );
    }

    /// Test that pairs of collinear points normalise to the same line.
    ///
    /// The points all lie on a line parallel to the x axis.
    #[test]
    fn test_x_axis_parallel_line_normalisation() {
        let p1 = Point3D { x: 0, y: 500, z: 2 };
        let p2 = Point3D { x: 1, y: 500, z: 2 };
        let p3 = Point3D {
            x: -4,
            y: 500,
            z: 2,
        };
        let p4 = Point3D { x: 7, y: 500, z: 2 };
        let p5 = Point3D {
            x: 39412,
            y: 500,
            z: 2,
        };
        let normalised_line = Line3D::new_normalised(&p1, &p2);
        let points = [p1, p2, p3, p4, p5];
        for point_1 in &points {
            for point_2 in &points {
                if point_1 == point_2 {
                    continue;
                }
                assert_eq!(Line3D::new_normalised(point_1, point_2), normalised_line);
            }
        }
        let p6 = Point3D {
            x: 428,
            y: 499,
            z: 2,
        };
        for point in &points {
            assert_ne!(Line3D::new_normalised(&p6, point), normalised_line);
        }
    }

    /// Test that pairs of collinear points normalise to the same line.
    ///
    /// The points all lie on a line parallel to the y axis.
    #[test]
    fn test_y_axis_parallel_line_normalisation() {
        let p1 = Point3D { x: 428, y: 0, z: 2 };
        let p2 = Point3D { x: 428, y: 1, z: 2 };
        let p3 = Point3D { x: 428, y: 2, z: 2 };
        let p4 = Point3D {
            x: 428,
            y: -3,
            z: 2,
        };
        let p5 = Point3D {
            x: 428,
            y: 500,
            z: 2,
        };
        let normalised_line = Line3D::new_normalised(&p1, &p2);
        let points = [p1, p2, p3, p4, p5];
        for point_1 in &points {
            for point_2 in &points {
                if point_1 == point_2 {
                    continue;
                }
                assert_eq!(Line3D::new_normalised(point_1, point_2), normalised_line);
            }
        }
        let p6 = Point3D {
            x: 428,
            y: 10,
            z: 3,
        };
        for point in &points {
            assert_ne!(Line3D::new_normalised(&p6, point), normalised_line);
        }
    }

    /// Test that pairs of collinear points normalise to the same line.
    ///
    /// The points all lie on a line parallel to the z axis.
    #[test]
    fn test_z_axis_parallel_line_normalisation() {
        let p1 = Point3D {
            x: 428,
            y: 500,
            z: 0,
        };
        let p2 = Point3D {
            x: 428,
            y: 500,
            z: 1,
        };
        let p3 = Point3D {
            x: 428,
            y: 500,
            z: 2,
        };
        let p4 = Point3D {
            x: 428,
            y: 500,
            z: -5,
        };
        let p5 = Point3D {
            x: 428,
            y: 500,
            z: 37,
        };
        let normalised_line = Line3D::new_normalised(&p1, &p2);
        let points = [p1, p2, p3, p4, p5];
        for point_1 in &points {
            for point_2 in &points {
                if point_1 == point_2 {
                    continue;
                }
                assert_eq!(Line3D::new_normalised(point_1, point_2), normalised_line);
            }
        }
        let p6 = Point3D {
            x: 42329,
            y: 500,
            z: 412,
        };
        for point in &points {
            assert_ne!(Line3D::new_normalised(&p6, point), normalised_line);
        }
    }

    /// Test that pairs of collinear points normalise to the same line.
    #[test]
    fn test_other_line_normalisation() {
        let p1 = Point3D {
            x: 345,
            y: 10,
            z: 97,
        };
        let p2 = Point3D {
            x: 375,
            y: 70,
            z: 106,
        };
        let p3 = Point3D {
            x: 355,
            y: 30,
            z: 100,
        };
        let p4 = Point3D {
            x: 445,
            y: 210,
            z: 127,
        };
        let p5 = Point3D {
            x: 455,
            y: 230,
            z: 130,
        };
        let normalised_line = Line3D::new_normalised(&p1, &p2);
        let points = [p1, p2, p3, p4, p5];
        for point_1 in &points {
            for point_2 in &points {
                if point_1 == point_2 {
                    continue;
                }
                assert_eq!(Line3D::new_normalised(point_1, point_2), normalised_line);
            }
        }
        let p6 = Point3D {
            x: 444,
            y: 444,
            z: 444,
        };
        for point in &points {
            assert_ne!(Line3D::new_normalised(&p6, point), normalised_line);
        }
    }

    #[test]
    fn test_build_point_sequence() {
        // The zero point is always included.
        let point_sequence = build_point_sequence(0);
        assert_eq!(point_sequence.len(), 1);
        assert_eq!(point_sequence[0], Point3D { x: 0, y: 0, z: 0 });
        let point_sequence = build_point_sequence(1);
        assert_eq!(point_sequence.len(), 2);
        assert_eq!(point_sequence[0], Point3D { x: 0, y: 0, z: 0 });
        assert_eq!(point_sequence[1], Point3D { x: 1, y: 0, z: 0 });
        // The first 7 points.
        let point_sequence = build_point_sequence(7);
        assert_eq!(point_sequence.len(), 8);
        assert_eq!(point_sequence[0], Point3D { x: 0, y: 0, z: 0 });
        assert_eq!(point_sequence[1], Point3D { x: 1, y: 0, z: 0 });
        assert_eq!(point_sequence[2], Point3D { x: 1, y: 1, z: 0 });
        assert_eq!(point_sequence[3], Point3D { x: 2, y: 1, z: 0 });
        assert_eq!(point_sequence[4], Point3D { x: 3, y: 1, z: 0 });
        assert_eq!(point_sequence[5], Point3D { x: 3, y: 1, z: 1 });
        assert_eq!(point_sequence[6], Point3D { x: 4, y: 1, z: 1 });
        assert_eq!(point_sequence[7], Point3D { x: 5, y: 1, z: 1 });
        // The first occurrence of 6 collinear points.
        let point_sequence = build_point_sequence(185);
        assert_eq!(point_sequence.len(), 186);
        assert_eq!(
            point_sequence[109],
            Point3D {
                x: 46,
                y: 40,
                z: 23
            }
        );
        assert_eq!(
            point_sequence[113],
            Point3D {
                x: 48,
                y: 41,
                z: 24
            }
        );
        assert_eq!(
            point_sequence[145],
            Point3D {
                x: 64,
                y: 49,
                z: 32
            }
        );
        assert_eq!(
            point_sequence[149],
            Point3D {
                x: 66,
                y: 50,
                z: 33
            }
        );
        assert_eq!(
            point_sequence[181],
            Point3D {
                x: 82,
                y: 58,
                z: 41
            }
        );
        assert_eq!(
            point_sequence[185],
            Point3D {
                x: 84,
                y: 59,
                z: 42
            }
        );
    }

    #[test]
    fn test_count_collinear() {
        let point_sequence = build_point_sequence(7);
        assert_eq!(count_collinear_points(&point_sequence, 0, 1, 100), 2);
        let cases = [
            (7, 0, 1, 2),
            (185, 0, 185, 6),
            (185, 0, 110, 6),
            (185, 0, 109, 5),
            (185, 109, 110, 6),
            (185, 110, 185, 5),
            (184, 0, 185, 5),
            (184, 0, 1000, 5),
        ];
        for (sequence_length, start_index, end_index, expected_collinear) in cases {
            let point_sequence = build_point_sequence(sequence_length);
            assert_eq!(
                count_collinear_points(&point_sequence, start_index, end_index, 10000),
                expected_collinear
            );
        }
    }
}
