FROM rust as builder

WORKDIR /usr/src/countcollinear
COPY . .
RUN cargo install --path . --bin count_collinear

FROM debian:buster-slim
RUN apt-get update && apt-get install -y libssl-dev ca-certificates && rm -rf /var/lib/apt/lists/*
COPY --from=builder /usr/local/cargo/bin/count_collinear /usr/local/bin/count_collinear
COPY defaults.conf .

CMD ["count_collinear"]
