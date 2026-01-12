// Suppress a known, external webpack warning coming from `@sqlite.org/sqlite-wasm`.
//
// Webpack warning:
//   "Critical dependency: the request of a dependency is an expression"
//
// Root cause:
//   `@sqlite.org/sqlite-wasm/sqlite-wasm/jswasm/sqlite3.mjs` uses a dynamic Worker URL:
//     `new Worker(new URL(options.proxyUri, import.meta.url))`
//   which webpack cannot statically analyze.
//
// We keep this suppression максимально spezifisch:
// - match only this warning message
// - and only if it originates from the sqlite-wasm package path.

(function (config) {
  config.ignoreWarnings = config.ignoreWarnings || []

  // Webpack passes warning objects with `message` and `module.resource`.
  config.ignoreWarnings.push((warning) => {
    const message = String(warning && warning.message ? warning.message : warning)
    if (!message.includes('Critical dependency: the request of a dependency is an expression')) return false

    const resource = warning && warning.module && warning.module.resource
      ? String(warning.module.resource)
      : ''

    return resource.includes('node_modules/@sqlite.org/sqlite-wasm/')
  })
})(config)
