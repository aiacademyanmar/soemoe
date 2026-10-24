#!/usr/bin/env python3
"""Build-time catalog of publisher-referenced MP3 hyperlinks; never downloads audio.
Source: www.dhammadownload.com; conservative limited crawling + full source attribution.
"""
from pathlib import Path
from urllib.parse import urljoin,urlsplit,urlunsplit,quote
from bs4 import BeautifulSoup
import requests,re,json,sys,time,html,csv
ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'app/src/main/assets'
MONKS=json.loads((ASSETS/'monks.json').read_text('utf8'))
SEEDS=json.loads((ASSETS/'seed_tracks.json').read_text('utf8'))
ALLOWED={'dhammadownload.com','www.dhammadownload.com'}
MP3=re.compile(r'\.mp3$',re.I)
def geturl(base,href):
 url=urljoin(base,html.unescape(href.strip()))
 x=urlsplit(url)
 if (x.hostname or '').lower() not in ALLOWED:return None
 if not MP3.search(x.path):return None
 path=quote(x.path,safe='/%:@+%$(),;=~.-_')
 return urlunsplit((x.scheme or 'https',x.netloc,path,x.query,''))
def key(url):
 x=urlsplit(url)
 return (x.netloc.lower().removeprefix('www.'),x.path.replace('%20',' ').lower(),x.query)
records={}
for x in SEEDS:records[key(x['url'])]=x
session=requests.Session();session.headers.update({'User-Agent':'DamaRadioIndexer/2.0 (educational link-only catalog; do not cache audio)'})
audit=[]
def scrape(url,mid,name):
 count=0
 try:
  resp=session.get(url,timeout=(15,35));resp.raise_for_status()
  if resp.encoding and resp.encoding.lower() in ('iso-8859-1','windows-1252') and resp.apparent_encoding:resp.encoding=resp.apparent_encoding
  soup=BeautifulSoup(resp.text,'html.parser')
  for a in soup.find_all('a',href=True):
   mp3=geturl(resp.url,a.get('href',''))
   if not mp3 or key(mp3) in records:continue
   title=' '.join(a.stripped_strings)
   if not title or title.lower() in {'download','mp3','listen','play'}:title=html.unescape(urlsplit(mp3).path.rsplit('/',1)[-1])
   records[key(mp3)]={'id':'PUBLISHER-'+str(len(records)+1).zfill(6),'monk_id':mid,'monk':name,'title':title[:235], 'category':'အခြား','url':mp3,'listing_url':resp.url,'status':'publisher_index_link_not_stream_tested'}
   count+=1
  audit.append({'source':url,'monk':name,'count_new':count,'status':'ok'})
 except Exception as exc:
  audit.append({'source':url,'monk':name,'count_new':0,'status':str(exc)[:190]})
 return count
for i,m in enumerate(MONKS,1):
 n=scrape(m['source_url'],m['id'],m['name']);print(f'{i:02d}/{len(MONKS)} {m["name"]} +{n} total={len(records)}',flush=True)
 time.sleep(0.55)
# Optional limited discovery of additional Sayadaw MP3 index pages if 1000 not yet reached.
if len(records)<1000:
 try:
  index='https://www.dhammadownload.com/AudioInMyanmar.htm'
  resp=session.get(index,timeout=25);resp.raise_for_status();soup=BeautifulSoup(resp.text,'html.parser')
  allindex=set(m['source_url'].lower().replace('www.','') for m in MONKS)
  found=[]
  for a in soup.find_all('a',href=True):
   ref=urljoin(index,a['href']);parts=urlsplit(ref)
   if (parts.hostname or '').lower() not in ALLOWED:continue
   if '.htm' not in parts.path.lower() or 'mp3' not in parts.path.lower():continue
   if ref.lower().replace('www.','') in allindex:continue
   label=' '.join(a.stripped_strings)
   if not label or len(label)<4:continue
   found.append((ref,label[:110]));allindex.add(ref.lower().replace('www.',''))
  for j,(url,name) in enumerate(found[:55],1):
   n=scrape(url,0,name);print(f'DISCOVERY {j} +{n} total={len(records)}',flush=True);time.sleep(.4)
   if len(records)>=2000:break
 except Exception as exc:print('Discovery could not be completed:',exc,flush=True)
assets=ASSETS/'full_catalog.json';assets.write_text(json.dumps(list(records.values()),ensure_ascii=False,separators=(',',':')),encoding='utf8')
report={'unique_mp3_links':len(records),'target_1000_reached':len(records)>=1000,'seed_mp3_count':len(SEEDS),'source_pages_attempted':len(audit),'successful_page_responses':sum(x['status']=='ok' for x in audit),'failed_page_responses':sum(x['status']!='ok' for x in audit),'audio_files_in_apk':0,'audio_playback_verified':0,'note':'URL catalog only; links extracted from third-party site, current stream status not verified; online streaming requires internet; user-initiated downloads stored private on device.','sources':audit}
(ROOT/'catalog_build_report.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf8')
with (ROOT/'catalog_links_export.csv').open('w',newline='',encoding='utf-8-sig') as file:
 writer=csv.DictWriter(file,fieldnames=['id','monk_id','monk','title','category','url','listing_url','status']);writer.writeheader();writer.writerows(records.values())
print('CATALOG FINAL:',len(records),'URLS', 'TARGET_1000',report['target_1000_reached'],flush=True)