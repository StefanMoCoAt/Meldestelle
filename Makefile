# ===================================================================
# Meldestelle Docker Development Makefile
# Optimierte Befehle für containerisierte Entwicklungs-Workflows
# ===================================================================

.PHONY: help dev-up dev-down dev-restart dev-logs build clean test
.PHONY: services-up services-down services-restart services-logs
.PHONY: clients-up clients-down clients-restart clients-logs
.PHONY: prod-up prod-down prod-restart prod-logs
.PHONY: infrastructure-up infrastructure-down infrastructure-logs
.PHONY: full-up full-down full-restart full-logs
.PHONY: dev-tools-up dev-tools-down status health-check logs shell env-setup env-dev env-prod env-staging env-test dev-info clean-all build-service build-client

.ONESHELL:

# Choose docker compose CLI (prefers new plugin)
DOCKER_COMPOSE_PLUGIN := $(shell docker compose version >/dev/null 2>&1 && echo 1 || echo 0)
DOCKER_COMPOSE_LEGACY := $(shell command -v docker-compose >/dev/null 2>&1 && echo 1 || echo 0)
ifeq ($(DOCKER_COMPOSE_PLUGIN),1)
COMPOSE = docker compose
else ifeq ($(DOCKER_COMPOSE_LEGACY),1)
COMPOSE = docker-compose
else
COMPOSE = docker compose
endif

# Default target
.DEFAULT_GOAL := help
help: ## Show this help message
	@echo "Meldestelle Docker Development Commands"
	@echo "======================================"
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

# ===================================================================
# Development Workflow Commands
# ===================================================================

dev-up: ## Start development environment (single compose)
	@echo "🚀 Starting development environment..."
	$(COMPOSE) -f docker-compose.yml up -d
	@$(MAKE) dev-info

dev-down: ## Stop development environment
	@echo "🛑 Stopping development environment..."
	$(COMPOSE) -f docker-compose.yml down

dev-restart: ## Restart full development environment
	@$(MAKE) dev-down
	@$(MAKE) dev-up

dev-logs: ## Show logs for all development services
	$(COMPOSE) -f docker-compose.yml logs -f

# ===================================================================
# Layer-specific Commands
# ===================================================================

infrastructure-up: ## Start only infrastructure services (postgres, redis, keycloak, consul)
	@echo "🏗️ Starting infrastructure services..."
	$(COMPOSE) -f docker-compose.yml up -d
	@echo "✅ Infrastructure services started"
	@echo "🗄️ PostgreSQL:      localhost:5432"
	@echo "🔴 Redis:           localhost:6379"
	@echo "🔐 Keycloak:        http://localhost:8180"
	@echo "🧭 Consul:          http://localhost:8500"

infrastructure-down: ## Stop infrastructure services
	$(COMPOSE) -f docker-compose.yml down

infrastructure-logs: ## Show infrastructure logs
	$(COMPOSE) -f docker-compose.yml logs -f

services-up: ## Start application services (infrastructure + microservices)
	@echo "⚙️ Starting application services..."
	$(COMPOSE) -f docker-compose.yml -f docker-compose.services.yml up -d
	@echo "✅ Application services started"
	@echo "🔗 Gateway:         http://localhost:8081"
	@echo "🏓 Ping Service:    http://localhost:8082"
	@echo "👥 Members Service: http://localhost:8083"
	@echo "🐎 Horses Service:  http://localhost:8084"
	@echo "🎯 Events Service:  http://localhost:8085"
	@echo "📊 Master Service:  http://localhost:8086"

services-down: ## Stop application services
	$(COMPOSE) -f docker-compose.yml -f docker-compose.services.yml down

services-restart: ## Restart application services
	@$(MAKE) services-down
	@$(MAKE) services-up

services-logs: ## Show application services logs
	$(COMPOSE) -f docker-compose.yml -f docker-compose.services.yml logs -f

clients-up: ## Start client applications (infrastructure + clients)
	@echo "💻 Starting client applications..."
	$(COMPOSE) -f docker-compose.yml -f docker-compose.clients.yml up -d
	@echo "✅ Client applications started"
	@echo "🌐 Web App:         http://localhost:4000"
	@echo "🔐 Auth Server:     http://localhost:8087"
	@echo "📈 Monitoring:      http://localhost:8088"

clients-down: ## Stop client applications
	$(COMPOSE) -f docker-compose.yml -f docker-compose.clients.yml down

