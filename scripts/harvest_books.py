#!/usr/bin/env python3
"""Harvest publisher-linked book metadata only (never the copyrighted books)."""
from pathlib import Path
from urllib.parse import urlparse,urljoin,urldefrag,unquote
from collections import deque,Counter
import requests,bs4,re,json,csv,time,hashlib,random
ROOT=Path(__file__).resolve().parents[1]
DIR=ROOT/'app/src/main/assets';DIR.mkdir(parents=True,exist_ok=True)
ROOTURL='https://www.dhammadownload.com/'
SEEDS=[ROOTURL+'eBook-Myanmar.htm', ROOTURL+'eBookInEnglish.htm', ROOTURL+'eBook-English.htm', ROOTURL+'Pahtan-eBookInMyanmar.htm',ROOTURL+'The-Ministry-Of-Religious-Affairs-eBookInMyanmar.htm']
allowed={'dhammadownload.com','www.dhammadownload.com','ftp.dhammadownload.com'}
PDF_EXT=('.pdf','.epub','.mobi','.doc','.docx','.zip','.rar')
RETRY=2;MAX_PAGES=240;MAX_LINKS=12000
stopwords={'eBook','PDF','Download','download','Click Here','Click to Download','Image','to download in PDF file','PDF File'}
client=requests.Session();client.headers.update({'User-Agent':'DAMA-Library/1.0 (educational catalog of existing public download links; https://github.com/aiacademyanmar/soemoe)','Accept-Language':'my,en;q=0.7'})
books={};queue=deque((x,'General Books',0) for x in SEEDS);visited=set();errors=[];parents={}

def scope(url):
 try:
  p=urlparse(url);return p.scheme in ('http','https') and (p.hostname or '').lower() in allowed
 except:return False

def canon(url):
 url,_=urldefrag(url);p=urlparse(url);return p._replace(scheme='https',netloc=p.netloc.lower().removeprefix('www.'),query='' if p.path.lower().endswith(PDF_EXT) else p.query).geturl()

def file_url(url):
 p=urlparse(url);return unquote(p.path).lower().endswith(PDF_EXT)

def valid_page(url):
 p=urlparse(url);v=p.path.lower()
 return v.endswith(('.htm','.html','.php')) and 'ebook' in v or 'ebook' in p.path.lower() or 'pali' in v or 'pahtan' in v or 'tipitaka' in v or 'pitr' in v or 'religious-affairs' in v

def title_from(a,url):
 s=' '.join(a.stripped_strings)
 if not s or s in stopwords or len(s)<6:
  holder=a.parent
  for _ in range(2):
   if holder is None:break
   st=holder.get_text(' ',strip=True)
   if 8<len(st)<180:s=st;break
   holder=holder.parent
 if s in stopwords or not s:s=unquote(urlparse(url).path.rsplit('/',1)[-1]).rsplit('.',1)[0].replace('-',' ').replace('_',' ')
 return s[:220]

def category(page,title):
 t=(page+' '+title).lower()
 for words,category in [('pahtan patthana ပဋ္ဌာန်း','ပဋ္ဌာန်း'),('abhidhamma အဘိဓမ္မာ','အဘိဓမ္မာ'),('tipitaka ပိဋကတ်','ပိဋကတ်သုံးပုံ'),('ministry-of-religious သာသနာရေးဝန်ကြီး','ဝန်ကြီးဌာန'),('vipassana ဝိပဿနာ','ဝိပဿနာ'),('paritta ပရိတ်','ပရိတ်/ဝတ်ရွတ်'),('pali ပါဠိ','ပါဠိ')]:
  if any(x in t for x in words.split()):return category
 return 'ဓမ္မစာအုပ်'

for attempt in range(1):
 while queue and len(visited)<MAX_PAGES and len(books)<MAX_LINKS:
  url,author,depth=queue.popleft();u=canon(url)
  if u in visited or not scope(u) or depth>3:continue
  visited.add(u)
  response=None
  for tr in range(RETRY):
   try:
    response=client.get(url,timeout=(7,23));response.raise_for_status()
    if len(response.content)>7_000_000:raise ValueError('Page too big')
    break
   except Exception as e:
    if tr==RETRY-1:errors.append({'url':url,'error':str(e)[:130]})
    time.sleep(.3)
  if response is None or response.status_code!=200:continue
  if 'html' not in response.headers.get('Content-Type','text/html').lower():continue
  # BS4 respects meta charset, but decoding damaged headers may produce garbage. Prefer apparent_encoding.
  if not response.encoding or response.encoding.lower() in ('iso-8859-1','windows-1252'):
   response.encoding=response.apparent_encoding or 'utf-8'
  soup=bs4.BeautifulSoup(response.text,'html.parser')
  if not soup.title: page_name=author
  else:page_name=soup.title.get_text(' ',strip=True)[:110]
  found_here=0
  for a in soup.find_all('a',href=True):
   target=urljoin(url,a['href'].strip());can=canon(target)
   if not scope(can):continue
   title=title_from(a,target)
   if file_url(target):
    if can in books:continue
    ext=unquote(urlparse(target).path).split('.')[-1].lower()
    books[can]={'id':hashlib.sha1(can.encode()).hexdigest()[:14], 'title':title, 'author':author or page_name, 'category':category(url,title), 'format':ext.upper(), 'url':target,'listing_url':url,'link_status':'unverified_direct_link','cover_key':'lotus'}
    found_here+=1
   elif depth<3 and valid_page(can):
    if can not in visited and len(queue)<1500:
     child_author=title if depth==0 and len(title)>8 and not title.lower().startswith(('image','click')) else author
     queue.append((target,child_author,depth+1))
  print(f'PAGE {len(visited):03d}: +{found_here:03d} total={len(books):04d} queue={len(queue):03d} {url}',flush=True)
  time.sleep(.30)

rows=sorted(books.values(),key=lambda x:(x['category'],x['author'],x['title']))
(DIR/'books_catalog.json').write_text(json.dumps(rows,ensure_ascii=False,separators=(',',':')),encoding='utf-8')
export=ROOT/'book_links_export.csv'
with export.open('w',encoding='utf-8-sig',newline='') as f:
 w=csv.DictWriter(f,fieldnames=['id','title','author','category','format','url','listing_url','link_status','cover_key']);w.writeheader();w.writerows(rows)
report={'unique_book_links':len(rows),'html_pages_checked':len(visited),'errors':errors[:35], 'source':'DhammaDownload eBook pages','formats':dict(Counter(x['format'] for x in rows)), 'categories':dict(Counter(x['category'] for x in rows)), 'url_listing_only':True,'actual_file_downloaded':0,'notes':'Metadata/links indexed offline; book files are NOT embedded or redistributed. Unverified individual link availability.'}
(ROOT/'books_build_report.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
print('DONE',json.dumps({k:v for k,v in report.items() if k not in ('errors','categories')},ensure_ascii=False),flush=True)