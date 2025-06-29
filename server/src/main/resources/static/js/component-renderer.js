/**
 * Component renderer module
 * Handles dynamic rendering of page components based on configuration
 */
class ComponentRenderer {
    constructor() {
        this.config = null;
    }

    /**
     * Initialize renderer with configuration
     * @param {Object} config - Site configuration
     */
    init(config) {
        this.config = config;
    }

    /**
     * Render the header section
     */
    renderHeader() {
        const header = document.querySelector('.header');
        if (!header || !this.config.site) return;

        header.innerHTML = `
            <h1>${this.config.site.logo} ${this.config.site.title.split(' - ')[0]}</h1>
            <p>${this.config.site.description}</p>
        `;
    }

    /**
     * Render the status section
     */
    renderStatus() {
        const statusElement = document.querySelector('.status');
        if (!statusElement || !this.config.site.status) return;

        statusElement.innerHTML = `
            <strong>System Status:</strong> ${this.config.site.status.message}
        `;
    }

    /**
     * Render feature cards
     */
    renderFeatures() {
        const featuresContainer = document.querySelector('.features');
        if (!featuresContainer || !this.config.features) return;

        featuresContainer.innerHTML = this.config.features.map(feature => `
            <div class="feature-card" data-feature-id="${feature.id}">
                <h3>${feature.icon} ${feature.title}</h3>
                <ul>
                    ${feature.items.map(item => `<li>${item}</li>`).join('')}
                </ul>
            </div>
        `).join('');
    }

    /**
     * Render API section
     */
    renderApiSection() {
        const apiSection = document.querySelector('.api-section');
        if (!apiSection || !this.config.api) return;

        const endpointsHtml = this.config.api.endpoints.map(endpoint => `
            <div class="endpoint">
                <span class="endpoint-method">${endpoint.method}</span>
                <span class="endpoint-path">${endpoint.path}</span>
                <div>${endpoint.description}</div>
            </div>
        `).join('');

        apiSection.innerHTML = `
            <h2>${this.config.api.title}</h2>
            <div class="api-endpoints">
                ${endpointsHtml}
            </div>
            <p style="margin-top: 20px; font-style: italic;">
                ${this.config.api.documentation.text}
                <a href="${this.config.api.documentation.link}" style="color: var(--primary-color);">${this.config.api.documentation.link}</a>
            </p>
        `;
    }

    /**
     * Render footer section
     */
    renderFooter() {
        const footer = document.querySelector('.footer');
        if (!footer || !this.config.footer) return;

        footer.innerHTML = `
            <p>${this.config.footer.copyright}</p>
            <p>${this.config.footer.technology}</p>
        `;
    }

    /**
     * Update page title
     */
    updatePageTitle() {
        if (this.config.site && this.config.site.title) {
            document.title = this.config.site.title;
        }
    }

    /**
     * Render all components
     */
    renderAll() {
        if (!this.config) {
            console.warn('No configuration available for rendering');
            return;
        }

        this.updatePageTitle();
        this.renderHeader();
        this.renderStatus();
        this.renderFeatures();
        this.renderApiSection();
        this.renderFooter();
    }

    /**
     * Add feature card click handlers for extensibility
     */
    addInteractivity() {
        const featureCards = document.querySelectorAll('.feature-card');
        featureCards.forEach(card => {
            card.addEventListener('click', (e) => {
                const featureId = card.getAttribute('data-feature-id');
                this.onFeatureCardClick(featureId, card);
            });
        });
    }

    /**
     * Handle feature card clicks (extensible)
     * @param {string} featureId - Feature identifier
     * @param {HTMLElement} cardElement - Card element
     */
    onFeatureCardClick(featureId, cardElement) {
        // Add visual feedback
        cardElement.style.transform = 'scale(0.98)';
        setTimeout(() => {
            cardElement.style.transform = '';
        }, 150);

        // Emit custom event for extensibility
        const event = new CustomEvent('featureCardClick', {
            detail: { featureId, cardElement }
        });
        document.dispatchEvent(event);

        console.log(`Feature card clicked: ${featureId}`);
    }
}

// Export singleton instance
window.componentRenderer = new ComponentRenderer();
