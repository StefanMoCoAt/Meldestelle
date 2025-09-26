const HtmlWebpackPlugin = require('html-webpack-plugin');
const path = require('path');

// Template-Pfad für deine index.html
const templatePath = path.resolve(__dirname, '../../../../clients/app/src/jsMain/resources/index.html');

// Erweitere die bestehende Kotlin/JS Webpack-Konfiguration
config.plugins.push(new HtmlWebpackPlugin({
    template: templatePath,
    filename: 'index.html',
    inject: 'body',
    // Optimierung hinzufügen
    minify: {
        removeComments: true,
        collapseWhitespace: true,
        removeRedundantAttributes: true,
        removeEmptyAttributes: true,
        useShortDoctype: true,
        removeStyleLinkTypeAttributes: true,
        keepClosingSlash: true,
        minifyJS: true,
        minifyCSS: true,
        minifyURLs: true,
    }
}));

// Bundle-Analyse für Development
if (process.env.ANALYZE_BUNDLE === 'true') {
    const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;
    config.plugins.push(new BundleAnalyzerPlugin({
        analyzerMode: 'static',
        openAnalyzer: false,
        reportFilename: 'bundle-report.html'
    }));
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