clients-restart: ## Restart client applications
	@$(MAKE) clients-down
	@$(MAKE) clients-up

clients-logs: ## Show client application logs
	$(COMPOSE) -f docker-compose.yml -f docker-compose.clients.yml logs -f

# ===================================================================
# Full System Commands
# ===================================================================

full-up: ## Start complete system (infrastructure + services + clients)
	@echo "🚀 Starting complete Meldestelle system..."
	$(COMPOSE) -f docker-compose.yml -f docker-compose.services.yml -f docker-compose.clients.yml up -d
	@echo "✅ Complete system started"
	@echo ""
	@echo "🌐 Frontend & APIs:"
	@echo "   Web App:         http://localhost:4000"
	@echo "   API Gateway:     http://localhost:8081"
	@echo ""
	@echo "🔧 Infrastructure:"
	@echo "   PostgreSQL:      localhost:5432"
	@echo "   Redis:           localhost:6379"
	@echo "   Keycloak:        http://localhost:8180"
	@echo "   Consul:          http://localhost:8500"
	@echo "   Prometheus:      http://localhost:9090"
	@echo "   Grafana:         http://localhost:3000"
	@echo ""
	@echo "⚙️  Microservices:"
	@echo "   Ping Service:    http://localhost:8082"
	@echo "   Members Service: http://localhost:8083"
	@echo "   Horses Service:  http://localhost:8084"
	@echo "   Events Service:  http://localhost:8085"
	@echo "   Master Service:  http://localhost:8086"
	@echo "   Auth Server:     http://localhost:8087"
	@echo "   Monitoring:      http://localhost:8088"

full-down: ## Stop complete system
	$(COMPOSE) -f docker-compose.yml -f docker-compose.services.yml -f docker-compose.clients.yml down

full-restart: ## Restart complete system
	@$(MAKE) full-down
	@$(MAKE) full-up

full-logs: ## Show all system logs
	$(COMPOSE) -f docker-compose.yml -f docker-compose.services.yml -f docker-compose.clients.yml logs -f

# ===================================================================
# Environment Configuration Commands
# ===================================================================

env-setup: ## Show environment setup instructions
	@echo "🔧 Umgebungskonfiguration - Environment Setup"
	@echo "=============================================="
	@echo ""
	@echo "Verfügbare Umgebungen:"
	@echo "  make env-dev      - Entwicklungsumgebung"
	@echo "  make env-prod     - Produktionsumgebung"
	@echo "  make env-staging  - Staging-Umgebung"
	@echo "  make env-test     - Testumgebung"
	@echo ""
	@echo "Aktuelle Konfiguration:"
	@ls -la .env 2>/dev/null || echo "  Keine .env Datei gefunden - führe 'make env-dev' aus"

env-dev: ## Switch to development environment
	@echo "🔧 Switching to development environment..."
	@ln -sf config/.env.dev .env
	@echo "✅ Development environment activated (.env -> config/.env.dev)"
	@echo "Debug mode: enabled, CORS: permissive, Logging: verbose"

env-prod: ## Switch to production environment
	@echo "🔧 Switching to production environment..."
	@ln -sf config/.env.prod .env
	@echo "✅ Production environment activated (.env -> config/.env.prod)"
	@echo "⚠️  WICHTIG: Überprüfen Sie alle CHANGE_ME Werte in .env!"

env-staging: ## Switch to staging environment
	@echo "🔧 Switching to staging environment..."
	@ln -sf config/.env.staging .env
	@echo "✅ Staging environment activated (.env -> config/.env.staging)"
	@echo "Production-like settings with moderate resources"

env-test: ## Switch to test environment
	@echo "🔧 Switching to test environment..."
	@ln -sf config/.env.test .env
	@echo "✅ Test environment activated (.env -> config/.env.test)"
	@echo "Optimized for automated testing with alternative ports"

# ===================================================================
# Production Commands
# ===================================================================

prod-up: ## Start production environment
	@echo "🚀 Starting production environment..."
	@echo "⚠️  Make sure environment variables are properly set!"
	@if [ ! -f .env ]; then echo "❌ No .env file found! Run 'make env-prod' first."; exit 1; fi
	$(COMPOSE) -f docker-compose.yml -f docker-compose.services.yml up -d
	@echo "✅ Production environment started"

prod-down: ## Stop production environment
	$(COMPOSE) -f docker-compose.yml -f docker-compose.services.yml down

