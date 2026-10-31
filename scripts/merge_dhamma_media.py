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
 if not p.exists(): return []
 with (gzip.open(p,"rt",encoding="utf-8") if p.suffix==".gz" or p.suffix==".dat" else p.open(encoding="utf-8")) as f:return json.load(f)
def main():
 ap=argparse.ArgumentParser();ap.add_argument("--input",required=True);a=ap.parse_args()
 rows=load_json(pathlib.Path(a.input))
 radio=root/"app/src/main/assets/radio_catalog.dat"
 if not radio.exists():raise SystemExit("Run after base radio_catalog.dat decode")
 existing=load_json(radio);seen={i.get("url","").split("#")[0] for i in existing};added=0
 mp4=[];video_seen=set()
 for entry in rows:
  u=(entry.get("media_url") or "").strip();t=(entry.get("title") or urllib.parse.unquote(u.rsplit("/",1)[-1])).strip()[:220]
  if not host_ok(u) or not t:continue
  fmt=entry.get("format","").upper()
  source=entry.get("source_page","")
  if fmt=="MP3" and u.split("#")[0] not in seen:
   existing.append({"id":"DD-"+hashlib.sha256(u.encode()).hexdigest()[:16],"monk_id":0,"monk":"Dhammadownload","title":t,"category":"အခြား","url":u,"listing_url":source,"status":"publisher_index_link_not_stream_tested"})
   seen.add(u.split("#")[0]);added+=1
  elif fmt=="MP4" and u.split("#")[0] not in video_seen:
   mp4.append({"title":t,"url":u,"source_url":source,"status":"publisher_index_link_not_stream_tested"});video_seen.add(u.split("#")[0])
 with gzip.open(radio,"wt",encoding="utf-8") as f:json.dump(existing,f,ensure_ascii=False,separators=(",",":"))
 # Direct MP4 links require a compatible native player; do not claim YouTube player support.
 target=root/"app/src/main/assets/dhammadownload_mp4_pending.json"
 target.write_text(json.dumps(mp4,ensure_ascii=False,separators=(",",":")),encoding="utf-8")
 print(f"Imported {added} new MP3 references, {len(mp4)} MP4 references awaiting native playback integration")
if __name__=="__main__":main()
