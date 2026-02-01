// Webpack configuration for SQLite WASM support AND Skiko fixes
const CopyWebpackPlugin = require('copy-webpack-plugin');
const webpack = require('webpack');
const path = require('path');
const fs = require('fs');

console.log("SQLite Config: Current working directory (cwd):", process.cwd());
console.log("SQLite Config: __dirname:", __dirname);

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
const copyPatterns = [];

if (fs.existsSync(sqliteBaseDir)) {
    console.log("Copying sqlite3 assets from:", sqliteBaseDir);
    copyPatterns.push({
        from: sqliteBaseDir,
        to: '.', // Copy to root of dist
        globOptions: {
            ignore: ['**/package.json']
        },
        noErrorOnMissing: true
    });
} else {
    console.error("ERROR: sqlite3 base directory does not exist:", sqliteBaseDir);
}

// 4. Copy sqlite.worker.js from source
// Try multiple strategies to find the file

// Strategy A: Relative to __dirname (webpack.config.d)
// ../../../core/local-db/src/jsMain/resources/sqlite.worker.js
const pathA = path.resolve(__dirname, '../../../core/local-db/src/jsMain/resources/sqlite.worker.js');

// Strategy B: Relative to process.cwd() (project root usually)
// ../../core/local-db/src/jsMain/resources/sqlite.worker.js (assuming cwd is meldestelle-portal)
const pathB = path.resolve(process.cwd(), '../../core/local-db/src/jsMain/resources/sqlite.worker.js');

// Strategy C: Hardcoded fallback based on typical structure
const pathC = path.resolve(__dirname, '../../../../core/local-db/src/jsMain/resources/sqlite.worker.js');

let workerSourcePath = null;

if (fs.existsSync(pathA)) {
    workerSourcePath = pathA;
    console.log("Found sqlite.worker.js at (Strategy A):", pathA);
} else if (fs.existsSync(pathB)) {
    workerSourcePath = pathB;
    console.log("Found sqlite.worker.js at (Strategy B):", pathB);
} else if (fs.existsSync(pathC)) {
    workerSourcePath = pathC;
    console.log("Found sqlite.worker.js at (Strategy C):", pathC);
} else {
    console.error("ERROR: Could not find sqlite.worker.js in any expected location!");
    console.error("Checked A:", pathA);
    console.error("Checked B:", pathB);
    console.error("Checked C:", pathC);
}

if (workerSourcePath) {
    copyPatterns.push({
        from: workerSourcePath,
        to: 'sqlite.worker.js',
        noErrorOnMissing: true
    });
}

config.plugins.push(
    new CopyWebpackPlugin({
        patterns: copyPatterns
    })
);

// 5. Alias sqlite3.wasm (still needed for some internal checks maybe)
const sqliteWasmPath = path.join(sqliteBaseDir, 'sqlite3.wasm');
config.resolve.alias['sqlite3.wasm'] = sqliteWasmPath;
config.resolve.alias['./sqlite3.wasm'] = sqliteWasmPath;

// 6. Handle .wasm files
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

// 7. Ignore warnings
config.ignoreWarnings = config.ignoreWarnings || [];
config.ignoreWarnings.push(/Critical dependency: the request of a dependency is an expression/);

// 8. Fix for "webpackEmptyContext" in sqlite3.mjs
config.plugins.push(
    new webpack.ContextReplacementPlugin(
        /@sqlite\.org\/sqlite-wasm/,
        (data) => {
            delete data.dependencies;
            return data;
        }
    )
);

// 9. MIME types
config.devServer = config.devServer || {};
config.devServer.devMiddleware = config.devServer.devMiddleware || {};
config.devServer.devMiddleware.mimeTypes = {
    'application/wasm': ['wasm'],
    'application/javascript': ['js']
};

// 10. OPTIMIZATION: Exclude SQLite workers from parsing and minification
// This fixes the "return outside of function" error in Terser and speeds up build
config.module.noParse = config.module.noParse || [];
if (Array.isArray(config.module.noParse)) {
    config.module.noParse.push(/sqlite3-worker1\.mjs/);
    config.module.noParse.push(/sqlite3\.mjs/);
} else {
    // If it's a function or RegExp, we wrap it (simplified for now, assuming array or undefined)
    config.module.noParse = [config.module.noParse, /sqlite3-worker1\.mjs/, /sqlite3\.mjs/];
}

if (config.optimization && config.optimization.minimizer) {
    config.optimization.minimizer.forEach(minimizer => {
        if (minimizer.constructor.name === 'TerserPlugin') {
            minimizer.options.exclude = minimizer.options.exclude || [];
            const excludePattern = /sqlite3-worker1\.mjs/;
            if (Array.isArray(minimizer.options.exclude)) {
                minimizer.options.exclude.push(excludePattern);
            } else {
                minimizer.options.exclude = [minimizer.options.exclude, excludePattern];
            }
        }
    });
}
