#!/usr/bin/env python3
"""Import verified publisher INDEX metadata into DAMA V22 build assets.
Input: dhamma_media_catalog.json from crawler, never downloads media.
Avoids duplicate URLs; app's current artwork and UI remain unchanged.
"""
import argparse,gzip,json,hashlib,pathlib,urllib.parse
root=pathlib.Path(__file__).resolve().parents[1]
def host_ok(url):
 p=urllib.parse.urlsplit(url)
 return p.scheme=="https" and (p.hostname or "").lower() in {"dhammadownload.com","www.dhammadownload.com","ftp.dhammadownload.com"}
def load_json(p):
 if not p.exists():return []
 with (gzip.open(p,"rt",encoding="utf-8") if p.suffix in {".gz",".dat"} else p.open(encoding="utf-8")) as f:
  raw=f.read().strip()
 # Old crawler workflow accidentally appended literal backslash-n after valid JSON.
 if raw.endswith(chr(92)+"n"):raw=raw[:-2].rstrip()
 try:
  items=json.loads(raw)
  if not isinstance(items,list):raise ValueError("Catalog must be a JSON array")
  return items
 except json.JSONDecodeError:
  # Recover concatenated JSON arrays from a previous GitHub import commit.
  # Decode complete objects; reject unexpected non-JSON content.
  decoder=json.JSONDecoder();i=0;items=[]
  while i<len(raw):
   if raw[i].isspace() or raw[i] in "[],":i+=1;continue
   if raw[i]!="{":raise ValueError(f"Unexpected catalog character at offset {i}: {raw[i]!r}")
   entry,end=decoder.raw_decode(raw,i)
   if not isinstance(entry,dict):raise ValueError("Catalog entries must be JSON objects")
   items.append(entry);i=end
  if not items:raise ValueError("Empty or corrupt catalog")
  return items
def main():
 ap=argparse.ArgumentParser();ap.add_argument("--input",required=True);ap.add_argument("--exact-new",type=int,default=0);a=ap.parse_args()
 rows=load_json(pathlib.Path(a.input))
 radio=root/"app/src/main/assets/radio_catalog.dat"
 if not radio.exists():raise SystemExit("Run after base radio_catalog.dat decode")
 existing=load_json(radio);seen={i.get("url","").split("#")[0] for i in existing};added=0
 mp4=[];video_seen=set();duplicates=0;rejected=0
 for entry in rows:
  u=(entry.get("media_url") or "").strip();t=(entry.get("title") or urllib.parse.unquote(u.rsplit("/",1)[-1])).strip()[:220]
  if not host_ok(u) or not t:rejected+=1;continue
  fmt=entry.get("format","").upper()
  source=entry.get("source_page","")
  if fmt=="MP3" and u.split("#")[0] not in seen:
   existing.append({"id":"DD-"+hashlib.sha256(u.encode()).hexdigest()[:16],"monk_id":0,"monk":"Dhammadownload","title":t,"category":"အခြား","url":u,"listing_url":source,"status":"publisher_index_link_not_stream_tested"})
   seen.add(u.split("#")[0]);added+=1
   if a.exact_new and added==a.exact_new:break
  elif fmt=="MP3" and u.split("#")[0] in seen:duplicates+=1
  elif fmt=="MP4" and u.split("#")[0] not in video_seen:
   mp4.append({"title":t,"url":u,"source_url":source,"status":"publisher_index_link_not_stream_tested"});video_seen.add(u.split("#")[0])
 if a.exact_new and added!=a.exact_new:raise SystemExit(f"Expected exactly {a.exact_new} fresh MP3 links, only {added} available")
 with gzip.open(radio,"wt",encoding="utf-8") as f:json.dump(existing,f,ensure_ascii=False,separators=(",",":"))
 # Direct MP4 links require a compatible native player; do not claim YouTube player support.
 target=root/"app/src/main/assets/dhammadownload_mp4_pending.json"
 target.write_text(json.dumps(mp4,ensure_ascii=False,separators=(",",":")),encoding="utf-8")
 print(f"DAMA MP3 catalog total: {len(existing)}; new: {added}; already indexed: {duplicates}; rejected: {rejected}; MP4 references: {len(mp4)}")
if __name__=="__main__":main()
