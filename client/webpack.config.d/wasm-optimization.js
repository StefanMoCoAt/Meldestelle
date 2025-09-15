// WASM Bundle Size Optimization Configuration
// Advanced Webpack configuration for smaller WASM bundles

const path = require('path');

// Bundle size optimization configuration
config.optimization = {
    ...config.optimization,
    // Enable aggressive tree shaking
    usedExports: true,
    sideEffects: false,

    // Split chunks for better caching and smaller initial bundle
    splitChunks: {
        chunks: 'all',
        cacheGroups: {
            // Separate Skiko (Compose UI) into its own chunk
            skiko: {
                test: /[\\/]skiko[\\/]/,
                name: 'skiko',
                chunks: 'all',
                priority: 30,
                reuseExistingChunk: true,
                enforce: true
            },
            // Separate Ktor client into its own chunk
            ktor: {
                test: /[\\/]ktor[\\/]/,
                name: 'ktor',
                chunks: 'all',
                priority: 20,
                reuseExistingChunk: true
            },
            // Separate Kotlin stdlib into its own chunk
            kotlinStdlib: {
                test: /[\\/]kotlin[\\/]/,
                name: 'kotlin-stdlib',
                chunks: 'all',
                priority: 15,
                reuseExistingChunk: true
            },
            // Default vendor chunk for remaining dependencies
            vendor: {
                test: /[\\/]node_modules[\\/]/,
                name: 'vendors',
                chunks: 'all',
                priority: 10,
                reuseExistingChunk: true
            },
            // Application code chunk
            default: {
                name: 'app',
                minChunks: 2,
                priority: 5,
                reuseExistingChunk: true
            }
        }
    },

    // Minimize bundle size
    minimize: true
    // Note: minimizer is automatically configured by Kotlin/JS
};

// Performance optimization
config.performance = {
    ...config.performance,
    // Increase hint limits for WASM (which is naturally larger)
    maxAssetSize: 2000000, // 2MB for individual assets
    maxEntrypointSize: 2000000, // 2MB for entrypoints
    hints: 'warning'
};

// Resolve optimization for faster builds
config.resolve = {
    ...config.resolve,
    // Skip looking in these directories to speed up resolution
    modules: ['node_modules'],
    // Cache module resolution
    cache: true
};

// Module optimization
config.module = {
    ...config.module,
    // Disable parsing for known pre-built modules
    noParse: [
        /kotlin\.js$/,
        /kotlinx-.*\.js$/
    ]
};

// Development vs Production optimizations
if (config.mode === 'production') {
    // Production-specific optimizations
    config.output = {
        ...config.output,
        // Better file names for caching
        filename: '[name].[contenthash:8].js',
        chunkFilename: '[name].[contenthash:8].chunk.js'
    };

    // Additional production optimizations
    config.optimization = {
        ...config.optimization,
        // Enable module concatenation (scope hoisting)
        concatenateModules: true,
        // Remove empty chunks
        removeEmptyChunks: true,
        // Merge duplicate chunks
        mergeDuplicateChunks: true
    };
} else {
    // Development optimizations for faster builds
    config.cache = {
        type: 'filesystem',
        buildDependencies: {
            config: [__filename]
        }
    };
}
