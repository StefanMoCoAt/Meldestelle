// Fix für sql.js unter Webpack 5
config.resolve = config.resolve || {};
config.resolve.fallback = config.resolve.fallback || {};
config.resolve.fallback.fs = false;
config.resolve.fallback.path = false;
config.resolve.fallback.crypto = false;
config.resolve.fallback.os = false;
config.resolve.fallback.stream = false;
config.resolve.fallback.buffer = false;
