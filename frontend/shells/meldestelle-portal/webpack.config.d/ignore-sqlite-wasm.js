// This file contains Webpack configuration adjustments for WebAssembly modules,
// specifically to handle `skiko.wasm` and `sqlite3.wasm` correctly.

var pathModule;
try {
    pathModule = path;
} catch (e) {
    pathModule = require('path');
}

var webpackModule;
try {
    webpackModule = webpack;
} catch (e) {
    webpackModule = require('webpack');
}

// 1. Enable WebAssembly experiments in Webpack 5
config.experiments = config.experiments || {};
config.experiments.asyncWebAssembly = true;

config.module = config.module || {};
config.module.rules = config.module.rules || [];

// 2. Add a rule to correctly handle .wasm files (like skiko.wasm) as WebAssembly modules
config.module.rules.push({
    test: /\.wasm$/,
    type: 'webassembly/async'
});

// 3. NormalModuleReplacementPlugin to redirect 'sqlite3.wasm' AND other internal sqlite-wasm modules to our dummy JS file.
// This is needed because the `sqlite-wasm` library tries to `require` these files in a Webpack environment.
// We want these `require` calls to return an empty JS object (from dummy.js) instead of failing.
// Our worker will manually fetch the real sqlite3.wasm.

const dummyPath = pathModule.resolve(__dirname, "../../../../frontend/shells/meldestelle-portal/build/processedResources/js/main/dummy.js");

// Redirect sqlite3.wasm
config.plugins.push(
    new webpackModule.NormalModuleReplacementPlugin(
        /sqlite3\.wasm$/,
        dummyPath
    )
);

// Redirect other internal sqlite-wasm modules that might be causing issues
// The error log showed: Can't resolve './sqlite-wasm/jswasm/sqlite3.mjs' and 'sqlite3-worker1-promiser.mjs'
// We redirect them to dummy.js as well, assuming we don't need them for our manual loading approach.
// Be careful not to redirect the main entry point if it's needed.
// The errors seem to come from inside the node_modules package trying to resolve relative paths.

// Let's try to be more specific. If these are optional dependencies or part of the node-loading logic,
// replacing them with dummy.js should be fine.
config.plugins.push(
    new webpackModule.NormalModuleReplacementPlugin(
        /sqlite3\.mjs$/,
        function(resource) {
            // Only replace if it's inside the sqlite-wasm package structure we want to avoid
            if (resource.context.includes('@sqlite.org/sqlite-wasm')) {
                resource.request = dummyPath;
            }
        }
    )
);

config.plugins.push(
    new webpackModule.NormalModuleReplacementPlugin(
        /sqlite3-worker1-promiser\.mjs$/,
        dummyPath
    )
);


// 4. Handle WASI imports for skiko.wasm (env, wasi_snapshot_preview1)
// Webpack needs to know how to resolve these "magic" imports.
// We can treat them as externals or empty modules.
// Since we are in a browser environment, these are often provided by the runtime or polyfilled.
// Mapping them to false tells Webpack to ignore them (empty module).

config.resolve = config.resolve || {};
config.resolve.fallback = config.resolve.fallback || {};

// Fallbacks for Node.js core modules that might be required by libraries
config.resolve.fallback.fs = false;
config.resolve.fallback.path = false;
config.resolve.fallback.crypto = false;

// Ignore WASI imports
config.ignoreWarnings = config.ignoreWarnings || [];
config.ignoreWarnings.push(/Critical dependency: the request of a dependency is an expression/);

// Use externals to handle WASI imports if fallback doesn't work
config.externals = config.externals || {};
// config.externals['env'] = 'env'; // This might expect a global 'env' variable
// config.externals['wasi_snapshot_preview1'] = 'wasi_snapshot_preview1';

// Better approach for WASI in Webpack 5 with asyncWebAssembly:
// Webpack should handle this if we don't interfere.
// The error "Can't resolve 'env'" suggests it's looking for a module named 'env'.
// We can provide a dummy module for these.

config.resolve.alias = config.resolve.alias || {};
config.resolve.alias['env'] = dummyPath;
config.resolve.alias['wasi_snapshot_preview1'] = dummyPath;
