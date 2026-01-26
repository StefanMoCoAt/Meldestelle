// We do NOT import from node_modules anymore to avoid Webpack bundling issues.
// import sqlite3InitModule from '@sqlite.org/sqlite-wasm';

console.log("Worker: sqlite.worker.js loaded. Starting initialization...");

// Minimal worker protocol compatible with SQLDelight's `web-worker-driver`.
function runWorker({driver}) {
  console.log("Worker: runWorker called");
  let db = null;
  const open = (name) => {
    console.log("Worker: Opening database", name);
    db = driver.open(name);
  };

  // Open once with the default database name expected by SQLDelight.
  open('app.db');

  self.onmessage = (event) => {
    const data = event.data;
    try {
      switch (data && data.action) {
        case 'exec': {
          if (!data.sql) throw new Error('exec: Missing query string');
          const rows = [];
          db.exec({
            sql: data.sql,
            bind: data.params ?? [],
            rowMode: 'array',
            callback: (row) => rows.push(row)
          });
          return postMessage({id: data.id, results: {values: rows}});
        }
        case 'begin_transaction':
          db.exec('BEGIN TRANSACTION;');
          return postMessage({id: data.id, results: []});
        case 'end_transaction':
          db.exec('END TRANSACTION;');
          return postMessage({id: data.id, results: []});
        case 'rollback_transaction':
          db.exec('ROLLBACK TRANSACTION;');
          return postMessage({id: data.id, results: []});
        default:
          throw new Error(`Unsupported action: ${data && data.action}`);
      }
    } catch (err) {
      console.error("Worker: Error processing message", err);
      return postMessage({id: data && data.id, error: err?.message ?? String(err)});
    }
  };
}

self.onerror = function (event) {
  console.error("Error in Web Worker (onerror):", event.message, event.filename, event.lineno);
  self.postMessage({type: 'error', message: event.message, filename: event.filename, lineno: event.lineno});
};

async function init() {
  try {
    // 1. Load the sqlite3.js library manually via importScripts.
    // This file is copied to the root by Webpack (CopyWebpackPlugin).
    // This bypasses Webpack's module resolution for the library itself.
    console.log("Worker: Loading sqlite3.js via importScripts...");
    importScripts('sqlite3.js');

    // After importScripts, `sqlite3InitModule` should be available globally.
    if (typeof self.sqlite3InitModule !== 'function') {
      throw new Error("sqlite3InitModule is not defined after importScripts. Check if sqlite3.js was loaded correctly.");
    }

    console.log("Worker: Fetching sqlite3.wasm manually...");
    const response = await fetch('sqlite3.wasm');
    if (!response.ok) {
      throw new Error(`Failed to fetch sqlite3.wasm: ${response.status} ${response.statusText}`);
    }
    const wasmBinary = await response.arrayBuffer();
    console.log("Worker: sqlite3.wasm fetched successfully, size:", wasmBinary.byteLength);

    console.log("Worker: Calling sqlite3InitModule with wasmBinary...");
    const sqlite3 = await self.sqlite3InitModule({
      print: console.log,
      printErr: console.error,
      wasmBinary: wasmBinary
    });

    console.log("Worker: sqlite3InitModule resolved successfully");
    const opfsAvailable = 'opfs' in sqlite3;
    console.log("Worker: OPFS available:", opfsAvailable);

    runWorker({
      driver: {
        open: (name) => {
          if (opfsAvailable) {
            console.log("Initialisiere persistente OPFS Datenbank: " + name);
            return new sqlite3.oo1.OpfsDb(name);
          } else {
            console.warn("OPFS nicht verfügbar, Fallback auf In-Memory");
            return new sqlite3.oo1.DB(name);
          }
        }
      }
    });

  } catch (e) {
    console.error("Database initialization error in worker:", e);
    self.postMessage({type: 'error', message: 'Database initialization failed: ' + e.message});
  }
}

init();
