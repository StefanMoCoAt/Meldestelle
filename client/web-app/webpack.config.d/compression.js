// Compression and module resolution optimizations

// Enhanced module resolution to reduce bundle size
config.resolve = config.resolve || {};
config.resolve.alias = config.resolve.alias || {};

// Resolve optimizations
config.resolve.modules = ['node_modules'];
config.resolve.extensions = ['.js', '.json', '.wasm'];

// Output optimizations
config.output = config.output || {};
config.output.pathinfo = false; // Disable path info in production for smaller bundles

// Module concatenation for better tree shaking
config.optimization = config.optimization || {};
config.optimization.concatenateModules = true;

// Enable scope hoisting for better performance
config.optimization.moduleIds = 'deterministic';
config.optimization.chunkIds = 'deterministic';

// Webpack production mode optimizations
if (config.mode === 'production') {
    // Disable development features
    config.devtool = false; // Disable source maps in production for smaller size

    // Additional optimization flags
    config.optimization.flagIncludedChunks = true;
    config.optimization.mergeDuplicateChunks = true;
    config.optimization.removeAvailableModules = true;
    config.optimization.removeEmptyChunks = true;

    // Aggressive dead code elimination
    config.optimization.innerGraph = true;
    config.optimization.mangleExports = true;
}
