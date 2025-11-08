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
import json
import os
import sys
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

# Globale Variablen, in main() gesetzt
PROJECT_ID = "MP"  # Standardwert, wird in main() überschrieben
KB_ID = None       # Knowledge-Base-ID des Projekts
KB_ROOT_ARTICLE_ID = None  # ID des echten KB-Wurzelartikels (Container)

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

# *** KORRIGIERTE FUNKTION ***
def get_project_by_short_name(name: str):
  """Sucht die interne ID des Projekts anhand des Kürzels (z.B. 'MP')."""
  url = yt_url(f"/api/projects?fields=id,shortName")
  r = http("GET", url)
  if r.status_code != 200:
    print(f"[YT] Projektliste fehlgeschlagen: HTTP {r.status_code} {r.text[:400]}")
    sys.exit(1)
  for proj in r.json():
    if proj.get("shortName") == name:
      return proj
  return None

# Neues Hilfs-API: Knowledge Base des Projekts abfragen

def get_project_knowledge_base(project_id: str):
  """Liest die Knowledge-Base eines Projekts, inkl. Root-Container-Artikel."""
  url = yt_url(f"/api/projects/{project_id}?fields=knowledgeBase(id,articlesCount,rootArticle(id,title))")
  r = http("GET", url)
  if r.status_code != 200:
    print(f"[YT] Projekt-Details fehlgeschlagen: HTTP {r.status_code} {r.text[:400]}")
    sys.exit(1)
  data = r.json()
  kb = data.get("knowledgeBase")
  if not kb:
    return None
  return kb

# *** KORRIGIERTE FUNKTION ***
def find_article_in_kb_by_title(title: str, parent_id: str | None = None):
  """Sucht einen Artikel anhand des Titels in der KB des Projekts.
  Optional kann parent_id angegeben werden, um die Suche auf direkte Kinder eines Artikels einzuschränken.
  """
  url = yt_url("/api/articles")
  params = {
    "query": f'title: "{title}"',
    "fields": "id,title,knowledgeBase(id),parent(id)"
  }
  r = http("GET", url, params=params)
  if r.status_code != 200:
    print(f"[YT] Artikelsuche fehlgeschlagen: HTTP {r.status_code} {r.text[:400]}")
    sys.exit(1)

  for art in r.json():
    if art.get("title") != title:
      continue
    kb = art.get("knowledgeBase") or {}
    if kb.get("id") != KB_ID:
      continue
    if parent_id:
      parent = art.get("parent") or {}
      if parent.get("id") != parent_id:
        continue
    return art
  return None

# *** KORRIGIERTE FUNKTION ***
def create_article(title: str, markdown: str, parent_id: str = None):
  """Erstellt einen neuen Artikel in der Knowledge Base des Projekts."""
  url = yt_url("/api/articles?fields=id,title")
  payload = {
    "title": title,
    "content": markdown,
    "knowledgeBase": {"id": KB_ID}
  }
  if parent_id:
    payload["parent"] = {"id": parent_id}

  r = http("POST", url, data=json.dumps(payload))
  if r.status_code not in (200, 201):
    print(f"[YT] Artikel erstellen fehlgeschlagen: HTTP {r.status_code} {r.text[:400]}")
    sys.exit(1)
  return r.json()

# *** KORRIGIERTE FUNKTION ***
def update_article(article_id: str, markdown: str):
  """Aktualisiert einen bestehenden Artikel in der Knowledge Base des Projekts."""
  url = yt_url(f"/api/articles/{article_id}?fields=id")
  payload = {"content": markdown}

  # YouTrack API verwendet POST für Updates an Artikeln
  r = http("POST", url, data=json.dumps(payload))
  if r.status_code not in (200, 201):
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
  global PROJECT_ID, KB_ID, KB_ROOT_ARTICLE_ID  # Zugriff auf globale Variablen

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

  # Projekt anhand des Kürzels ermitteln
  project_short_name = os.environ.get("YT_PROJECT", "MP").strip() or "MP"
  project = get_project_by_short_name(project_short_name)
  if not project:
    print(f"[YT] Projekt mit Kürzel '{project_short_name}' nicht gefunden.")
    sys.exit(1)
  PROJECT_ID = project["id"]
  print(f"[YT] Arbeite im Projekt: {project_short_name} (ID: {PROJECT_ID})")

  # Knowledge Base des Projekts abrufen
  kb = get_project_knowledge_base(PROJECT_ID)
  if not kb:
    print("[YT] Dieses Projekt hat keine Knowledge Base. Bitte in YouTrack aktivieren/anlegen.")
    sys.exit(1)
  KB_ID = kb.get("id")
  root_article = kb.get("rootArticle") or {}
  KB_ROOT_ARTICLE_ID = root_article.get("id")
  print(f"[YT] Verwende Knowledge Base: {KB_ID}; Root-Container: {KB_ROOT_ARTICLE_ID}")

  # Root-Artikel (logische Wurzel unterhalb des KB-Root-Containers) finden/erstellen
  kb_root = find_article_in_kb_by_title(kb_root_title, parent_id=KB_ROOT_ARTICLE_ID)
  if not kb_root:
    print(f"[YT] Root-Artikel '{kb_root_title}' nicht gefunden. Erstelle ihn unterhalb des KB-Root-Containers…")
    kb_root = create_article(kb_root_title, f"# {kb_root_title}\n\nDieser Artikel dient als Wurzel für die automatische KDoc-Synchronisation.", parent_id=KB_ROOT_ARTICLE_ID)
    print(f"[YT] Root-Artikel erstellt: {kb_root_title} (ID: {kb_root['id']})")

  kb_root_id = kb_root["id"]
  print(f"[YT] Verwende KB-Root: {kb_root_title} ({kb_root_id})")

  count = 0
  for md in src.rglob("*.md"):
    rel = md.relative_to(src)
    title = build_title_from_path(rel, bc_root)
    content = load_markdown(md)

    existing = find_article_in_kb_by_title(title, parent_id=kb_root_id)
    if existing:
      update_article(existing["id"], content)
      print(f"[YT] Aktualisiert: {title}")
    else:
      # Erstelle Artikel als Kind des Root-Artikels
      create_article(title, content, parent_id=kb_root_id)
      print(f"[YT] Erstellt: {title}")
    count += 1

  print(f"[YT] Fertig. {count} Artikel synchronisiert.")
  return 0


if __name__ == "__main__":
  sys.exit(main())
