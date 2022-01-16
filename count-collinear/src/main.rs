use std::env;
use std::collections::HashMap;

fn main() {
    let args: Vec<String> = env::args().collect();
    let sequence_length = &args[1];
    let sequence_length: u32 = sequence_length.to_string()
        .trim()
        .parse()
        .expect("Enter a positive integer for a sequence length.");
    
    let morphism: [[u8; 7]; 12] = [
        [ 0, 4, 9, 0, 8, 9, 0],
        [ 1, 5,10, 1, 6,10, 1],
        [ 2, 3,11, 2, 7,11, 2],
        [ 3, 2, 6, 3,10, 6, 3],
        [ 4, 0, 7, 4,11, 7, 4],
        [ 5, 1, 8, 5, 9, 8, 5],
        [ 6, 3, 2, 6, 3,10, 6],
        [ 7, 4, 0, 7, 4,11, 7],
        [ 8, 5, 1, 8, 5, 9, 8],
        [ 9, 0, 4, 9, 0, 8, 9],
        [10, 1, 5,10, 1, 6,10],
        [11, 2, 3,11, 2, 7,11],
    ];
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
    let mut symbol_sequence: Vec<u8> = Vec::new();
    let mut index = 0;
    while symbol_sequence.len() < sequence_length.try_into().unwrap() {
        let mut curr_morphism_rule: usize = 0;
        if index != 0 {
            curr_morphism_rule = usize::from(symbol_sequence[index]);
        }
        let mut rule_index = 0;
        while symbol_sequence.len() < sequence_length.try_into().unwrap() && rule_index < morphism[curr_morphism_rule].len() {
            symbol_sequence.push(morphism[curr_morphism_rule][rule_index]);
            rule_index += 1;
        }
        index += 1;
    }
    
    #[derive(Debug)]
    #[derive(PartialEq, Eq, Hash)]
    struct Point3D {
        x: i128,
        y: i128,
        z: i128,
    }
    #[derive(PartialEq, Eq, Hash)]
    struct Fraction {
        num: i128,
        denom: i128,
    }
    #[derive(PartialEq, Eq, Hash)]
    struct Line3D {
        point: (Fraction, Fraction, Fraction),
        direction: (Fraction, Fraction, Fraction),
    }
    fn gcd(a: i128, b: i128) -> i128 {
        if a < 0 {
            gcd(-a, b)
        } else {
            if b == 0 {
                a
            } else {
                gcd(b, a%b)
            }
        }
    }
    fn build_line(p1: &Point3D, p2: &Point3D) -> Line3D {
        let mut pn: [i128; 3] = [p1.x, p1.y, p1.z];
        let mut pd: [i128; 3] = [1, 1, 1];
        let mut tn: [i128; 3] = [p2.x - p1.x, p2.y - p1.y, p2.z - p1.z];
        let mut td: [i128; 3] = [1, 1, 1];
        let mut non_zero_index = 0;
        while tn[non_zero_index] == 0 {
          non_zero_index += 1;
        }
        let mut mult = tn[non_zero_index];
        for i in non_zero_index..3 {
            td[i] *= mult;
            if td[i] < 0 {
                tn[i] *= -1;
                td[i] *= -1;
            }
        }
        for i in 0..3 {
            if tn[i] == 0 {
                td[i] = 1;
                continue;
            }
            let div = gcd(tn[i], td[i]);
            tn[i] /= div;
            td[i] /= div;
        }
        mult = pn[non_zero_index];
        for i in 0..3 {
            pn[i] *= td[i];
            pd[i] *= td[i];
            pn[i] -= mult*tn[i];
            if pd[i] < 0 {
                pn[i] *= -1;
                pd[i] *= -1;
            }
            let div = gcd(pn[i], pd[i]);
            pn[i] /= div;
            pd[i] /= div;
        }

        let point_x = Fraction {num: pn[0], denom: pd[0]};
        let point_y = Fraction {num: pn[1], denom: pd[1]};
        let point_z = Fraction {num: pn[2], denom: pd[2]};
        let direction_x = Fraction {num: tn[0], denom: td[0]};
        let direction_y = Fraction {num: tn[1], denom: td[1]};
        let direction_z = Fraction {num: tn[2], denom: td[2]};
        Line3D { point: (point_x, point_y, point_z), direction: (direction_x, direction_y, direction_z)}
    }


    let mut z_sequence: Vec<Point3D> = Vec::new();
    z_sequence.push(Point3D { 
        x: 0, y: 0, z: 0
    });
    for symbol in &symbol_sequence {
        let prev = match z_sequence.len() {
            0 => &Point3D {x: 0, y: 0, z: 0},
            n => &z_sequence[n-1],
        };
        let add: (i128, i128, i128) = output_map[usize::from(*symbol)];
        let next_z = Point3D {
            x: prev.x + add.0,
            y: prev.y + add.1,
            z: prev.z + add.2,
        };
        z_sequence.push(next_z);
    }
    let mut max_count = 0;
    for i in 0..z_sequence.len() - 1 {
        println!("Progress: considering lines through {}, current max collinear is: {}", i, max_count);
        let mut line_counts = HashMap::new();
        for j in i+1..z_sequence.len() {
            let line = build_line(&z_sequence[i], &z_sequence[j]);
            let count = line_counts.entry(line).or_insert(1);
            *count += 1;
            if *count > max_count {
                max_count = *count;
            }
        }
    }
    println!("The largest number of collinear points in the first {} indices is {}.", sequence_length, max_count);
}
