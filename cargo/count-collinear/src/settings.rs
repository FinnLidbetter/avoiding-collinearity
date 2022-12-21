use configparser::ini::Ini;
use std::path::PathBuf;

use std::env;
use std::process;

pub fn read_config() -> Config {
    let mut config_ini = Ini::new();
    let config_path = get_config_path();
    println!("Reading config from {:?}", config_path.clone());
    if let Err(failure_reason) = config_ini.load(config_path.clone()) {
        // If loading the config failed, then setup the logger and log
        // the failure reason.
        env::set_var("RUST_LOG", "count-collinear=info");
        env_logger::init();
        log::info!("Loading config from {:?}", &config_path);
        log::error!("Failed to load config: {}", failure_reason);
        process::exit(1);
    }
    Config::new(config_ini)
}

#[derive(Debug, Clone)]
pub enum Source {
    Args,
    StdIn,
    Sqs,
}

#[derive(Debug, Clone)]
pub enum Destination {
    DynamoDb,
    Email,
    StdOut,
}

#[derive(Debug, Clone)]
pub struct EmailSettings {
    pub from_email: String,
    pub to_email: String,
    pub smtp_url: String,
    pub smtp_username: String,
    pub smtp_password: String,
}
impl EmailSettings {
    fn new(
        from_email: Option<String>,
        to_email: Option<String>,
        smtp_url: Option<String>,
        smtp_username: Option<String>,
        smtp_password: Option<String>,
    ) -> Option<EmailSettings> {
        Some(EmailSettings {
            from_email: from_email?,
            to_email: to_email?,
            smtp_url: smtp_url?,
            smtp_username: smtp_username?,
            smtp_password: smtp_password?,
        })
    }
}

#[derive(Debug, Clone)]
pub struct AWSAuthSettings {
    pub access_key: String,
    pub secret_key: String,
    pub account_number: String,
    pub region: String,
}
impl AWSAuthSettings {
    pub fn new(
        access_key: Option<String>,
        secret_key: Option<String>,
        account_number: Option<String>,
        region: Option<String>,
    ) -> Option<AWSAuthSettings> {
        Some(AWSAuthSettings {
            access_key: access_key?,
            secret_key: secret_key?,
            account_number: account_number?,
            region: region?,
        })
    }
}

#[derive(Debug, Clone)]
pub struct Config {
    pub log_level: String,
    pub input_source: Source,
    pub output_destination: Destination,
    pub email_settings: Option<EmailSettings>,
    pub aws_auth_settings: Option<AWSAuthSettings>,
}

impl Config {
    pub fn new(config_ini: Ini) -> Config {
        let from_email = config_ini.get("email", "from_email");
        let to_email = config_ini.get("email", "to_email");
        let smtp_url = config_ini.get("email", "smtp_url");
        let smtp_username = config_ini.get("email", "smtp_username");
        let smtp_password = config_ini.get("email", "smtp_password");
        let email_settings =
            EmailSettings::new(from_email, to_email, smtp_url, smtp_username, smtp_password);

        let aws_access_key = config_ini.get("aws_auth", "access_key");
        let aws_secret_key = config_ini.get("aws_auth", "secret_key");
        let aws_account_number = config_ini.get("aws_auth", "account_number");
        let aws_region = config_ini.get("aws_auth", "region");
        let aws_auth_settings = AWSAuthSettings::new(
            aws_access_key,
            aws_secret_key,
            aws_account_number,
            aws_region,
        );
        let log_level = config_ini.get("main", "log_level").unwrap();
        let input_source = config_ini.get("main", "source").unwrap();
        let output_destination = config_ini.get("main", "destination").unwrap();
        let input_source = match input_source.to_lowercase().as_str() {
            "args" => Source::Args,
            "sqs" => Source::Sqs,
            "stdin" | _ => Source::StdIn,
        };
        let output_destination = match output_destination.to_lowercase().as_str() {
            "dynamodb" => Destination::DynamoDb,
            "email" => Destination::Email,
            "stdout" | _ => Destination::StdOut,
        };
        Config {
            log_level,
            input_source,
            output_destination,
            email_settings,
            aws_auth_settings,
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
