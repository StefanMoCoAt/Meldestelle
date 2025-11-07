#!/bin/bash
# ===================================================================
# Keycloak Setup Script für Meldestelle Projekt
# ===================================================================

set -e

# Konfiguration
KEYCLOAK_URL=${KEYCLOAK_URL:-"http://localhost:8180"}
# Support both new KC_BOOTSTRAP_* (Keycloak 26+) and legacy KEYCLOAK_* env vars
ADMIN_USER=${KC_BOOTSTRAP_ADMIN_USERNAME:-${KEYCLOAK_ADMIN:-"admin"}}
ADMIN_PASSWORD=${KC_BOOTSTRAP_ADMIN_PASSWORD:-${KEYCLOAK_ADMIN_PASSWORD:-"admin"}}
REALM_NAME="meldestelle"

echo "🚀 Starting Keycloak setup for Meldestelle..."

# Warte auf Keycloak
echo "⏳ Waiting for Keycloak to be ready..."
timeout=60
counter=0
while ! curl -f "$KEYCLOAK_URL/health/ready" >/dev/null 2>&1; do
    if [ $counter -eq $timeout ]; then
        echo "❌ Keycloak is not ready after $timeout seconds"
        exit 1
    fi
    echo "   Waiting... ($counter/$timeout)"
    sleep 1
    counter=$((counter + 1))
done

echo "✅ Keycloak is ready!"

# Obtain admin token
echo "🔐 Obtaining admin token..."
ADMIN_TOKEN=$(curl -s \
    -d "client_id=admin-cli" \
    -d "username=$ADMIN_USER" \
    -d "password=$ADMIN_PASSWORD" \
    -d "grant_type=password" \
    "$KEYCLOAK_URL/realms/master/protocol/openid-connect/token" | \
    jq -r '.access_token')

if [ "$ADMIN_TOKEN" = "null" ] || [ -z "$ADMIN_TOKEN" ]; then
    echo "❌ Failed to obtain admin token"
    exit 1
fi

echo "✅ Admin token obtained"

# Check if realm exists
echo "🔍 Checking if realm '$REALM_NAME' exists..."
REALM_EXISTS=$(curl -s \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    "$KEYCLOAK_URL/admin/realms/$REALM_NAME" \
    -w "%{http_code}" -o /dev/null)

if [ "$REALM_EXISTS" = "200" ]; then
    echo "✅ Realm '$REALM_NAME' already exists"
else
    echo "❌ Realm '$REALM_NAME' not found (HTTP $REALM_EXISTS)"
    echo "💡 Please import the realm configuration manually or check the import volume"
fi

# Verify API Gateway client
echo "🔍 Checking API Gateway client..."
CLIENT_EXISTS=$(curl -s \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    "$KEYCLOAK_URL/admin/realms/$REALM_NAME/clients?clientId=api-gateway" | \
    jq '. | length')

if [ "$CLIENT_EXISTS" -gt "0" ]; then
    echo "✅ API Gateway client exists"
else
    echo "⚠️  API Gateway client not found"
fi

# Test realm endpoints
echo "🧪 Testing realm endpoints..."
curl -s "$KEYCLOAK_URL/realms/$REALM_NAME/.well-known/openid_configuration" > /dev/null && \
    echo "✅ OpenID configuration accessible" || \
    echo "❌ OpenID configuration not accessible"

curl -s "$KEYCLOAK_URL/realms/$REALM_NAME/protocol/openid-connect/certs" > /dev/null && \
    echo "✅ JWK Set accessible" || \
    echo "❌ JWK Set not accessible"

echo ""
echo "🎉 Keycloak setup check completed!"
echo "📝 Summary:"
echo "   - Keycloak URL: $KEYCLOAK_URL"
echo "   - Realm: $REALM_NAME"
echo "   - Admin Console: $KEYCLOAK_URL/admin/"
echo ""
echo "🔗 Next steps:"
echo "   1. Visit the admin console: $KEYCLOAK_URL/admin/"
echo "   2. Login with: $ADMIN_USER / $ADMIN_PASSWORD"
echo "   3. Verify realm configuration"
echo "   4. Test authentication flow"
