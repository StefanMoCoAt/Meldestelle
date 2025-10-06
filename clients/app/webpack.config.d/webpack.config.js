// HTML template will be handled by Kotlin/JS build system
// No need for custom HtmlWebpackPlugin configuration

// Bundle-Analyse für Development (optional, only if package is available)
if (process.env.ANALYZE_BUNDLE === 'true') {
    try {
        const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;
        config.plugins.push(new BundleAnalyzerPlugin({
            analyzerMode: 'static',
            openAnalyzer: false,
            reportFilename: 'bundle-report.html'
        }));
        console.log('Bundle analyzer enabled');
    } catch (e) {
        console.log('Bundle analyzer not available (webpack-bundle-analyzer not installed)');
    }
}

// Weitere Optimierungen hinzufügen (erweitert bestehende config)
config.optimization = {
    ...config.optimization, // Behalte Kotlin/JS Optimierungen
    splitChunks: {
        chunks: 'all',
        cacheGroups: {
            vendor: {
                test: /[\\/]node_modules[\\/]/,
                name: 'vendor',
                chunks: 'all'
            }
        }
    }
};

// Development Server Konfiguration erweitern
if (config.devServer) {
    config.devServer = {
        ...config.devServer,
        historyApiFallback: true,
        hot: true,
        // API Proxy für Backend-Anfragen (Array-Format für moderne Webpack)
        proxy: [
            {
                context: ['/api'],
                target: 'http://localhost:8081',
                changeOrigin: true,
                secure: false,
                pathRewrite: { '^/api': '' }
            }
        ]
    }
}
