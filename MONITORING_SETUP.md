# Meldestelle Monitoring System

This document describes the monitoring system set up for the Meldestelle application. The monitoring system includes metrics collection, visualization, centralized logging, and alerting.

## Components

The monitoring system consists of the following components:

1. **Prometheus** - For metrics collection and storage
2. **Grafana** - For metrics visualization and dashboards
3. **ELK Stack** - For centralized logging (Elasticsearch, Logstash, Kibana)
4. **Alertmanager** - For alert management and notifications

## Architecture

The monitoring system is deployed as Docker containers alongside the Meldestelle application. The components interact as follows:

- The Meldestelle application exposes metrics at the `/metrics` endpoint
- Prometheus scrapes metrics from the application and stores them
- Grafana visualizes the metrics from Prometheus
- The application sends logs to Logstash
- Logstash processes the logs and sends them to Elasticsearch
- Kibana visualizes the logs from Elasticsearch
- Prometheus evaluates alerting rules and sends alerts to Alertmanager
- Alertmanager manages alerts and sends notifications via configured channels (email, Slack, etc.)

## Setup

The monitoring system is configured in the `docker-compose.yml` file and the configuration files in the `config/monitoring` directory.

### Prerequisites

- Docker and Docker Compose
- The Meldestelle application running with metrics enabled

### Starting the Monitoring System

To start the monitoring system, run:

```bash
docker-compose up -d prometheus grafana alertmanager
```

To start the ELK Stack, run:

```bash
docker-compose up -d elasticsearch logstash kibana
```

### Testing the Monitoring System

A test script is provided to verify that the monitoring system is working correctly:

```bash
./test-monitoring.sh
```

## Accessing the Monitoring Tools

- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (default credentials: admin/admin)
- **Alertmanager**: http://localhost:9093
- **Kibana**: http://localhost:5601

## Metrics

The following metrics are collected by Prometheus:

### JVM Metrics

- Memory usage (heap and non-heap)
- Garbage collection statistics
- Thread counts
- Class loading statistics
- CPU usage

### Application Metrics

- HTTP request counts
- HTTP request durations
- Error rates
- Custom business metrics

## Dashboards

Grafana dashboards are provided for visualizing the metrics:

- **JVM Dashboard**: Shows JVM metrics such as memory usage, garbage collection, and thread counts
- **Application Dashboard**: Shows application metrics such as request rates, error rates, and response times

## Alerting

Alerting is configured in Prometheus and Alertmanager. The following alerts are defined:

- **High Memory Usage**: Triggered when JVM heap memory usage exceeds 85% for 5 minutes
- **High CPU Usage**: Triggered when CPU usage exceeds 85% for 5 minutes
- **High Error Rate**: Triggered when the error rate exceeds 5% for 2 minutes
- **Service Unavailable**: Triggered when the service is down for 1 minute
- **Slow Response Time**: Triggered when the average response time exceeds 1 second for 5 minutes
- **High GC Pause Time**: Triggered when the average GC pause time exceeds 0.5 seconds for 5 minutes

Alerts are sent to the configured notification channels (email and Slack).

## Logging

Logs are collected by Logstash, stored in Elasticsearch, and visualized in Kibana. The following log sources are configured:

- Application logs via TCP (JSON format)
- File logs from the `/var/log/meldestelle` directory

## Configuration Files

- **Prometheus**: `config/monitoring/prometheus.yml`
- **Alertmanager**: `config/monitoring/alertmanager/alertmanager.yml`
- **Alerting Rules**: `config/monitoring/prometheus/rules/alerts.yml`
- **Grafana Dashboards**: `config/monitoring/grafana/dashboards/`
- **Grafana Datasources**: `config/monitoring/grafana/provisioning/datasources/`
- **Logstash**: `config/monitoring/elk/logstash.conf`
- **Elasticsearch**: `config/monitoring/elk/elasticsearch.yml`

## Troubleshooting

### Prometheus

- Check if Prometheus is running: `docker-compose ps prometheus`
- Check Prometheus logs: `docker-compose logs prometheus`
- Verify that Prometheus can scrape metrics: http://localhost:9090/targets
- Check if alerting rules are loaded: http://localhost:9090/rules

### Grafana

- Check if Grafana is running: `docker-compose ps grafana`
- Check Grafana logs: `docker-compose logs grafana`
- Verify that Grafana can connect to Prometheus: http://localhost:3000/datasources

### Alertmanager

- Check if Alertmanager is running: `docker-compose ps alertmanager`
- Check Alertmanager logs: `docker-compose logs alertmanager`
- Verify that Alertmanager is receiving alerts: http://localhost:9093/#/alerts

### ELK Stack

- Check if Elasticsearch is running: `docker-compose ps elasticsearch`
- Check Elasticsearch logs: `docker-compose logs elasticsearch`
- Check if Logstash is running: `docker-compose ps logstash`
- Check Logstash logs: `docker-compose logs logstash`
- Check if Kibana is running: `docker-compose ps kibana`
- Check Kibana logs: `docker-compose logs kibana`
- Verify that Elasticsearch is receiving logs: http://localhost:9200/_cat/indices
- Verify that Kibana can connect to Elasticsearch: http://localhost:5601/app/management/kibana/indexPatterns

## Maintenance

### Backup and Restore

- Prometheus data is stored in the `prometheus_data` volume
- Grafana data is stored in the `grafana_data` volume
- Alertmanager data is stored in the `alertmanager_data` volume
- Elasticsearch data is stored in the `elasticsearch_data` volume

To backup these volumes, use Docker's volume backup functionality:

```bash
docker run --rm -v prometheus_data:/source -v $(pwd)/backup:/backup alpine tar -czf /backup/prometheus_data.tar.gz -C /source .
```

To restore from a backup:

```bash
docker run --rm -v prometheus_data:/target -v $(pwd)/backup:/backup alpine sh -c "rm -rf /target/* && tar -xzf /backup/prometheus_data.tar.gz -C /target"
```

### Updating

To update the monitoring components, update the image tags in the `docker-compose.yml` file and run:

```bash
docker-compose pull prometheus grafana alertmanager
docker-compose up -d prometheus grafana alertmanager
```

## Security Considerations

- The monitoring system is configured for development and testing purposes
- For production use, consider the following security measures:
  - Enable authentication for Prometheus
  - Use strong passwords for Grafana
  - Configure TLS for all components
  - Restrict access to the monitoring endpoints
  - Use environment variables for sensitive configuration values
  - Implement network segmentation to isolate the monitoring system

## Further Reading

- [Prometheus Documentation](https://prometheus.io/docs/introduction/overview/)
- [Grafana Documentation](https://grafana.com/docs/grafana/latest/)
- [Alertmanager Documentation](https://prometheus.io/docs/alerting/latest/alertmanager/)
- [ELK Stack Documentation](https://www.elastic.co/guide/index.html)
