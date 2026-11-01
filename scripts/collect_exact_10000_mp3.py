#!/usr/bin/env python3
"""Crawl publisher indexes; export at most 10,000 genuinely new MP3 URLs.
No fabricated URLs. Any shortage exits nonzero. Respect robots.txt and throttling.
"""
import argparse,concurrent.futures,gzip,html,json,re,time,urllib.parse,urllib.request,urllib.robotparser
from collections import deque
from html.parser import HTMLParser
from pathlib import Path
ROOT="https://www.dhammadownload.com/"
HOSTS={"dhammadownload.com","www.dhammadownload.com"}
class Links(HTMLParser):
 def __init__(self):super().__init__();self.hrefs=[];self.href=None;self.words=[]
 def handle_starttag(self,tag,attrs):
  if tag=="a":self.href=dict(attrs).get("href");self.words=[]
 def handle_data(self,data):
  if self.href is not None:self.words.append(data)
 def handle_endtag(self,tag):
  if tag=="a" and self.href is not None:self.hrefs.append((self.href," ".join(self.words).strip()));self.href=None
def norm(base,href):
 try:
  u=urllib.parse.urljoin(base,html.unescape(href.strip()).replace(" ","%20"))
  p=urllib.parse.urlsplit(u);host=(p.hostname or "").lower()
  if p.scheme not in ("http","https") or host not in HOSTS:return None
  return urllib.parse.urlunsplit(("https",host,p.path,p.query,""))
 except Exception:return None
def main():
 ap=argparse.ArgumentParser();ap.add_argument("--target",type=int,default=10000);ap.add_argument("--pages",type=int,default=1500);ap.add_argument("--baseline",default="");ap.add_argument("--output",default="imports/dhamma_10000_new.json");a=ap.parse_args()
 baseline=set()
 if a.baseline:
  p=Path(a.baseline)
  if p.exists():
   with gzip.open(p,"rt",encoding="utf-8") if p.suffix==".dat" else p.open(encoding="utf-8") as f:
    baseline={x["url"].replace("http://","https://",1) for x in json.load(f)}
 robot=urllib.robotparser.RobotFileParser(ROOT+"robots.txt")
 try:robot.read()
 except Exception:pass
 seeds=[ROOT+"audioinmyanmar.htm",ROOT+"AudioInMyanmar.htm"]
 pending=deque(seeds);seen_pages=set();found={};errors=0
 while pending and len(seen_pages)<a.pages and len(found)<a.target:
  page=pending.popleft()
  if page in seen_pages:continue
  seen_pages.add(page)
  if not robot.can_fetch("DAMA-CatalogBot",page):continue
  try:
   request=urllib.request.Request(page,headers={"User-Agent":"DAMA-CatalogBot/1.0 (publisher index metadata only)"})
   with urllib.request.urlopen(request,timeout=18) as response:
    if "html" not in response.headers.get("Content-Type","").lower():continue
    data=response.read(2_000_000)
   parser=Links();parser.feed(data.decode("utf-8",errors="replace"))
   for href,title in parser.hrefs:
    url=norm(page,href)
    if not url:continue
    suffix=urllib.parse.urlsplit(url).path.lower()
    if suffix.endswith(".mp3"):
     if url not in baseline and url not in found:
      found[url]={"media_url":url,"title":title or urllib.parse.unquote(url.rsplit("/",1)[-1]),"format":"MP3","source_page":page}
    elif suffix.endswith((".htm",".html")) and url not in seen_pages and len(pending)<10000:
     pending.append(url)
  except Exception as e:
   errors+=1;print("Fetch failed:",page,str(e)[:90],flush=True)
  time.sleep(.12)
 rows=list(found.values())[:a.target];target=Path(a.output);target.parent.mkdir(parents=True,exist_ok=True)
 target.write_text(json.dumps(rows,ensure_ascii=False,indent=2),encoding="utf-8")
 print(json.dumps({"new_unique_mp3":len(rows),"requested":a.target,"pages_checked":len(seen_pages),"errors":errors,"file":str(target)}))
 if len(rows)!=a.target:raise SystemExit("INCOMPLETE: required exactly 10000 new legitimate unique MP3 links; no padded records")
if __name__=="__main__":main()
