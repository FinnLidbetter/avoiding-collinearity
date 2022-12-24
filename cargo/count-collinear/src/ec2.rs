use crate::aws_request::{get_authorization_header, get_base_headers};
use http::HeaderMap;
use std::collections::HashMap;
use std::fmt;
use std::fmt::Formatter;

const API_VERSION: &str = "2016-11-15";
const SERVICE_NAME: &str = "ec2";
type Result<T> = std::result::Result<T, EC2Error>;

pub struct EC2Error {
    msg: String,
}

impl fmt::Display for EC2Error {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        write!(f, "EC2Error: {}", self.msg)
    }
}

pub struct EC2Controller {
    access_key: String,
    secret_access_key: String,
    region: String,
    client: reqwest::blocking::Client,
}

impl EC2Controller {
    pub fn new(access_key: &str, secret_access_key: &str, region: &str) -> EC2Controller {
        let access_key = String::from(access_key);
        let secret_access_key = String::from(secret_access_key);
        let region = String::from(region);
        let client = reqwest::blocking::Client::new();
        EC2Controller {
            access_key,
            secret_access_key,
            region,
            client,
        }
    }

    pub fn get_instance_id(&self) -> Result<String> {
        let endpoint = "http://169.254.169.254/latest/meta-data/instance-id";
        let request = self.client.get(endpoint);
        let result = request.send().map_err(|err| EC2Error {
            msg: format!("Request to {} failed due to {}", endpoint, err),
        })?;
        result.text().map_err(|_| EC2Error {
            msg: "Decoding response for get_instance_id failed.".to_string(),
        })
    }

    fn get_endpoint(&self) -> String {
        format!("https://ec2.{}.amazonaws.com", self.region)
    }

    pub fn terminate(&self) -> Result<()> {
        let instance_id = self.get_instance_id()?;
        self.terminate_instance(instance_id)
    }

    pub fn terminate_instance(&self, instance_id: String) -> Result<()> {
        let method = "GET";
        let endpoint = self.get_endpoint();
        let endpoint = endpoint.as_str();
        let params: HashMap<&str, &str> = HashMap::from([
            ("Action", "TerminateInstances"),
            ("Version", API_VERSION),
            ("InstanceId.1", instance_id.as_str()),
        ]);
        let query_params: Vec<(&str, &str)> =
            params.iter().map(|(key, value)| (*key, *value)).collect();
        let base_headers = get_base_headers(SERVICE_NAME, self.region.as_str());
        let mut headers: HashMap<&str, &str> = base_headers
            .iter()
            .map(|(key, value)| (*key, value.as_str()))
            .collect();
        let authorization_header = get_authorization_header(
            method,
            endpoint,
            params,
            &headers,
            None,
            self.region.as_str(),
            SERVICE_NAME,
            self.access_key.as_str(),
            self.secret_access_key.as_str(),
        )
        .map_err(|err| EC2Error {
            msg: format!("Failed to get authorization header due to '{}'.", err.msg),
        })?;
        headers.insert("Authorization", authorization_header.as_str());
        let headers: HashMap<String, String> = headers
            .iter()
            .map(|(key, value)| ((*key).to_string(), (*value).to_string()))
            .collect();
        let mut request = self.client.get(endpoint);
        let header_map: HeaderMap = (&headers).try_into().map_err(|err| EC2Error {
            msg: format!("Request to {} failed due to {}", endpoint, err),
        })?;
        request = request.headers(header_map);
        request = request.query(&query_params);
        let result = request.send().map_err(|err| EC2Error {
            msg: format!("Request to {} failed due to {}", endpoint, err),
        })?;
        result.error_for_status_ref().map_err(|err| EC2Error {
            msg: format!("Request to {} failed due to {}", endpoint, err),
        })?;
        Ok(())
    }
}
