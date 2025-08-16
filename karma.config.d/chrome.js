// Karma configuration for Chrome browser testing
// This file fixes Chrome/Chromium path issues and permission errors

config.set({
    // Use Chrome with custom configuration to avoid snap permission issues
    browsers: ['ChromeHeadlessNoSandbox'],

    // Custom browser configuration
    customLaunchers: {
        ChromeHeadlessNoSandbox: {
            base: 'ChromeHeadless',
            flags: [
                '--no-sandbox',
                '--disable-web-security',
                '--disable-features=VizDisplayCompositor',
                '--disable-dev-shm-usage',
                '--disable-gpu',
                '--remote-debugging-port=9222'
            ]
        },
        ChromeHeadlessCI: {
            base: 'ChromeHeadless',
            flags: [
                '--no-sandbox',
                '--disable-web-security',
                '--disable-features=VizDisplayCompositor',
                '--disable-dev-shm-usage',
                '--disable-gpu',
                '--headless',
                '--disable-extensions',
                '--disable-plugins',
                '--disable-images',
                '--disable-javascript',
                '--disable-default-apps',
                '--disable-translate',
                '--disable-background-timer-throttling',
                '--disable-renderer-backgrounding',
                '--disable-device-discovery-notifications'
            ]
        }
    },

    // Browser detection and fallback
    detectBrowsers: {
        enabled: false // Disable auto-detection to use our custom config
    },

    // Timeout configuration to handle slower CI environments
    browserNoActivityTimeout: 60000,
    browserDisconnectTimeout: 10000,
    browserDisconnectTolerance: 3,

    // Process configuration
    processKillTimeout: 5000,
    captureTimeout: 60000
});

// Try to use system Chrome if snap Chromium fails
if (process.env.CI || process.env.GITHUB_ACTIONS) {
    // Use CI-optimized Chrome configuration in CI environments
    config.browsers = ['ChromeHeadlessCI'];
} else {
    // Use standard no-sandbox configuration for local development
    config.browsers = ['ChromeHeadlessNoSandbox'];
}

console.log('Chrome browser configuration applied for testing');
