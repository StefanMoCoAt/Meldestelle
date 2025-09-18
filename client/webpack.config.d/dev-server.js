// Development server configuration with API proxy
// This forwards API requests from webpack-dev-server to the gateway
const path = require('path');

if (config.mode !== 'production') {
    config.devServer = {
        ...config.devServer,

        // Proxy API requests to the gateway - using modern object syntax
        proxy: {
            '/api/**': {
                target: 'http://localhost:8081',
                changeOrigin: true,
                secure: false,
                logLevel: 'debug',
                pathRewrite: {
                    '^/api': '/api' // Keep the /api prefix for gateway routing
                }
            }
        },

        // Disable all caches as requested in previous issue
        headers: {
            'Cache-Control': 'no-cache, no-store, must-revalidate',
            'Pragma': 'no-cache',
            'Expires': '0'
        },

        // Development middleware settings
        devMiddleware: {
            writeToDisk: false,
            stats: 'minimal'
        },

        // Static files configuration
        static: {
            directory: path.resolve(__dirname, '../../build/dist/wasmJs/developmentExecutable'),
            serveIndex: true,
            watch: true
        },

        // CORS settings for development
        allowedHosts: 'all',
        historyApiFallback: true,
        hot: true,
        liveReload: true
    };
}
