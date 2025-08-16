# ===================================================================
# Meldestelle Docker Development Makefile
# Convenient commands for managing containerized development workflow
# ===================================================================

.PHONY: help dev-up dev-down dev-restart dev-logs build clean test
.PHONY: services-up services-down services-restart services-logs
.PHONY: clients-up clients-down clients-restart clients-logs
.PHONY: prod-up prod-down prod-restart prod-logs
.PHONY: infrastructure-up infrastructure-down infrastructure-logs
.PHONY: dev-tools-up dev-tools-down status health-check

# Default target
help: ## Show this help message
	@echo "Meldestelle Docker Development Commands"
	@echo "======================================"
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

# ===================================================================
# Development Workflow Commands
# ===================================================================

dev-up: ## Start full development environment (infrastructure + services + clients)
	@echo "🚀 Starting full development environment..."
	docker-compose \
		-f docker-compose.yml \
		-f docker-compose.services.yml \
		-f docker-compose.clients.yml \
		up -d
	@$(MAKE) dev-info

dev-down: ## Stop full development environment
	@echo "🛑 Stopping full development environment..."
	docker-compose \
		-f docker-compose.yml \
		-f docker-compose.services.yml \
		-f docker-compose.clients.yml \
		down

dev-restart: ## Restart full development environment
	@$(MAKE) dev-down
	@$(MAKE) dev-up

dev-logs: ## Show logs for all development services
	docker-compose \
		-f docker-compose.yml \
		-f docker-compose.services.yml \
		-f docker-compose.clients.yml \
		logs -f

# ===================================================================
# Layer-specific Commands
# ===================================================================

infrastructure-up: ## Start only infrastructure services (postgres, redis, etc.)
	@echo "🏗️ Starting infrastructure services..."
	docker-compose -f docker-compose.yml up -d
	@echo "✅ Infrastructure services started"
	@echo "📊 Grafana: http://localhost:3000 (admin/admin)"
	@echo "🔍 Prometheus: http://localhost:9090"
	@echo "🗄️ PostgreSQL: localhost:5432"
	@echo "🔴 Redis: localhost:6379"

infrastructure-down: ## Stop infrastructure services
	docker-compose -f docker-compose.yml down

infrastructure-logs: ## Show infrastructure logs
	docker-compose -f docker-compose.yml logs -f

services-up: ## Start application services (requires infrastructure)
	@echo "⚙️ Starting application services..."
	docker-compose \
		-f docker-compose.yml \
		-f docker-compose.services.yml \
		up -d
	@echo "✅ Application services started"
	@echo "🚪 API Gateway: http://localhost:8080"
	@echo "🔐 Auth Server: http://localhost:8081"
	@echo "📊 Monitoring Server: http://localhost:8083"
	@echo "🏓 Ping Service: http://localhost:8082"

services-down: ## Stop application services
	docker-compose -f docker-compose.services.yml down

services-restart: ## Restart application services
	@$(MAKE) services-down
	@$(MAKE) services-up

services-logs: ## Show application services logs
	docker-compose -f docker-compose.services.yml logs -f

clients-up: ## Start client applications (requires services)
	@echo "💻 Starting client applications..."
	docker-compose \
		-f docker-compose.yml \
		-f docker-compose.services.yml \
		-f docker-compose.clients.yml \
		up -d
	@echo "✅ Client applications started"
	@echo "🌐 Web App: http://localhost:3001"

clients-down: ## Stop client applications
	docker-compose -f docker-compose.clients.yml down

clients-restart: ## Restart client applications
	@$(MAKE) clients-down
	@$(MAKE) clients-up

clients-logs: ## Show client application logs
	docker-compose -f docker-compose.clients.yml logs -f

# ===================================================================
# Production Commands
# ===================================================================

prod-up: ## Start production environment
	@echo "🚀 Starting production environment..."
	@echo "⚠️ Make sure environment variables are properly set!"
	docker-compose \
		-f docker-compose.prod.yml \
		-f docker-compose.services.yml \
		up -d
	@echo "✅ Production environment started"

prod-down: ## Stop production environment
	docker-compose \
		-f docker-compose.prod.yml \
		-f docker-compose.services.yml \
		down

prod-restart: ## Restart production environment
	@$(MAKE) prod-down
	@$(MAKE) prod-up

prod-logs: ## Show production logs
	docker-compose \
		-f docker-compose.prod.yml \
		-f docker-compose.services.yml \
		logs -f

# ===================================================================
# Development Tools
# ===================================================================

dev-tools-up: ## Start development tools (pgAdmin, Redis Commander)
	@echo "🔧 Starting development tools..."
	docker-compose --profile dev-tools up -d pgadmin redis-commander
	@echo "✅ Development tools started"
	@echo "🐘 pgAdmin: http://localhost:5050 (admin@meldestelle.dev/admin)"
	@echo "🔴 Redis Commander: http://localhost:8081"

dev-tools-down: ## Stop development tools
	docker-compose --profile dev-tools down pgadmin redis-commander

# ===================================================================
# Build and Maintenance Commands
# ===================================================================

build: ## Build all custom Docker images
	@echo "🔨 Building all custom Docker images..."
	docker-compose \
		-f docker-compose.yml \
		-f docker-compose.services.yml \
		-f docker-compose.clients.yml \
		build --no-cache

build-service: ## Build specific service (usage: make build-service SERVICE=auth-server)
	@test -n "$(SERVICE)" || (echo "❌ SERVICE parameter required. Usage: make build-service SERVICE=auth-server"; exit 1)
	@echo "🔨 Building $(SERVICE)..."
	docker-compose \
		-f docker-compose.services.yml \
		build --no-cache $(SERVICE)

