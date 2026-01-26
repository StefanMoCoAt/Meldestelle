// Webpack configuration for SQLite WASM support AND Skiko fixes
const CopyWebpackPlugin = require('copy-webpack-plugin');
const webpack = require('webpack');
const path = require('path');
const fs = require('fs');

config.resolve = config.resolve || {};
config.resolve.fallback = config.resolve.fallback || {};
config.resolve.alias = config.resolve.alias || {};

// 1. Fallbacks for Node.js core modules
config.resolve.fallback.fs = false;
config.resolve.fallback.path = false;
config.resolve.fallback.crypto = false;

// 2. Resolve sqlite3 paths
let sqliteBaseDir;
try {
  const packagePath = path.dirname(require.resolve('@sqlite.org/sqlite-wasm/package.json'));
  sqliteBaseDir = path.join(packagePath, 'sqlite-wasm/jswasm');
} catch (e) {
  console.warn("Could not resolve @sqlite.org/sqlite-wasm path automatically. Using fallback path.");
  sqliteBaseDir = path.resolve(__dirname, '../../../../../../node_modules/@sqlite.org/sqlite-wasm/sqlite-wasm/jswasm');
}

// 3. Copy ALL sqlite3 assets (wasm, js, and auxiliary workers)
if (fs.existsSync(sqliteBaseDir)) {
  console.log("Copying sqlite3 assets from:", sqliteBaseDir);
  config.plugins.push(
    new CopyWebpackPlugin({
      patterns: [
        {
          from: sqliteBaseDir,
          to: '.', // Copy to root of dist
          globOptions: {
            ignore: ['**/package.json'] // Don't copy package.json if present
          },
          noErrorOnMissing: true
        }
      ]
    })
  );
} else {
  console.error("ERROR: sqlite3 base directory does not exist:", sqliteBaseDir);
}

// 4. Alias sqlite3.wasm (still needed for some internal checks maybe)
const sqliteWasmPath = path.join(sqliteBaseDir, 'sqlite3.wasm');
config.resolve.alias['sqlite3.wasm'] = sqliteWasmPath;
config.resolve.alias['./sqlite3.wasm'] = sqliteWasmPath;

// 5. Handle .wasm files
config.experiments = config.experiments || {};
config.experiments.asyncWebAssembly = true;

config.module = config.module || {};
config.module.rules = config.module.rules || [];

// Treat Skiko WASM as resource to avoid parsing errors
config.module.rules.push({
  test: /skiko\.wasm$/,
  type: 'asset/resource'
});

// Treat other WASM as async (default)
config.module.rules.push({
  test: /\.wasm$/,
  exclude: /skiko\.wasm$/,
  type: 'webassembly/async'
});

// 6. Ignore warnings
config.ignoreWarnings = config.ignoreWarnings || [];
config.ignoreWarnings.push(/Critical dependency: the request of a dependency is an expression/);

// 7. Fix for "webpackEmptyContext" in sqlite3.mjs
config.plugins.push(
  new webpack.ContextReplacementPlugin(
    /@sqlite\.org\/sqlite-wasm/,
    (data) => {
      delete data.dependencies;
      return data;
    }
  )
);

// 8. MIME types
config.devServer = config.devServer || {};
config.devServer.devMiddleware = config.devServer.devMiddleware || {};
config.devServer.devMiddleware.mimeTypes = {
  'application/wasm': ['wasm'],
  'application/javascript': ['js']
};
