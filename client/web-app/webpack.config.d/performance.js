module.exports = (config) => {
    config.performance = {
        hints: false,                // Warnungen aus
        maxEntrypointSize: 1024 * 1024,
        maxAssetSize: 1024 * 1024,
    };
};
