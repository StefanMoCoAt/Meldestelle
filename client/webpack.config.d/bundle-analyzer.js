// Bundle Analyzer Configuration for WASM Bundle Size Monitoring
// Helps identify which parts of the bundle are largest and can be optimized

// Enable bundle analysis based on environment variable
const enableAnalyzer = process.env.ANALYZE_BUNDLE === 'true';
// Ensure mutable config sections exist to avoid spread on undefined
config.plugins = config.plugins || [];
config.resolve = config.resolve || {};
config.module = config.module || {};

if (enableAnalyzer) {
    console.log('📊 Bundle analyzer enabled - generating bundle report...');

    // Simple bundle size logging without external dependencies
    const originalEmit = config.plugins.find(plugin => plugin.constructor.name === 'DefinePlugin');

    // Add a custom plugin to log bundle sizes
    config.plugins.push({
        apply: (compiler) => {
            compiler.hooks.done.tap('BundleSizeLogger', (stats) => {
                const json = stats.toJson({ all: false, assets: true });
                const assets = (json && json.assets) ? json.assets : [];

                console.log('\n📦 WASM Bundle Analysis Report:');
                console.log('=====================================');

                // Sort assets by size (largest first)
                const sortedAssets = assets
                    .filter(asset => !asset.name.endsWith('.map'))
                    .sort((a, b) => b.size - a.size);

                let totalSize = 0;
                sortedAssets.forEach(asset => {
                    const sizeKB = (asset.size / 1024).toFixed(2);
                    const sizeMB = (asset.size / (1024 * 1024)).toFixed(2);
                    totalSize += asset.size;

                    console.log(`📄 ${asset.name}:`);
                    console.log(`   Size: ${sizeKB} KB (${sizeMB} MB)`);

                    // Identify what type of asset this likely is
                    if (asset.name.includes('skiko')) {
                        console.log('   Type: 🎨 Skiko (Compose UI Framework)');
                    } else if (asset.name.includes('ktor')) {
                        console.log('   Type: 🌐 Ktor (HTTP Client)');
                    } else if (asset.name.includes('kotlin')) {
                        console.log('   Type: 📚 Kotlin Standard Library');
                    } else if (asset.name.includes('wasm')) {
                        console.log('   Type: ⚡ WebAssembly Binary');
                    } else if (asset.name.includes('meldestelle')) {
                        console.log('   Type: 🏠 Application Code');
                    } else {
                        console.log('   Type: 📦 Other/Vendor');
                    }
                    console.log('');
                });

                const totalSizeKB = (totalSize / 1024).toFixed(2);
                const totalSizeMB = (totalSize / (1024 * 1024)).toFixed(2);

                console.log(`📊 Total Bundle Size: ${totalSizeKB} KB (${totalSizeMB} MB)`);
                console.log('=====================================');

                // Provide optimization recommendations
                const wasmAsset = sortedAssets.find(asset => asset.name.includes('.wasm'));
                const jsAsset = sortedAssets.find(asset => asset.name.includes('meldestelle-wasm.js'));

                if (wasmAsset && jsAsset) {
                    const wasmSizeMB = (wasmAsset.size / (1024 * 1024)).toFixed(2);
                    const jsSizeKB = (jsAsset.size / 1024).toFixed(2);

                    console.log('\n💡 Optimization Recommendations:');
                    console.log('=====================================');

                    if (wasmAsset.size > 5 * 1024 * 1024) { // > 5MB
                        console.log(`⚠️  WASM binary is large (${wasmSizeMB}MB). Consider:`);
                        console.log('   - Reducing Compose UI components');
                        console.log('   - Lazy loading features');
                        console.log('   - Tree-shaking unused dependencies');
                    }

                    if (jsAsset.size > 500 * 1024) { // > 500KB
                        console.log(`⚠️  JS bundle is large (${jsSizeKB}KB). Consider:`);
                        console.log('   - Code splitting');
                        console.log('   - Dynamic imports');
                        console.log('   - Removing unused imports');
                    }

                    if (sortedAssets.length > 10) {
                        console.log('✅ Good chunk splitting - multiple small files for better caching');
                    }
                }

                console.log('\n🎯 To analyze specific chunks, set ANALYZE_BUNDLE=true and rebuild');
                console.log('=====================================\n');
            });
        }
    });
}

// Additional tree-shaking optimizations
config.resolve = {
    ...(config.resolve || {}),
    // Prioritize ES6 modules for better tree-shaking
    mainFields: ['module', 'browser', 'main'],
    // Add extensions for better resolution
    extensions: ['.js', '.mjs', '.wasm', '.json']
};

// Mark packages as side-effect-free for better tree-shaking
config.module = {
    ...(config.module || {}),
    rules: [
        ...(config.module && config.module.rules ? config.module.rules : []),
        {
            // Mark Kotlin-generated code as side-effect-free where possible
            test: /\.js$/,
            include: /kotlin/,
            sideEffects: false
        }
    ]
};
