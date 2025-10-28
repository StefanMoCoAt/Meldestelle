#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Spiegelt Markdown-Dateien (z. B. aus build/dokka/gfm) in eine YouTrack Knowledge Base.
- Verwendet Umgebungsvariablen YT_URL und YT_TOKEN
- Erwartet den KB-Root-Titel in KB_ROOT_TITLE (z. B. "API & Entwicklerdoku")
- Optional: KB_BC_ROOT (Unterordnername, z. B. "BCs") – wird aktuell nur als Titelpräfix genutzt

Sicherheit:
- Tokens werden niemals geloggt.
- Bei HTTP-Fehlern werden Statuscode und gekürzte Antwort ausgegeben.
"""
import argparse
import os
import sys
import json
import time
from pathlib import Path

try:
    import requests
except ImportError:  # pragma: no cover
    print("[YT] requests fehlt. Bitte 'pip install requests' ausführen.")
    sys.exit(2)

SESSION = requests.Session()
SESSION.headers.update({
    "Accept": "application/json",
    "Content-Type": "application/json",
})


def yt_url(path: str) -> str:
    base = os.environ.get("YT_URL", "").rstrip("/")
    if not base:
        print("[YT] YT_URL fehlt in Env.")
        sys.exit(2)
    if not path.startswith("/"):
        path = "/" + path
    return base + path


def set_auth():
    token = os.environ.get("YT_TOKEN")
    if not token:
        print("[YT] YT_TOKEN fehlt in Env.")
        sys.exit(2)
    # Bearer Token
    SESSION.headers["Authorization"] = f"Bearer {token}"


def http(method: str, url: str, **kw):
    # Einfaches Retry bei 429/5xx
    for attempt in range(5):
        r = SESSION.request(method, url, timeout=30, **kw)
        if r.status_code in (429, 500, 502, 503, 504):
            wait = (attempt + 1) * 1.5
            print(f"[YT] {r.status_code} → Retry in {wait:.1f}s…")
            time.sleep(wait)
            continue
        return r
    return r


def get_knowledge_bases():
    url = yt_url("/api/knowledgeBases?fields=id,name")
    r = http("GET", url)
    if r.status_code != 200:
        print(f"[YT] KB-Liste fehlgeschlagen: HTTP {r.status_code} {r.text[:400]}")
        sys.exit(1)
    return r.json()


def find_kb_by_name(name: str):
    for kb in get_knowledge_bases():
        if kb.get("name") == name:
            return kb
    return None


def find_article_in_kb_by_title(kb_id: str, title: str):
    # Filter per search nicht stabil → hole paginiert und filtere clientseitig
    url = yt_url(f"/api/articles?fields=id,title,knowledgeBase(id)&$top=1000")
    r = http("GET", url)
    if r.status_code != 200:
        print(f"[YT] Artikel-Liste fehlgeschlagen: HTTP {r.status_code} {r.text[:400]}")
        sys.exit(1)
    for art in r.json():
        kb = art.get("knowledgeBase") or {}
        if kb.get("id") == kb_id and art.get("title") == title:
            return art
    return None


def create_article(kb_id: str, title: str, markdown: str):
    url = yt_url("/api/articles?fields=id,title")
    payload = {
        "title": title,
        "content": markdown,
        "knowledgeBase": {"id": kb_id},
        # Sichtbarkeit: öffentlich/privat – Standard-Einstellungen der KB werden übernommen
    }
    r = http("POST", url, data=json.dumps(payload))
    if r.status_code not in (200, 201):
        print(f"[YT] Artikel erstellen fehlgeschlagen: HTTP {r.status_code} {r.text[:400]}")
        sys.exit(1)
    return r.json()


def update_article(article_id: str, markdown: str):
    url = yt_url(f"/api/articles/{article_id}?fields=id")
    payload = {"content": markdown}
    r = http("POST", url, data=json.dumps(payload))  # YouTrack erlaubt POST als Update
    if r.status_code not in (200, 201):
        # Fallback PATCH
        r = http("PATCH", url, data=json.dumps(payload))
    if r.status_code not in (200, 204):
        print(f"[YT] Artikel aktualisieren fehlgeschlagen: HTTP {r.status_code} {r.text[:400]}")
        sys.exit(1)


def build_title_from_path(rel_path: Path, bc_root: str | None) -> str:
    # Beispiel: infrastructure/gateway/index.md → "infrastructure / gateway / index.md"
    parts = list(rel_path.parts)
    title = " / ".join(parts)
    if bc_root:
        title = f"{bc_root} / {title}"
    return title


def load_markdown(path: Path) -> str:
    try:
        text = path.read_text(encoding="utf-8")
    except Exception as e:
        print(f"[YT] Kann Datei nicht lesen: {path}: {e}")
        sys.exit(1)
    return text


def main():
    ap = argparse.ArgumentParser(description="Sync Dokka Markdown nach YouTrack KB")
    ap.add_argument("--src", default="build/dokka/gfm", help="Quellverzeichnis (Markdown)")
    args = ap.parse_args()

    kb_root_title = os.environ.get("KB_ROOT_TITLE")
    bc_root = os.environ.get("KB_BC_ROOT")
    if not kb_root_title:
        print("[YT] KB_ROOT_TITLE fehlt in Env.")
        sys.exit(2)

    set_auth()

    src = Path(args.src)
    if not src.exists():
        print(f"[YT] Quelle nicht gefunden: {src} – nichts zu tun.")
        return 0

    kb = find_kb_by_name(kb_root_title)
    if not kb:
        print(f"[YT] Knowledge Base '{kb_root_title}' nicht gefunden. Bitte in YouTrack anlegen.")
        sys.exit(1)
    kb_id = kb["id"]
    print(f"[YT] Verwende KB: {kb_root_title} ({kb_id})")

    count = 0
    for md in src.rglob("*.md"):
        rel = md.relative_to(src)
        title = build_title_from_path(rel, bc_root)
        content = load_markdown(md)
        # Optional: youtrack-spez. Front-Matter entfernen – Dokka erzeugt keine
        existing = find_article_in_kb_by_title(kb_id, title)
        if existing:
            update_article(existing["id"], content)
            print(f"[YT] Aktualisiert: {title}")
        else:
            create_article(kb_id, title, content)
            print(f"[YT] Erstellt: {title}")
        count += 1

    print(f"[YT] Fertig. {count} Artikel synchronisiert.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
