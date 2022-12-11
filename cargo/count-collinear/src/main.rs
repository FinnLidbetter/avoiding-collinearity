extern crate core;

mod aws_request;
mod aws_signing;
mod dynamo_db;
mod sqs;
mod utilities;

use log::{debug, error, info};
use std::collections::HashMap;
use std::env;
use std::error;
use std::path::PathBuf;
use std::process;

use configparser::ini::Ini;
use lettre::{
    message::Mailbox, transport::smtp::authentication::Credentials, Message, SmtpTransport,
    Transport,
};

#[derive(Debug, Clone)]
pub struct Config {
    from_email: String,
    to_email: String,
    smtp_url: String,
    smtp_username: String,
    smtp_password: String,
    aws_access_key: String,
    aws_secret_key: String,
    aws_account_number: String,
    aws_region: String,
    log_level: String,
}

impl Config {
    pub fn new(config_ini: Ini) -> Config {
        let from_email = config_ini.get("email", "from_email").unwrap();
        let to_email = config_ini.get("email", "to_email").unwrap();
        let smtp_url = config_ini.get("email", "smtp_url").unwrap();
        let smtp_username = config_ini.get("email", "smtp_username").unwrap();
        let smtp_password = config_ini.get("email", "smtp_password").unwrap();
        let log_level = config_ini.get("main", "log_level").unwrap();
        let aws_access_key = config_ini.get("aws_auth", "access_key").unwrap();
        let aws_secret_key = config_ini.get("aws_auth", "secret_key").unwrap();
        let aws_account_number = config_ini.get("aws_auth", "account_number").unwrap();
        let aws_region = config_ini.get("aws_auth", "region").unwrap();
        Config {
            from_email,
            to_email,
            smtp_url,
            smtp_username,
            smtp_password,
            aws_access_key,
            aws_secret_key,
            aws_account_number,
            aws_region,
            log_level,
        }
    }
}

/// Get a path to the configuration file.
fn get_config_path() -> PathBuf {
    let mut default_config_path = PathBuf::new();
    default_config_path.push("defaults.conf");
    let home = dirs::home_dir();
    let prod_config_dir = match home {
        None => {
            let mut default_dir = PathBuf::new();
            default_dir.push("/home/ubuntu");
            default_dir
        }
        Some(val) => val,
    };
    let prod_config_path = prod_config_dir.as_path().join(".count_collinear.conf");
    let mut config_path = prod_config_path;
    if !config_path.as_path().exists() {
        config_path = default_config_path;
    }
    config_path
}

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

/// Get the largest number of points in the sequence intersected by a single line.
///
/// Only lines going through points with indices in [`start_index`, `end_index`) are
/// considered and only points after `start_index` are considered.
/// Note that that all points after `start_index` in `point_sequence` are considered
/// for counting points on lines having some point at an index in
/// [`start_index`, `end_index`).
fn count_collinear_points(
    point_sequence: Vec<Point3D>,
    start_index: usize,
    end_index: usize,
) -> i32 {
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
    max_count
}

fn send_result(
    sequence_length: u32,
    start_index: usize,
    end_index: usize,
    max_count: i32,
    config: &Config,
) -> Result<(), Box<dyn error::Error>> {
    let mailer = SmtpTransport::starttls_relay(&config.smtp_url.clone())?
        // Add credentials for authentication
        .credentials(Credentials::new(
            config.smtp_username.clone(),
            config.smtp_password.clone(),
        ))
        .build();

    let sender_mailbox: Mailbox = format!("<{}>", &config.from_email).parse()?;
    let receiver_mailbox: Mailbox = format!("<{}>", &config.to_email).parse()?;
    let subject = "Count Collinear Results";
    let body = format!(
        "Considering all lines with at least one point with an index in [{}, {}], \
        the largest number of collinear points in the first {} indices of \
        the sequence is {}.",
        start_index, end_index, sequence_length, max_count
    );
    let email = match Message::builder()
        .from(sender_mailbox)
        .to(receiver_mailbox)
        .subject(subject)
        .body(body)
    {
        Ok(email) => email,
        Err(err) => {
            error!("Failed to construct email: {}", err);
            return Ok(());
        }
    };
    match mailer.send(&email) {
        Ok(_) => Ok(()),
        Err(err) => {
            error!("Failed to send email: {}", err);
            Ok(())
        }
    }
}

fn main() {
    let mut config_ini = Ini::new();
    let config_path = get_config_path();
    println!("Reading config from {:?}", config_path.clone());
    if let Err(failure_reason) = config_ini.load(config_path.clone()) {
        env::set_var("RUST_LOG", "count-collinear=info");
        env_logger::init();
        log::info!("Loading config from {:?}", &config_path);
        log::error!("Failed to load config: {}", failure_reason);
        process::exit(1);
    }
    let config = Config::new(config_ini);
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

#[cfg(test)]
mod tests {
    use crate::{build_point_sequence, count_collinear_points, gcd, Fraction, Line3D, Point3D};

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
        assert_eq!(count_collinear_points(point_sequence, 0, 1), 2);
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
                count_collinear_points(point_sequence, start_index, end_index),
                expected_collinear
            );
        }
    }
}
