#!/usr/bin/env python3
"""Best effort download only explicitly documented Commons portraits.
Asset fallbacks are symbolic lotuses and are not misrepresented as actual portraits.
"""
from pathlib import Path
from urllib.parse import quote
import io,json
import requests
from PIL import Image,ImageOps
ROOT=Path(__file__).resolve().parents[1]
TARGET=ROOT/'app/src/main/res/drawable-nodpi'
ITEMS=[
  {'name':'mogok_sayadaw','file':'Mogok Sayadaw portrait.jpg','credit':'Wikimedia Commons, photographer unknown, purported public domain in Myanmar; https://commons.wikimedia.org/wiki/File:Mogok_Sayadaw_portrait.jpg'},
  {'name':'mahasi_sayadaw','file':'Mahasi Sayadaw.jpg','credit':'Wikimedia Commons, photographer unknown, purported public domain in Myanmar; https://commons.wikimedia.org/wiki/File:Mahasi_Sayadaw.jpg'},
  {'name':'nandamala_sayadaw','file':'Sayadaw nandamalabhivamsa.jpg','credit':'Wikimedia Commons, author Kiwiladi, CC BY-SA 4.0; https://commons.wikimedia.org/wiki/File:Sayadaw_nandamalabhivamsa.jpg'}
]
for x in ITEMS:
 try:
  url='https://commons.wikimedia.org/wiki/Special:FilePath/'+quote(x['file'])+'?width=360'
  data=requests.get(url,headers={'User-Agent':'DamaRadioApp/2.0 (educational catalog; https://github.com/aiacademyanmar/soemoe)'},timeout=25)
  data.raise_for_status()
  img=Image.open(io.BytesIO(data.content)).convert('RGB')
  if img.width<90 or img.height<90:raise ValueError('image too small')
  img=ImageOps.fit(img,(280,280),method=Image.Resampling.LANCZOS)
  img.save(TARGET/(x['name']+'.jpg'),quality=85,optimize=True)
  x['downloaded']=True
 except Exception as ex:
  x['downloaded']=False;x['error']=str(ex)[:150]
 print(x['name'],'photo received' if x['downloaded'] else 'using symbolic fallback',flush=True)
(ROOT/'photo_license_report.json').write_text(json.dumps(ITEMS,ensure_ascii=False,indent=2),encoding='utf8')
(ROOT/'app/src/main/assets/portrait_credits.txt').write_text('\n'.join(x['name']+': '+x['credit']+' | downloaded='+str(x['downloaded']) for x in ITEMS)+'\n',encoding='utf8')