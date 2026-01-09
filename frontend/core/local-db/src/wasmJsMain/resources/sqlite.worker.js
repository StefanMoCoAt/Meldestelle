import { runWorker } from '@cashapp/sqldelight-sqljs-worker';
import sqlite3InitModule from '@sqlite.org/sqlite-wasm';

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
