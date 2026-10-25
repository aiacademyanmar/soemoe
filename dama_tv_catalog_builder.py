#!/usr/bin/env python3
"""DAMA TV catalog. Collects YouTube video ID/title/link from public channels/playlists.
No download of videos/audio. Real IDs only; row-level provenance; dedupe by ID.
"""
import csv,json,re,sys,time,collections
from pathlib import Path
from yt_dlp import YoutubeDL
OUT=Path("dama_tv_catalog")
OUT.mkdir(exist_ok=True)
sources=[
("Dhamma Channel (multi-Sayadaw)","channel","https://www.youtube.com/channel/UCgWCAiNyLCT1H1V3lpQT1tA/videos"),
("Dhamma Channel: U Sumanagala","channel","https://www.youtube.com/channel/UCLGuEng1lp44Xu7nZ1bp9jQ/videos"),
("Dhamma Yatha","channel","https://www.youtube.com/channel/UChrClAAAfeZNw0N6XRTq8JQ/videos"),
("STT Note — Buddhism","channel","https://www.youtube.com/channel/UCzutQhn_6cc1rV1EdeqX_iQ/videos"),
("Nandamalabhivamsa — teaching","channel","https://www.youtube.com/channel/UC2JtW2mL0rQ-mnK6U1VSQww/videos"),
("Tayar Taw channel","channel","https://www.youtube.com/@tayartaw3853/videos"),
("Dhamma Yatha — sermon collection","channel","https://www.youtube.com/channel/UCNtVU5TCKSy-b8RKMB6A2Dg/videos"),
("BuddhaDhamma Myanmar","channel","https://www.youtube.com/channel/UCPxUXfbroDTTs1SO2K5GOtQ/videos"),
("Patthana — Nandamala 78 talks","playlist","https://www.youtube.com/playlist?list=PLKBA2DIB9OQeCgpQNwbkDVdvwufgR4f_F"),
("Patthana Lectures","playlist","https://www.youtube.com/playlist?list=PL8wdiyqqpfrrrDe2p0hzldKIM6fEOg1Df"),
("Kusalattika Patthana","playlist","https://www.youtube.com/playlist?list=PLtYBvkguVF4ZL5Zf2pf2evtZbeHYVpa5T")
]
monks=[
("ဒယ်အိုးဆရာတော် ဦးသုမင်္ဂလ",["ဒယ်အိုး","သုမင်္ဂလ","sumangala","sumanagala"]),
("ပါမောက္ခချုပ်ဆရာတော် နန္ဒမာလာဘိဝံသ",["နန္ဒမာလာ","ပါချုပ်","nandamala"]),
("မိုးကုတ်ဆရာတော် ဦးဝိမလ",["မိုးကုတ်","mogok","u vimala"]),
("မဟာစည်ဆရာတော် ဦးသောဘန",["မဟာစည်","mahasi","sobhana"]),
("မင်းကွန်းဆရာတော် ဦးဝိစိတ္တသာရာဘိဝံသ",["မင်းကွန်း","mingun"]),
("သီတဂူဆရာတော် ဦးဉာဏိဿရ",["သီတဂူ","sitagu","nyanissara"]),
("ဖားအောက်ဆရာတော် အရှင်အာစိဏ္ဏ",["ဖားအောက်","pa auk","pa-auk"]),
("သစ္စာရွှေစည်ဆရာတော် အရှင်ဥတ္တမ",["သစ္စာရွှေစည်","ဦးဥတ္တမ","သစ္စာ ရွှေစည်"]),
("မြောက်ဦးဆရာတော် အရှင်ဝါယာမိန္ဒ",["မြောက်ဦး","mrauk","ဝါယာမိန္ဒ"]),
("ချမ်းမြေ့ဆရာတော် အရှင်ဇနကာဘိဝံသ",["ချမ်းမြေ့","chanmyay","janakabhivamsa"]),
("ကြာနီကန်ဆရာတော် ဦးဇဠိလ",["ကြာနီကန်","ဇဠိလ"]),
("ဓမ္မဒူတဆရာတော် အရှင်ဆေကိန္ဒ",["ဆေကိန္ဒ","sekinda"]),
("အောက်စဖို့ဒ်ဆရာတော် ဒေါက်တာဓမ္မသာမိ",["အောက်စဖို့ဒ်","dhammasami"]),
("မဟာဗောဓိမြိုင်ဆရာတော်",["မဟာဗောဓိမြိုင်","mahabodhi myaing"]),
("တောင်တန်းသာသနာပြုဆရာတော် ဦးဥတ္တမသာရ",["တောင်တန်းသာသနာ","ဥတ္တမသာရ"]),
("ဒေါက်တာစိုးလွင် (မန္တလေး)",["ဒေါက်တာစိုးလွင်","dr soe lwin"]),
]
topics=[
("ပဋ္ဌာန်း",["ပဋ္ဌာန်း","ပဌာန်း","pahtan","patthana","pattana"]),
("အဘိဓမ္မာ",["အဘိဓမ္မာ","abhidhamma"]),
("ပဋိစ္စသမုပ္ပါဒ်",["ပဋိစ္စ","paticca","dependent origination"]),
("ဝိပဿနာ / တရားထိုင်",["ဝိပဿနာ","ဝိပဿန","vipassana","meditation","သတိပဋ္ဌာန်","တရားထိုင်","mindfulness"]),
("ပရိတ် / ပူဇော်",["ပရိတ်","ဂါထာ","paritta","metta sutta","မေတ္တာသုတ်","ရွတ်ဖတ်","ဂုဏ်တော်"]),
("သုတ္တန်",["သုတ်","sutta","sutra","dhammapada","ဓမ္မစကြာ","မင်္ဂလသုတ်"]),
("ဗုဒ္ဓဝင် / သာသနာ",["ဗုဒ္ဓဝင်","ဘုရားရှင်","သံဃာ","သာသနာ","buddha life"]),
("ဓမ္မအမေးအဖြေ",["အမေးအဖြေ","q&a","question and answer","မေးမြန်း"]),
]
def detect_monk(title):
 t=title.casefold()
 for name,keys in monks:
  if any(k.casefold() in t for k in keys):return name,"video_title"
 return "ဆရာတော် မသတ်မှတ်ရသေး","not_explicit"
