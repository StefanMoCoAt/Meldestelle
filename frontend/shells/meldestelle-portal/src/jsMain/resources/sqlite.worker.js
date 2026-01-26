// Minimal debug worker
console.log("Worker: sqlite.worker.js loaded. Starting initialization...");

try {
    // We do NOT import from node_modules anymore to avoid Webpack bundling issues.
    // import sqlite3InitModule from '@sqlite.org/sqlite-wasm';

    // Message buffer for messages arriving before DB is ready
    let messageQueue = [];
    let db = null;
    let isReady = false;

    // Minimal worker protocol compatible with SQLDelight's `web-worker-driver`.
    self.onmessage = (event) => {
        if (!isReady) {
            console.log("Worker: Buffering message (DB not ready)", event.data);
            messageQueue.push(event);
            return;
        }
        processMessage(event);
    };

    function processMessage(event) {
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
            console.error("Worker: Error processing message", err);
            return postMessage({ id: data && data.id, error: err?.message ?? String(err) });
        }
    }

    self.onerror = function(event) {
        console.error("Error in Web Worker (onerror):", event.message, event.filename, event.lineno);
        // Don't postMessage here as it might confuse the driver if it expects a response to a query
    };

    async function init() {
        try {
            // 1. Load the sqlite3.js library manually via importScripts.
            console.log("Worker: Loading sqlite3.js via importScripts...");
            try {
                importScripts('sqlite3.js');
            } catch (e) {
                throw new Error("Failed to importScripts('sqlite3.js'). Check if file exists at root. Error: " + e.message);
            }

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

            // Initialize DB
            const dbName = 'app.db';
            if (opfsAvailable) {
                console.log("Initialisiere persistente OPFS Datenbank: " + dbName);
                db = new sqlite3.oo1.OpfsDb(dbName);
            } else {
                console.warn("OPFS nicht verfügbar, Fallback auf In-Memory");
                db = new sqlite3.oo1.DB(dbName);
            }

            // Mark as ready and process queue
            isReady = true;
            console.log("Worker: DB Ready. Processing " + messageQueue.length + " buffered messages.");
            while (messageQueue.length > 0) {
                processMessage(messageQueue.shift());
            }

        } catch (e) {
            console.error("Database initialization error in worker:", e);
            // We can't easily communicate this back to the driver during init,
            // but console.error should show up.
        }
    }

    init();

} catch (e) {
    console.error("Critical Worker Error:", e);
}
