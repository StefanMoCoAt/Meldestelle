/**
 * Configuration loader module
 * Handles loading and caching of site configuration
 */
class ConfigLoader {
    constructor() {
        this.config = null;
        this.loaded = false;
    }

    /**
     * Load configuration from JSON file
     * @returns {Promise<Object>} Site configuration
     */
    async loadConfig() {
        if (this.loaded && this.config) {
            return this.config;
        }

        try {
            const response = await fetch('/config/site-config.json');
            if (!response.ok) {
                throw new Error(`Failed to load config: ${response.status}`);
            }

            this.config = await response.json();
            this.loaded = true;
            return this.config;
        } catch (error) {
            console.error('Error loading configuration:', error);
            // Return fallback configuration
            return this.getFallbackConfig();
        }
    }

    /**
     * Get fallback configuration if loading fails
     * @returns {Object} Fallback configuration
     */
    getFallbackConfig() {
        return {
            site: {
                title: "Meldestelle - Österreichisches Pferdesport Management System",
                description: "Österreichisches Pferdesport Management System",
                logo: "🐎",
                status: {
                    message: "Online und betriebsbereit",
                    type: "success"
                }
            },
            features: [],
            api: {
                title: "🔗 API Endpunkte",
                endpoints: [],
                documentation: {
                    text: "API-Dokumentation",
                    link: "/docs/API_Documentation.md"
                }
            },
            footer: {
                copyright: "© 2024 Meldestelle",
                technology: "Entwickelt mit Kotlin Multiplatform & Ktor"
            }
        };
    }

    /**
     * Get specific configuration section
     * @param {string} section - Configuration section name
     * @returns {*} Configuration section or null
     */
    getSection(section) {
        return this.config ? this.config[section] : null;
    }
}

// Export singleton instance
window.configLoader = new ConfigLoader();