prod-restart: ## Restart production environment
	@$(MAKE) prod-down
	@$(MAKE) prod-up

prod-logs: ## Show production logs (simplified)
	$(COMPOSE) -f docker-compose.yml logs -f

# ===================================================================
# Development Tools
# ===================================================================

dev-tools-up: ## Info: development tool containers were removed (use local tools instead)
	@echo "ℹ️ Development tool containers are not part of the simplified setup."
	@echo "Use your local tools instead (e.g., pgAdmin, TablePlus, DBeaver, RedisInsight)."
	@echo "Connection hints:"
	@echo "  PostgreSQL: localhost:5432 (user/password per .env or defaults)"
	@echo "  Redis:      localhost:6379"
	@echo "  Consul:     http://localhost:8500"
	@echo "  Keycloak:   http://localhost:8180"

dev-tools-down: ## Info: nothing to stop for dev tools in simplified setup
	@echo "ℹ️ No dev-tool containers to stop in the simplified setup."

# ===================================================================
# Build and Maintenance Commands
# ===================================================================

build: ## Build all custom Docker images (simplified)
	@echo "🔨 Building all custom Docker images (using docker-compose.yml)..."
	$(COMPOSE) -f docker-compose.yml build --no-cache

build-service: ## Build specific service (usage: make build-service SERVICE=auth-server)
	@test -n "$(SERVICE)" || (echo "❌ SERVICE parameter required. Usage: make build-service SERVICE=auth-server"; exit 1)
	@echo "🔨 Building $(SERVICE)..."
	$(COMPOSE) -f docker-compose.yml build --no-cache $(SERVICE)

build-client: ## Build specific client (usage: make build-client CLIENT=web-app)
	@test -n "$(CLIENT)" || (echo "❌ CLIENT parameter required. Usage: make build-client CLIENT=web-app"; exit 1)
	@echo "🔨 Building $(CLIENT)..."
	$(COMPOSE) -f docker-compose.yml build --no-cache $(CLIENT)

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
	$(COMPOSE) -f docker-compose.yml ps

health-check: ## Check health of core infrastructure services
	@echo "🏥 Health Check Results:"
	@echo "========================"
	@$(COMPOSE) ps
	@echo "-- Postgres --"
	@$(COMPOSE) exec -T postgres pg_isready -U meldestelle -d meldestelle >/dev/null \
		&& echo "PostgreSQL: ✅ Ready" || echo "PostgreSQL: ❌ Not ready"
	@echo "-- Redis --"
	@$(COMPOSE) exec -T redis redis-cli ping | grep -q PONG \
		&& echo "Redis: ✅ PONG" || echo "Redis: ❌ Not responding"
	@echo "-- Consul --"
	@curl -sf http://localhost:8500/v1/status/leader >/dev/null \
		&& echo "Consul: ✅ Leader elected" || echo "Consul: ❌ Not accessible"
	@echo "-- Keycloak --"
	@curl -sf http://localhost:8180/health/ready >/dev/null \
		&& echo "Keycloak: ✅ Ready" || echo "Keycloak: ❌ Not accessible"

logs: ## Show logs for specific service (usage: make logs SERVICE=postgres)
	@test -n "$(SERVICE)" || (echo "❌ SERVICE parameter required. Usage: make logs SERVICE=postgres"; exit 1)
	$(COMPOSE) logs -f $(SERVICE)

shell: ## Open shell in specific container (usage: make shell SERVICE=postgres)
	@test -n "$(SERVICE)" || (echo "❌ SERVICE parameter required. Usage: make shell SERVICE=postgres"; exit 1)
	$(COMPOSE) exec $(SERVICE) sh

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
	@echo "🧭 Service Discovery:"
	@echo "  Consul:           http://localhost:8500"
	@echo ""
	@echo "🔐 Authentication:"
	@echo "  Keycloak:         http://localhost:8180 (admin/admin by default)"
	@echo ""
	@echo "🗄️ Infrastructure:"
	@echo "  PostgreSQL:       localhost:5432 (default user: meldestelle)"
	@echo "  Redis:            localhost:6379"
	@echo ""
	@echo "ℹ️ Tips: Use 'make health-check' to verify services, and 'make logs SERVICE=postgres' for logs."
	@echo ""

env-template: ## Create .env template file
	@echo "📝 Creating .env template..."
	@cat > .env.template <<-'EOF'
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
