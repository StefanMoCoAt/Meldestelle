const CACHE_NAME = 'meldestelle-cache-v1';
const PRECACHE_URLS = [
  '/',
  '/index.html',
  '/styles.css'
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then((cache) => cache.addAll(PRECACHE_URLS))
      .then(() => self.skipWaiting())
  );
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((keys) => Promise.all(
      keys.filter((k) => k !== CACHE_NAME).map((k) => caches.delete(k))
    )).then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', (event) => {
  const req = event.request;
  const url = new URL(req.url);

  const isHttp = url.protocol === 'http:' || url.protocol === 'https:';
  const sameOrigin = url.origin === self.location.origin;
  const isExtension = url.protocol === 'chrome-extension:';
  const isHotUpdate = url.pathname.includes('hot-update');

  // Ignore non-GET, cross-origin, browser extensions, and HMR/hot-update requests
  if (req.method !== 'GET' || !isHttp || !sameOrigin || isExtension || isHotUpdate) {
    return; // Let the browser handle it
  }

  if (req.mode === 'navigate') {
    // Network-first for navigation
    event.respondWith(
      fetch(req)
        .then((resp) => {
          if (resp && resp.status === 200 && resp.type === 'basic') {
            const copy = resp.clone();
            caches.open(CACHE_NAME).then((cache) => cache.put('/index.html', copy)).catch(() => {});
          }
          return resp;
        })
        .catch(() => caches.match('/index.html'))
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
            caches.open(CACHE_NAME).then((cache) => cache.put(req, copy)).catch(() => {});
          }
          return resp;
        })
        .catch(() => caches.match(req));
    })
  );
});
