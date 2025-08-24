// Test-specific webpack optimization configuration
// This reduces warnings for test bundles which naturally include more dependencies

// Only apply test optimizations for test builds
if (config.name && config.name.includes('test')) {
    // Relax performance budgets for test builds
    config.performance = {
        hints: false, // Disable size warnings for tests
        maxAssetSize: 15000000, // 15MB for test bundles
        maxEntrypointSize: 15000000,
        assetFilter: function(assetFilename) {
            return false; // Don't check test files
        }
    };

    // Test-specific optimizations
    config.optimization = {
        ...config.optimization,

        // Less aggressive splitting for tests (faster build)
        splitChunks: {
            chunks: 'all',
            minSize: 100000, // 100KB minimum for test chunks
            maxSize: 2000000, // 2MB max size for test chunks
            cacheGroups: {
                // Single vendor chunk for all dependencies
                testVendors: {
                    test: /[\\/]node_modules[\\/]/,
                    name: 'test-vendors',
                    chunks: 'all',
                    enforce: true,
                    priority: 20
                },
                // Single chunk for all Kotlin libraries
                testKotlin: {
                    test: /kotlin/,
                    name: 'test-kotlin',
                    chunks: 'all',
                    enforce: true,
                    priority: 10
                },
                // Default test chunk
                testDefault: {
                    name: 'test-common',
                    minChunks: 2,
                    chunks: 'all',
                    priority: 5
                }
            }
        },

        // Disable some optimizations for faster test builds
        minimize: false, // Don't minify test bundles
        concatenateModules: false // Disable for faster builds
    };

    // Test-specific webpack optimization applied (silent)
} else {
    // For production builds, apply stricter size limits for non-test files
    if (config.mode === 'production') {
        // Override performance settings for production
        config.performance = config.performance || {};
        config.performance.hints = 'error'; // Make size violations errors in production
    }
}

// Additional test environment detection
const isTestEnvironment = process.env.NODE_ENV === 'test' ||
                          process.env.KARMA_ENV === 'true' ||
                          config.target === 'web' && config.mode === 'development';

if (isTestEnvironment) {
    // Disable source maps for test builds to reduce size
    config.devtool = false;

    // Optimize for faster compilation rather than smaller bundles
    config.optimization = config.optimization || {};
    config.optimization.removeAvailableModules = false;
    config.optimization.removeEmptyChunks = false;
    config.optimization.splitChunks = false; // Disable splitting for tests

    // Fast test build configuration applied (silent)
}
