// Essenzielle Header für OPFS Support (SharedArrayBuffer)
// Siehe: https://sqlite.org/wasm/doc/trunk/persistence.html#opfs
config.devServer = config.devServer || {};
config.devServer.headers = {
  ...config.devServer.headers,
    "Cross-Origin-Opener-Policy": "same-origin",
    "Cross-Origin-Embedder-Policy": "require-corp"
};
