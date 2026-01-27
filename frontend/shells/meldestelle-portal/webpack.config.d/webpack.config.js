// HTML template will be handled by Kotlin/JS build system
// No need for custom HtmlWebpackPlugin configuration

// Bundle-Analyze für Development (optional, only if package is available)
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
// Diese Datei enthält nur ein Script-Tag zu "web-app.js" und wird NICHT
// vom HtmlWebpackPlugin generiert. Zusätzliche Chunks (z. B. vendor/runtime)
// würden dann nicht automatisch injiziert und führen dazu, dass die App nicht startet
// (Bildschirm bleibt auf "Loading ...").
//
// Daher überschreiben wir config.optimization NICHT mehr mit splitChunks.
// Wenn später Chunking gewünscht ist, muss die index.html durch das generierte
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
        // WICHTIG: pathRewrite entfernt /api, wenn das Backend unter /api lauscht,
        // ist das falsch. Wenn das Backend unter / lauscht, ist es richtig.
        // Das API Gateway lauscht unter http://localhost:8081/api/...
        // Wenn wir also /api/ping aufrufen, soll es zu http://localhost:8081/api/ping gehen.
        // Daher KEIN pathRewrite, wenn das Gateway selbst /api erwartet.
        // Wenn das Gateway aber die Routen ohne /api mappt (z.B. /ping), dann brauchen wir Rewrite.
        //
        // Analyse:
        // Gateway Routes sind oft: /api/ping -> Ping Service /api/ping oder /ping
        // Wenn Gateway Routes definiert sind als:
        // - id: ping-service
        //   uri: lb://ping-service
        //   predicates:
        //     - Path=/api/ping/**
        //
        // Dann leitet das Gateway /api/ping weiter.
        // Wenn wir pathRewrite machen, kommt beim Gateway nur /ping an.
        // Das Gateway matcht aber auf /api/ping.
        // Also: pathRewrite entfernen!
        // pathRewrite: {'^/api': ''}
      }
    ]
  }
}
