// Dummy module to satisfy WASI imports in Webpack
// Used for skiko.wasm and potentially others

export function abort() {
  console.error("WASI abort called");
}

// Some WASM modules might look for these
export function emscripten_notify_memory_growth() {
}

export default {
  abort,
  emscripten_notify_memory_growth
};
