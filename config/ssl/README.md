# SSL/TLS Certificate Setup for Production

This directory contains SSL/TLS certificates and keys for securing the Meldestelle application in production.

## Directory Structure

```
config/ssl/
├── postgres/          # PostgreSQL SSL certificates
├── redis/             # Redis TLS certificates
├── keycloak/          # Keycloak HTTPS certificates
├── prometheus/        # Prometheus HTTPS certificates
├── grafana/           # Grafana HTTPS certificates
├── nginx/             # Nginx SSL certificates
└── README.md          # This file
```

## Certificate Requirements

### 1. PostgreSQL SSL Certificates
Place the following files in `config/ssl/postgres/`:
- `server.crt` - Server certificate
- `server.key` - Server private key
- `ca.crt` - Certificate Authority certificate

### 2. Redis TLS Certificates
Place the following files in `config/ssl/redis/`:
- `redis.crt` - Redis server certificate
- `redis.key` - Redis server private key
- `ca.crt` - Certificate Authority certificate
- `redis.dh` - Diffie-Hellman parameters

### 3. Keycloak HTTPS Certificates
Place the following files in `config/ssl/keycloak/`:
- `server.crt.pem` - Server certificate in PEM format
- `server.key.pem` - Server private key in PEM format

### 4. Prometheus HTTPS Certificates
Place the following files in `config/ssl/prometheus/`:
- `prometheus.crt` - Prometheus server certificate
- `prometheus.key` - Prometheus server private key
- `web.yml` - Prometheus web configuration file

### 5. Grafana HTTPS Certificates
Place the following files in `config/ssl/grafana/`:
- `server.crt` - Grafana server certificate
- `server.key` - Grafana server private key

### 6. Nginx SSL Certificates
Place the following files in `config/ssl/nginx/`:
- `server.crt` - Main SSL certificate
- `server.key` - Main SSL private key
- `dhparam.pem` - Diffie-Hellman parameters

## Generating Self-Signed Certificates (Development/Testing)

⚠️ **Warning**: Only use self-signed certificates for development and testing. Use proper CA-signed certificates in production.

### Generate CA Certificate
```bash
# Create CA private key
openssl genrsa -out ca.key 4096

# Create CA certificate
openssl req -new -x509 -days 365 -key ca.key -out ca.crt \
  -subj "/C=AT/ST=Vienna/L=Vienna/O=Meldestelle/OU=IT/CN=Meldestelle-CA"
```

### Generate Server Certificates
```bash
# For each service, generate private key and certificate signing request
openssl genrsa -out server.key 2048
openssl req -new -key server.key -out server.csr \
  -subj "/C=AT/ST=Vienna/L=Vienna/O=Meldestelle/OU=IT/CN=your-domain.com"

# Sign the certificate with CA
openssl x509 -req -days 365 -in server.csr -CA ca.crt -CAkey ca.key \
  -CAcreateserial -out server.crt

# Clean up
rm server.csr
```

### Generate Diffie-Hellman Parameters
```bash
openssl dhparam -out dhparam.pem 2048
```

## Production Certificate Setup

### Option 1: Let's Encrypt (Recommended)
Use Certbot to obtain free SSL certificates:

```bash
# Install certbot
sudo apt-get install certbot

# Obtain certificates
sudo certbot certonly --standalone -d your-domain.com -d www.your-domain.com

# Copy certificates to appropriate directories
sudo cp /etc/letsencrypt/live/your-domain.com/fullchain.pem config/ssl/nginx/server.crt
sudo cp /etc/letsencrypt/live/your-domain.com/privkey.pem config/ssl/nginx/server.key
```

### Option 2: Commercial CA
1. Generate Certificate Signing Requests (CSRs)
2. Submit CSRs to your Certificate Authority
3. Download signed certificates
4. Place certificates in appropriate directories

### Option 3: Internal CA
If using an internal Certificate Authority:
1. Generate CSRs for each service
2. Sign certificates with your internal CA
3. Distribute CA certificate to all clients

## File Permissions

Ensure proper file permissions for security:

```bash
# Set restrictive permissions on private keys
chmod 600 config/ssl/*/server.key
chmod 600 config/ssl/*/redis.key
chmod 600 config/ssl/*/prometheus.key

# Set readable permissions on certificates
chmod 644 config/ssl/*/server.crt
chmod 644 config/ssl/*/ca.crt

# Set directory permissions
chmod 755 config/ssl/*/
```

## Docker Volume Mounts

The certificates are mounted as read-only volumes in the Docker containers:

```yaml
volumes:
  - ./config/ssl/nginx:/etc/ssl/nginx:ro
  - ./config/ssl/keycloak:/opt/keycloak/conf:ro
  # ... other mounts
```

## Certificate Renewal

### Automated Renewal (Let's Encrypt)
Set up a cron job for automatic renewal:

```bash
# Add to crontab
0 12 * * * /usr/bin/certbot renew --quiet --post-hook "docker-compose -f docker-compose.prod.yml restart nginx"
```

### Manual Renewal
1. Generate new certificates
2. Replace old certificates in SSL directories
3. Restart affected services:
   ```bash
   docker-compose -f docker-compose.prod.yml restart nginx keycloak grafana prometheus
   ```

## Security Best Practices

1. **Use Strong Encryption**: Use at least 2048-bit RSA keys or 256-bit ECDSA keys
2. **Regular Rotation**: Rotate certificates regularly (annually or bi-annually)
3. **Secure Storage**: Store private keys securely and limit access
4. **Monitor Expiration**: Set up monitoring for certificate expiration
5. **Use HSTS**: Enable HTTP Strict Transport Security
6. **Perfect Forward Secrecy**: Use ECDHE cipher suites
7. **Certificate Transparency**: Monitor CT logs for unauthorized certificates

## Troubleshooting

### Common Issues

1. **Permission Denied**
   ```bash
   # Fix file permissions
   sudo chown -R $USER:$USER config/ssl/
   chmod -R 755 config/ssl/
   chmod 600 config/ssl/*/server.key
   ```

2. **Certificate Verification Failed**
   ```bash
   # Verify certificate
   openssl x509 -in config/ssl/nginx/server.crt -text -noout

   # Check certificate chain
   openssl verify -CAfile config/ssl/nginx/ca.crt config/ssl/nginx/server.crt
   ```

3. **TLS Handshake Errors**
   - Check certificate validity dates
   - Verify certificate matches hostname
   - Ensure proper cipher suite configuration

### Testing SSL Configuration

```bash
# Test SSL certificate
openssl s_client -connect your-domain.com:443 -servername your-domain.com

# Test with specific protocol
openssl s_client -connect your-domain.com:443 -tls1_2

# Check certificate expiration
openssl x509 -in config/ssl/nginx/server.crt -noout -dates
```

## Support

For certificate-related issues:
1. Check service logs: `docker-compose -f docker-compose.prod.yml logs [service-name]`
2. Verify certificate files exist and have correct permissions
3. Test SSL configuration with OpenSSL tools
4. Consult service-specific SSL documentation
