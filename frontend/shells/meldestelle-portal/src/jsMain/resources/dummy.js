// This is a dummy file to satisfy Webpack's requirement for sqlite3.wasm and other modules.
// It mimics the structure of the sqlite3-wasm module to prevent build errors.

// The worker code imports it like this:
// import sqlite3InitModule from '@sqlite.org/sqlite-wasm';

// So we need a default export that is a function.
// This function should mimic the behavior of sqlite3InitModule, which returns a Promise.
export default function dummySqlite3InitModule() {
  // Since we are manually loading the WASM binary in the worker, this dummy module
  // is primarily here to satisfy Webpack's resolution and prevent errors.
  // It doesn't need to actually load the WASM.
  return Promise.resolve({});
};
