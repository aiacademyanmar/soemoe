#!/usr/bin/env python3
"""Build a bundled SQLite corpus from VRI's Myanmar-script Mula Tipitaka XML.
Source: https://github.com/VipassanaTech/tipitaka-xml
VRI permits non-commercial use with attribution; see source README.
"""
import concurrent.futures
import json
import os
import sqlite3
import sys
import urllib.request
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "app" / "src" / "main" / "assets"
ASSETS.mkdir(parents=True, exist_ok=True)
DB_PATH = ASSETS / "tipitaka_myanmar.db"
API = "https://api.github.com/repos/VipassanaTech/tipitaka-xml/contents/mymr?ref=main&per_page=1000"
UA = "MyanmarTipitakaNative/1.4 (offline Dhamma reader; VRI attribution)"

def fetch(url):
    req = urllib.request.Request(url, headers={"User-Agent": UA, "Accept": "application/vnd.github+json"})
    with urllib.request.urlopen(req, timeout=90) as response:
        return response.read()

listing = json.loads(fetch(API).decode("utf-8"))
files = [x for x in listing if x.get("type") == "file"
         and x.get("name", "").endswith(".mul.xml")
         and x.get("name", "").startswith(("vin", "s", "abh"))]
files.sort(key=lambda x: x["name"])
if len(files) < 50:
    raise SystemExit("Expected the full canonical Mula corpus; found only %d files" % len(files))

def local(tag):
    return tag.rsplit("}", 1)[-1] if "}" in tag else tag

def text_of(element):
    return " ".join("".join(element.itertext()).replace("\u00a0", " ").split()).strip()

def parse_file(item):
    raw = fetch(item["download_url"])
    root = ET.fromstring(raw)
    category = ("ဝိနည်းပိဋကတ်" if item["name"].startswith("vin")
                else "အဘိဓမ္မပိဋကတ်" if item["name"].startswith("abh")
                else "သုတ္တပိဋကတ်")
    title = ""
    for node in root.iter():
        tag = local(node.tag)
        rend = node.attrib.get("rend", "")
        if tag in ("head", "p") and rend in ("book", "nikaya"):
            candidate = text_of(node)
            if candidate and candidate != "။ နမော တဿ ဘဂဝတော အရဟတော သမ္မာသမ္ဗုဒ္ဓဿ။":
                title = candidate
                if rend == "book":
                    break
    if not title:
        title = item["name"].replace(".mul.xml", "")
    body = root.find(".//body")
    if body is None:
        raise ValueError("No body element in " + item["name"])
    blocks = []
    def walk(node, inside_block=False):
        tag = local(node.tag)
        if tag in ("p", "head") and not inside_block:
            value = text_of(node)
            if value:
                rend = node.attrib.get("rend", "")
                kind = "heading" if tag == "head" or rend in (
                    "book", "nikaya", "chapter", "subhead", "subsubhead",
                    "subsubsubhead", "title", "gatha", "gatha1", "gathalast"
                ) else "paragraph"
                blocks.append((kind, value))
            return
        for child in list(node):
            walk(child, inside_block or tag in ("p", "head"))
    walk(body)
    if len(blocks) < 10:
        raise ValueError("Too few text paragraphs in " + item["name"])
    return {
        "filename": item["name"],
        "code": item["name"].replace(".mul.xml", ""),
        "category": category,
        "title": title,
        "blocks": blocks,
    }

parsed = []
errors = []
with concurrent.futures.ThreadPoolExecutor(max_workers=8) as pool:
    futures = {pool.submit(parse_file, item): item["name"] for item in files}
    for future in concurrent.futures.as_completed(futures):
        name = futures[future]
        try:
            parsed.append(future.result())
            print("Downloaded and parsed:", name, flush=True)
        except Exception as exc:
            errors.append((name, str(exc)))
if errors:
    raise SystemExit("Corpus build stopped; failed files: " + repr(errors))
parsed.sort(key=lambda x: x["filename"])

if DB_PATH.exists():
    DB_PATH.unlink()
db = sqlite3.connect(DB_PATH)
db.execute("PRAGMA journal_mode=OFF")
db.execute("PRAGMA synchronous=OFF")
db.execute("PRAGMA page_size=4096")
db.executescript("""
CREATE TABLE documents(
  id INTEGER PRIMARY KEY,
  code TEXT NOT NULL UNIQUE,
  title TEXT NOT NULL,
  category TEXT NOT NULL,
  filename TEXT NOT NULL,
  paragraph_count INTEGER NOT NULL
);
CREATE TABLE passages(
  id INTEGER PRIMARY KEY,
  doc_id INTEGER NOT NULL,
  seq INTEGER NOT NULL,
  kind TEXT NOT NULL,
  body TEXT NOT NULL,
  FOREIGN KEY(doc_id) REFERENCES documents(id)
);
CREATE INDEX idx_passages_doc_seq ON passages(doc_id, seq);
CREATE INDEX idx_documents_category ON documents(category);
""")
total_blocks = 0
for doc in parsed:
    cur = db.execute(
        "INSERT INTO documents(code,title,category,filename,paragraph_count) VALUES(?,?,?,?,?)",
        (doc["code"], doc["title"], doc["category"], doc["filename"], len(doc["blocks"]))
    )
    doc_id = cur.lastrowid
    db.executemany(
        "INSERT INTO passages(doc_id,seq,kind,body) VALUES(?,?,?,?)",
        [(doc_id, i + 1, kind, body) for i, (kind, body) in enumerate(doc["blocks"])]
    )
    total_blocks += len(doc["blocks"])
db.execute("CREATE VIRTUAL TABLE passages_fts USING fts5(body, content='passages', content_rowid='id')")
db.execute("INSERT INTO passages_fts(passages_fts) VALUES('rebuild')")
db.execute("ANALYZE")
db.commit()
db.close()
meta = {
    "source": "Vipassana Research Institute / VipassanaTech tipitaka-xml",
    "source_url": "https://github.com/VipassanaTech/tipitaka-xml",
    "source_folder": "mymr/",
    "edition": "Chaṭṭha Saṅgāyana Tipiṭaka; Pāli text rendered in Myanmar script",
    "license": "Free for non-commercial use; attribute Vipassana Research Institute",
    "license_note": "This is Pāli source text in Myanmar script, not a modern Burmese translation.",
    "documents": len(parsed),
    "paragraphs": total_blocks,
    "files": [x["filename"] for x in parsed],
}
(ASSETS / "corpus_metadata.json").write_text(json.dumps(meta, ensure_ascii=False, indent=2), encoding="utf-8")
print("BUILT", DB_PATH, "bytes=", DB_PATH.stat().st_size, "documents=", len(parsed), "paragraphs=", total_blocks, flush=True)
