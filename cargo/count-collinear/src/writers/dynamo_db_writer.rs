use crate::dynamo_db::{AttributeValue, DynamoDbController};
use crate::settings::Config;
use crate::writers::CollinearWriterError;
use crate::{CollinearWriter, CountCollinearResult};
use std::fmt;
use std::fmt::Formatter;
use std::thread::sleep;
use std::time::{Duration, Instant};

const TABLE_NAME: &str = "collinearity";
const WRITE_INTERVAL_SECONDS: u64 = 60;

pub struct DynamoDbWriter {
    dynamo_db_controller: DynamoDbController,
    last_write_time: Option<Instant>,
}

impl DynamoDbWriter {
    pub fn new(config: &Config) -> Result<DynamoDbWriter, CollinearWriterError> {
        let aws_auth_settings = config
            .aws_auth_settings
            .as_ref()
            .ok_or(CollinearWriterError {
                msg: "Missing AWS auth settings".to_string(),
            })?;
        let dynamo_db_controller = DynamoDbController::new(
            aws_auth_settings.access_key.as_str(),
            aws_auth_settings.secret_key.as_str(),
            aws_auth_settings.account_number.as_str(),
            aws_auth_settings.region.as_str(),
        );
        let last_write_time = None;
        Ok(DynamoDbWriter {
            dynamo_db_controller,
            last_write_time,
        })
    }
}
impl fmt::Display for DynamoDbWriter {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        write!(f, "DynamoDB Writer")
    }
}

impl CollinearWriter for DynamoDbWriter {
    fn write_count_collinear_result(
        &mut self,
        count_collinear_result: CountCollinearResult,
    ) -> Result<(), CollinearWriterError> {
        let partiql_statement = format!(
            "INSERT INTO {} VALUE {{'seqeunce_length': ?, 'start_index': ?, 'end_index': ?, 'count_max': ?, 'build_duration_seconds': ?, 'count_duration_seconds': ?}}",
            TABLE_NAME
        );
        let parameters = vec![
            AttributeValue::Number(count_collinear_result.sequence_length.to_string()),
            AttributeValue::Number(count_collinear_result.start_index.to_string()),
            AttributeValue::Number(count_collinear_result.end_index.to_string()),
            AttributeValue::Number(
                count_collinear_result
                    .build_duration
                    .as_secs_f32()
                    .to_string(),
            ),
            AttributeValue::Number(
                count_collinear_result
                    .count_duration
                    .as_secs_f32()
                    .to_string(),
            ),
        ];
        match self.last_write_time {
            Some(last_write_time) => {
                let seconds_since_write = last_write_time.elapsed().as_secs();
                if seconds_since_write < WRITE_INTERVAL_SECONDS {
                    sleep(Duration::from_secs(
                        WRITE_INTERVAL_SECONDS - seconds_since_write,
                    ));
                }
            }
            None => (),
        }
        self.last_write_time = Some(Instant::now());
        self.dynamo_db_controller
            .execute_statement(partiql_statement.as_str(), parameters)
            .map_err(|err| CollinearWriterError {
                msg: format!("DynamoDB write failed: {}", err),
            })?;
        Ok(())
    }
}