build-client: ## Build specific client (usage: make build-client CLIENT=web-app)
	@test -n "$(CLIENT)" || (echo "❌ CLIENT parameter required. Usage: make build-client CLIENT=web-app"; exit 1)
	@echo "🔨 Building $(CLIENT)..."
	docker-compose \
		-f docker-compose.clients.yml \
		build --no-cache $(CLIENT)

clean: ## Clean up Docker resources
	@echo "🧹 Cleaning up Docker resources..."
	docker system prune -f
	docker volume prune -f
	docker network prune -f
	@echo "✅ Cleanup completed"

clean-all: ## Clean up all Docker resources (including images)
	@echo "🧹 Cleaning up all Docker resources..."
	docker system prune -af --volumes
	@echo "✅ Complete cleanup finished"

# ===================================================================
# Monitoring and Debugging Commands
# ===================================================================

status: ## Show status of all containers
	@echo "📊 Container Status:"
	docker-compose \
		-f docker-compose.yml \
		-f docker-compose.services.yml \
		-f docker-compose.clients.yml \
		ps

health-check: ## Check health of all services
	@echo "🏥 Health Check Results:"
	@echo "========================"
	@curl -s http://localhost:8080/actuator/health | jq -r '"API Gateway: " + .status' || echo "API Gateway: ❌ Not accessible"
	@curl -s http://localhost:8081/actuator/health | jq -r '"Auth Server: " + .status' || echo "Auth Server: ❌ Not accessible"
	@curl -s http://localhost:8082/actuator/health | jq -r '"Ping Service: " + .status' || echo "Ping Service: ❌ Not accessible"
	@curl -s http://localhost:8083/actuator/health | jq -r '"Monitoring Server: " + .status' || echo "Monitoring Server: ❌ Not accessible"
	@curl -s http://localhost:3001/health | grep -q healthy && echo "Web App: UP" || echo "Web App: ❌ Not accessible"

logs: ## Show logs for specific service (usage: make logs SERVICE=auth-server)
	@test -n "$(SERVICE)" || (echo "❌ SERVICE parameter required. Usage: make logs SERVICE=auth-server"; exit 1)
	docker-compose logs -f $(SERVICE)

shell: ## Open shell in specific container (usage: make shell SERVICE=auth-server)
	@test -n "$(SERVICE)" || (echo "❌ SERVICE parameter required. Usage: make shell SERVICE=auth-server"; exit 1)
	docker-compose exec $(SERVICE) sh

# ===================================================================
# Testing Commands
# ===================================================================

test: ## Run integration tests
	@echo "🧪 Running integration tests..."
	@$(MAKE) infrastructure-up
	@echo "⏳ Waiting for services to be ready..."
	@sleep 30
	@echo "✅ Running test suite..."
	./gradlew test
	@$(MAKE) infrastructure-down

test-e2e: ## Run end-to-end tests with full environment
	@echo "🧪 Running end-to-end tests..."
	@$(MAKE) dev-up
	@echo "⏳ Waiting for full environment to be ready..."
	@sleep 60
	@echo "✅ Running e2e test suite..."
	./gradlew :client:web-app:jsTest
	@$(MAKE) dev-down

# ===================================================================
# Information and Help
# ===================================================================

dev-info: ## Show development environment information
	@echo ""
	@echo "🚀 Meldestelle Development Environment Ready!"
	@echo "============================================="
	@echo ""
	@echo "📊 Monitoring & Management:"
	@echo "  Grafana:          http://localhost:3000 (admin/admin)"
	@echo "  Prometheus:       http://localhost:9090"
	@echo "  Consul:           http://localhost:8500"
	@echo ""
	@echo "🔧 Application Services:"
	@echo "  API Gateway:      http://localhost:8080"
	@echo "  Auth Server:      http://localhost:8081"
	@echo "  Monitoring:       http://localhost:8083"
	@echo "  Ping Service:     http://localhost:8082"
	@echo ""
	@echo "💻 Client Applications:"
	@echo "  Web App:          http://localhost:3001"
	@echo ""
	@echo "🗄️ Infrastructure:"
	@echo "  PostgreSQL:       localhost:5432 (meldestelle/meldestelle)"
	@echo "  Redis:            localhost:6379"
	@echo "  Keycloak:         http://localhost:8180"
	@echo ""
	@echo "🔧 Development Tools (optional):"
	@echo "  make dev-tools-up to start pgAdmin & Redis Commander"
	@echo ""

env-template: ## Create .env template file
	@echo "📝 Creating .env template..."
	@cat > .env.template << 'EOF'
# ===================================================================
# Meldestelle Environment Variables Template
# Copy to .env and customize for your environment
# ===================================================================

# Database Configuration
POSTGRES_USER=meldestelle
POSTGRES_PASSWORD=meldestelle
POSTGRES_DB=meldestelle

# Redis Configuration
REDIS_PASSWORD=

# Keycloak Configuration
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=admin
KC_DB=postgres
KC_DB_URL=jdbc:postgresql://postgres:5432/keycloak
KC_DB_USERNAME=meldestelle
KC_DB_PASSWORD=meldestelle

# JWT Configuration
JWT_SECRET=meldestelle-auth-secret-key-change-in-production
JWT_EXPIRATION=86400

# Monitoring Configuration
GF_SECURITY_ADMIN_USER=admin
GF_SECURITY_ADMIN_PASSWORD=admin

# Production URLs (for production environment)
KC_HOSTNAME=auth.meldestelle.at
GRAFANA_HOSTNAME=monitor.meldestelle.at
PROMETHEUS_HOSTNAME=metrics.meldestelle.at
EOF
	@echo "✅ .env.template created"
