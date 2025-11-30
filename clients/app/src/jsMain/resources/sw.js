const IS_DEV = self.location.hostname === 'localhost' || self.location.hostname === '127.0.0.1' || self.location.hostname === '::1';

const CACHE_NAME = 'meldestelle-cache-v2';
const PRECACHE_URLS = [
  '/',
  '/index.html',
  '/styles.css'
];

self.addEventListener('install', (event) => {
  if (IS_DEV) {
    // In dev, don't precache. Just activate the SW immediately.
    self.skipWaiting();
    return;
  }
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then((cache) => cache.addAll(PRECACHE_URLS))
      .then(() => self.skipWaiting())
  );
});

self.addEventListener('activate', (event) => {
  if (IS_DEV) {
    event.waitUntil(self.clients.claim());
    return;
  }
  event.waitUntil(
    caches.keys().then((keys) => Promise.all(
      keys.filter((k) => k !== CACHE_NAME).map((k) => caches.delete(k))
    )).then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', (event) => {
  if (IS_DEV) {
    return; // don't interfere with dev server/HMR
  }

  const req = event.request;
  const url = new URL(req.url);

  const isHttp = url.protocol === 'http:' || url.protocol === 'https:';
  const sameOrigin = url.origin === self.location.origin;
  const isExtension = url.protocol === 'chrome-extension:';
  const isHotUpdate = url.pathname.includes('hot-update');
  const isWebSocketUpgrade = req.headers.get('upgrade') === 'websocket';

  // Ignore non-GET, cross-origin, browser extensions, HMR, and WebSocket upgrade requests
  if (req.method !== 'GET' || !isHttp || !sameOrigin || isExtension || isHotUpdate || isWebSocketUpgrade) {
    return; // Let the browser handle it
  }

  if (req.mode === 'navigate') {
    // Network-first for navigation
    event.respondWith(
      fetch(req)
        .then((resp) => {
          if (resp && resp.status === 200 && resp.type === 'basic') {
            const copy = resp.clone();
            caches.open(CACHE_NAME).then((cache) => cache.put('/index.html', copy)).catch(() => {
            });
          }
          return resp;
        })
        .catch(() => caches.match('/index.html'))
    );
    return;
  }

  // Avoid noisy errors for favicon during dev/prod when missing
  if (url.pathname === '/favicon.ico') {
    event.respondWith(
      fetch(req).catch(() => caches.match(req))
    );
    return;
  }

  // Cache-first for static assets
  event.respondWith(
    caches.match(req).then((cached) => {
      if (cached) return cached;
      return fetch(req)
        .then((resp) => {
          if (resp && resp.status === 200 && resp.type === 'basic') {
            const copy = resp.clone();
            caches.open(CACHE_NAME).then((cache) => cache.put(req, copy)).catch(() => {
            });
          }
          return resp;
        })
        .catch(() => caches.match(req));
    })
  );
});
