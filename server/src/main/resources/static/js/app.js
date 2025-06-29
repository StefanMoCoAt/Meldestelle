/**
 * Main application script
 * Coordinates configuration loading and component rendering
 */
class MeldestelleApp {
    constructor() {
        this.initialized = false;
        this.config = null;
    }

    /**
     * Initialize the application
     */
    async init() {
        if (this.initialized) {
            console.warn('Application already initialized');
            return;
        }

        try {
            console.log('Initializing Meldestelle application...');

            // Load configuration
            this.config = await window.configLoader.loadConfig();
            console.log('Configuration loaded:', this.config);

            // Initialize component renderer
            window.componentRenderer.init(this.config);

            // Render all components
            window.componentRenderer.renderAll();

            // Add interactivity
            window.componentRenderer.addInteractivity();

            // Set up event listeners
            this.setupEventListeners();

            this.initialized = true;
            console.log('Meldestelle application initialized successfully');

            // Emit initialization complete event
            this.emitEvent('appInitialized', { config: this.config });

        } catch (error) {
            console.error('Failed to initialize application:', error);
            this.handleInitializationError(error);
        }
    }

    /**
     * Set up global event listeners
     */
    setupEventListeners() {
        // Listen for feature card clicks
        document.addEventListener('featureCardClick', (event) => {
            this.handleFeatureCardClick(event.detail);
        });

        // Listen for configuration changes (for future extensibility)
        document.addEventListener('configChanged', (event) => {
            this.handleConfigChange(event.detail);
        });

        // Handle window resize for responsive behavior
        window.addEventListener('resize', this.debounce(() => {
            this.handleResize();
        }, 250));
    }

    /**
     * Handle feature card clicks
     * @param {Object} detail - Event detail with featureId and cardElement
     */
    handleFeatureCardClick(detail) {
        const { featureId, cardElement } = detail;

        // Example extensible behavior - could be configured
        switch (featureId) {
            case 'persons':
                console.log('Persons management clicked - could navigate to /api/persons');
                break;
            case 'clubs':
                console.log('Clubs management clicked - could navigate to /api/vereine');
                break;
            case 'horses':
                console.log('Horses management clicked - could navigate to horses section');
                break;
            case 'tournaments':
                console.log('Tournaments management clicked - could navigate to tournaments section');
                break;
            default:
                console.log(`Unknown feature clicked: ${featureId}`);
        }
    }

    /**
     * Handle configuration changes (for future extensibility)
     * @param {Object} newConfig - New configuration
     */
    handleConfigChange(newConfig) {
        console.log('Configuration changed, re-rendering...');
        this.config = newConfig;
        window.componentRenderer.init(newConfig);
        window.componentRenderer.renderAll();
        window.componentRenderer.addInteractivity();
    }

    /**
     * Handle window resize
     */
    handleResize() {
        // Could implement responsive behavior adjustments here
        console.log('Window resized');
    }

    /**
     * Handle initialization errors
     * @param {Error} error - Initialization error
     */
    handleInitializationError(error) {
        // Show user-friendly error message
        const container = document.querySelector('.container');
        if (container) {
            container.innerHTML = `
                <div style="text-align: center; color: white; padding: 40px;">
                    <h1>⚠️ Fehler beim Laden</h1>
                    <p>Die Anwendung konnte nicht geladen werden.</p>
                    <p style="font-size: 0.9em; opacity: 0.8;">Bitte versuchen Sie es später erneut oder kontaktieren Sie den Administrator.</p>
                    <button onclick="location.reload()" style="margin-top: 20px; padding: 10px 20px; background: white; border: none; border-radius: 5px; cursor: pointer;">
                        Seite neu laden
                    </button>
                </div>
            `;
        }
    }

    /**
     * Emit custom events
     * @param {string} eventName - Event name
     * @param {Object} detail - Event detail
     */
    emitEvent(eventName, detail = {}) {
        const event = new CustomEvent(eventName, { detail });
        document.dispatchEvent(event);
    }

    /**
     * Debounce utility function
     * @param {Function} func - Function to debounce
     * @param {number} wait - Wait time in milliseconds
     * @returns {Function} Debounced function
     */
    debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }

    /**
     * Get current configuration
     * @returns {Object|null} Current configuration
     */
    getConfig() {
        return this.config;
    }

    /**
     * Check if application is initialized
     * @returns {boolean} Initialization status
     */
    isInitialized() {
        return this.initialized;
    }
}

// Create global app instance
window.meldestelleApp = new MeldestelleApp();

// Initialize when DOM is ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        window.meldestelleApp.init();
    });
} else {
    // DOM is already ready
    window.meldestelleApp.init();
}
