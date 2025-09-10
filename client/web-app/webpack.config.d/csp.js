// Content Security Policy configuration for development
config.devServer = config.devServer || {};
config.devServer.headers = config.devServer.headers || {};
config.devServer.headers['Content-Security-Policy'] =
    "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; connect-src *; img-src 'self' data:; font-src 'self' data:; frame-src 'self';";
