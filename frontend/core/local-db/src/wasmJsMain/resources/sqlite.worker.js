import sqlite3InitModule from '@sqlite.org/sqlite-wasm';

// Minimal worker protocol compatible with SQLDelight's `web-worker-driver`.
// Mirrors the message format used by SQLDelight's `sqljs.worker.js` implementation.
function runWorker({driver}) {
  let db = null;
  const open = (name) => {
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
      return postMessage({id: data && data.id, error: err?.message ?? String(err)});
    }
  };
}

// Error handling wrapper
self.onerror = function (event) {
  console.error("Error in Web Worker:", event.message, event.filename, event.lineno);
  // Optionally, send the error back to the main thread
  self.postMessage({type: 'error', message: event.message, filename: event.filename, lineno: event.lineno});
};

try {
  sqlite3InitModule({
    print: console.log,
    printErr: console.error,
  }).then((sqlite3) => {
    try {
      const opfsAvailable = 'opfs' in sqlite3;

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
      console.error("Database initialization error in worker (inner):", e);
      self.postMessage({type: 'error', message: 'Database initialization failed (inner): ' + e.message});
    }
  });
} catch (e) {
  console.error("Database initialization error in worker (outer):", e);
  self.postMessage({type: 'error', message: 'Database initialization failed (outer): ' + e.message});
}