def detect_category(title):
 t=title.casefold()
 for name,keys in topics:
  if any(k.casefold() in t for k in keys):return name
 return "အထွေထွေ ဓမ္မတရား"
validid=re.compile(r"^[A-Za-z0-9_-]{11}$")
found={}
errors=[]
for i,(name,kind,url) in enumerate(sources,1):
 print(f"[{i}/{len(sources)}] EXTRACT {name}",flush=True)
 count=0
 try:
  opts={'quiet':True,'skip_download':True,'extract_flat':'in_playlist','ignoreerrors':True,
        'no_warnings':True,'socket_timeout':14,'retries':1,'playlistend':500,
        'extractor_retries':1,'lazy_playlist':True,
        'http_headers':{'Accept-Language':'en-US,en;q=0.9'}}
  with YoutubeDL(opts) as ydl:
   info=ydl.extract_info(url,download=False)
   if info is None:raise RuntimeError("no info")
   entries=info.get('entries') or []
   for e in entries:
    if not e:continue
    id=e.get('id') or ''
    if not validid.fullmatch(id):continue
    if id in found:continue
    title=(e.get('title') or '').strip()
    if not title or title.startswith('https://'):title="(title unavailable)"
    monk,evidence=detect_monk(title)
    # For dedicated channel with explicit focus, override only if source itself is single-monk
    if monk=="ဆရာတော် မသတ်မှတ်ရသေး" and "U Sumanagala" in name:
     monk="ဒယ်အိုးဆရာတော် ဦးသုမင်္ဂလ";evidence="channel_focus"
    if monk=="ဆရာတော် မသတ်မှတ်ရသေး" and "Nandamalabhivamsa" in name:
     monk="ပါမောက္ခချုပ်ဆရာတော် နန္ဒမာလာဘိဝံသ";evidence="channel_focus"
    found[id]={'video_id':id,'title':title,'sayadaw':monk,'sayadaw_evidence':evidence,
     'category':detect_category(title),'youtube_url':'https://www.youtube.com/watch?v='+id,
     'source_channel_or_playlist':name,'source_url':url,
     'upload_channel':e.get('channel') or e.get('uploader') or name,
     'duration_seconds':e.get('duration') or '',
     'availability':'publisher_index_only_not_playback_checked'}
    count+=1
    if count%100==0:print("  NEW",count,"GLOBAL",len(found),flush=True)
 except Exception as exc:
  errors.append({'source':name,'url':url,'error':str(exc)[:350]})
  print('ERROR',name,str(exc)[:140],flush=True)
 print('DONE',name,count,'GLOBAL',len(found),flush=True)
 # Save incremental work
 (OUT/'youtube_video_catalog.json').write_text(json.dumps(list(found.values()),ensure_ascii=False,indent=1),encoding='utf8')
rows=list(found.values())
with (OUT/'youtube_videos.csv').open('w',encoding='utf-8-sig',newline='') as f:
 w=csv.DictWriter(f,fieldnames=['video_id','title','sayadaw','sayadaw_evidence','category','youtube_url','source_channel_or_playlist','source_url','upload_channel','duration_seconds','availability'])
 w.writeheader();w.writerows(rows)
stats={'video_count':len(rows),'target_1000_reached':len(rows)>=1000,'category_counts':dict(collections.Counter(v['category'] for v in rows)),
       'sayadaw_counts':dict(collections.Counter(v['sayadaw'] for v in rows)),
       'source_count':len(sources),'failures':errors,'scope':'Video ID + title + source URL; no video media downloaded, URL playback not individually checked'}
(OUT/'report.json').write_text(json.dumps(stats,ensure_ascii=False,indent=2),encoding='utf8')
(OUT/'sources.json').write_text(json.dumps([{'name':n,'type':k,'url':u} for n,k,u in sources],ensure_ascii=False,indent=2),encoding='utf8')
print('FINAL',len(rows),'VIDEOS;',len(errors),'ERRORS','OVER1000',len(rows)>=1000,flush=True)
