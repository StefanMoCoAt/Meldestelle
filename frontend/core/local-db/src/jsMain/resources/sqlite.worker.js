import sqlite3InitModule from '@sqlite.org/sqlite-wasm';

// Minimal worker protocol compatible with SQLDelight's `web-worker-driver`.
// Mirrors the message format used by SQLDelight's `sqljs.worker.js` implementation.
function runWorker({ driver }) {
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
                    // sqlite-wasm oo1 DB supports `.exec(...)`.
                    // We intentionally return only `values` which is sufficient for SQLDelight.
                    const rows = [];
                    db.exec({
                        sql: data.sql,
                        bind: data.params ?? [],
                        rowMode: 'array',
                        callback: (row) => rows.push(row)
                    });
                    return postMessage({ id: data.id, results: { values: rows } });
                }
                case 'begin_transaction':
                    db.exec('BEGIN TRANSACTION;');
                    return postMessage({ id: data.id, results: [] });
                case 'end_transaction':
                    db.exec('END TRANSACTION;');
                    return postMessage({ id: data.id, results: [] });
                case 'rollback_transaction':
                    db.exec('ROLLBACK TRANSACTION;');
                    return postMessage({ id: data.id, results: [] });
                default:
                    throw new Error(`Unsupported action: ${data && data.action}`);
            }
        } catch (err) {
            return postMessage({ id: data && data.id, error: err?.message ?? String(err) });
        }
    };
}

sqlite3InitModule({
    print: console.log,
    printErr: console.error,
}).then((sqlite3) => {
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
});
