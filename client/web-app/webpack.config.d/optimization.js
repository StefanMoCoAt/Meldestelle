const path = require('path');

// Webpack optimization configuration for production builds
config.optimization = {
    // Enable tree shaking and dead code elimination
    usedExports: true,
    sideEffects: false,

    // Code splitting configuration optimized for Kotlin/JS
    splitChunks: {
        chunks: 'all',
        minSize: 30000,
        maxSize: 300000,
        maxInitialRequests: 8, // Allow more initial requests for better caching
        maxAsyncRequests: 15,
        cacheGroups: {
            // Kotlin standard library - separate chunk
            kotlinStdlib: {
                test: /kotlin-kotlin-stdlib/,
                name: 'kotlin-stdlib',
                chunks: 'all',
                enforce: true,
                priority: 30,
                reuseExistingChunk: true,
            },
            // Coroutines - separate chunk
            coroutines: {
                test: /kotlinx-coroutines/,
                name: 'coroutines',
                chunks: 'all',
                enforce: true,
                priority: 25,
                reuseExistingChunk: true,
            },
            // Compose runtime - separate chunk
            composeRuntime: {
                test: /compose.*runtime/,
                name: 'compose-runtime',
                chunks: 'all',
                enforce: true,
                priority: 20,
                reuseExistingChunk: true,
            },
            // Large vendor libraries
            largeVendors: {
                test: /ktor|androidx-collection|kotlinx-serialization/,
                name: 'large-vendors',
                chunks: 'all',
                enforce: true,
                priority: 15,
                reuseExistingChunk: true,
            },
            // Common vendors
            vendors: {
                test: /[\\/]kotlin[\\/]/,
                name: 'vendors',
                chunks: 'all',
                priority: 10,
                reuseExistingChunk: true,
            },
            // Application code
            default: {
                minChunks: 2,
                priority: -10,
                reuseExistingChunk: true,
            },
        },
    },

    // Minimize bundle size
    minimize: true,
    minimizer: [
        // Use default TerserPlugin for JS minification
        '...',
    ],

    // Runtime chunk optimization
    runtimeChunk: {
        name: 'runtime',
    },
};

// Performance budget adjusted for Kotlin/JS applications
// Note: Kotlin/JS apps require all dependencies loaded initially, so larger budgets are realistic
config.performance = {
    maxAssetSize: 400000, // 400KB per asset (realistic for Kotlin libs)
    maxEntrypointSize: 2000000, // 2MB total entry point (realistic for Kotlin/JS + Compose)
    hints: 'warning',
    assetFilter: function(assetFilename) {
        // Only check JS files for performance
        return assetFilename.endsWith('.js');
    },
};

// Production-specific optimizations
if (config.mode === 'production') {
    // Additional compression and optimizations
    config.optimization.concatenateModules = true;
    config.optimization.providedExports = true;

    // More aggressive code splitting for production
    config.optimization.splitChunks.maxInitialRequests = 10;
    config.optimization.splitChunks.maxAsyncRequests = 10;
}
