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

// Hinweis: Wir liefern eine statische index.html aus src/jsMain/resources aus.
// Diese Datei enthält nur einen Script-Tag zu "web-app.js" und wird NICHT
// vom HtmlWebpackPlugin generiert. Zusätzliche Chunks (z. B. vendor/runtime)
// würden dann nicht automatisch injiziert und führen dazu, dass die App nicht startet
// (Bildschirm bleibt auf "Loading...").
//
// Daher überschreiben wir config.optimization NICHT mehr mit splitChunks.
// Wenn später Chunking gewünscht ist, muss die index.html durch die generierte
// HTML ersetzt oder die zusätzlichen Chunks manuell eingebunden werden.
//
// (Frühere splitChunks-Konfiguration wurde bewusst entfernt.)

// Development Server Konfiguration erweitern
if (config.devServer) {
  config.devServer = {
    ...config.devServer,
    historyApiFallback: true,
    hot: true,
    // API Proxy für Backend-Anfragen (Array-Format für modernen Webpack)
    proxy: [
      {
        context: ['/api'],
        target: 'http://localhost:8081',
        changeOrigin: true,
        secure: false,
        pathRewrite: {'^/api': ''}
      }
    ]
  }
}
