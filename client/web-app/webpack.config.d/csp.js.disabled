// Content Security Policy configuration for development
// More relaxed CSP suitable for development environment
config.devServer = config.devServer || {};
config.devServer.headers = config.devServer.headers || {};
config.devServer.headers['Content-Security-Policy'] =
    "default-src 'self' 'unsafe-inline' 'unsafe-eval' data: blob:; " +
    "script-src 'self' 'unsafe-inline' 'unsafe-eval' data: blob:; " +
    "style-src 'self' 'unsafe-inline' data: blob:; " +
    "img-src 'self' data: blob: http: https:; " +
    "font-src 'self' data: blob: http: https:; " +
    "connect-src 'self' ws: wss: http: https:; " +
    "frame-src 'self' data: blob:; " +
    "object-src 'none'; " +
    "base-uri 'self';";
