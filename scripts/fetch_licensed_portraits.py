#!/usr/bin/env python3
"""Retrieve small, credited real photos from documented Wikimedia Commons file pages.
Do not label a generated illustration as a real portrait when the photo is unavailable.
"""
from pathlib import Path
from urllib.parse import quote
import io,json
import requests
from PIL import Image,ImageOps
ROOT=Path(__file__).resolve().parents[1]
TARGET=ROOT/'app/src/main/res/drawable-nodpi'
TARGET.mkdir(parents=True,exist_ok=True)
ITEMS=[
  {'name':'mogok_sayadaw','file':'Mogok Sayadaw portrait.jpg','author':'Unknown photographer','license':'Public domain (Myanmar status; verify jurisdictions)','source':'https://commons.wikimedia.org/wiki/File:Mogok_Sayadaw_portrait.jpg'},
  {'name':'mahasi_sayadaw','file':'Mahasi Sayadaw.jpg','author':'Unknown photographer','license':'Public domain (Myanmar status; verify jurisdictions)','source':'https://commons.wikimedia.org/wiki/File:Mahasi_Sayadaw.jpg'},
  {'name':'nandamala_sayadaw','file':'Sayadaw nandamalabhivamsa.jpg','author':'Kiwiladi','license':'CC BY-SA 4.0','source':'https://commons.wikimedia.org/wiki/File:Sayadaw_nandamalabhivamsa.jpg'},
  {'name':'sitagu_sayadaw','file':'Sitagu Sayadaw.jpg','author':'Pete Souza / The White House','license':'Public domain — US federal government','source':'https://commons.wikimedia.org/wiki/File:Sitagu_Sayadaw.jpg','profile_name_contains':'သီတဂူ'},
  {'name':'paauk_sayadaw','file':'Venerable Pa-Auk Tawya Sayadaw Bhaddanta Āciṇṇa.jpg','author':'Paingpeace','license':'CC BY-SA 4.0','source':'https://commons.wikimedia.org/wiki/File:Venerable_Pa-Auk_Tawya_Sayadaw_Bhaddanta_Āciṇṇa.jpg','profile_name_contains':'ဖားအောက်'}
]
for item in ITEMS:
 try:
  url='https://commons.wikimedia.org/wiki/Special:FilePath/'+quote(item['file'])+'?width=400'
  response=requests.get(url,headers={'User-Agent':'DamaLightGallery/22 (https://github.com/aiacademyanmar/soemoe; educational portrait attribution)'},timeout=35)
  response.raise_for_status()
  if len(response.content)>12_000_000:raise ValueError('photo too large')
  photo=ImageOps.exif_transpose(Image.open(io.BytesIO(response.content))).convert('RGB')
  if min(photo.size)<120:raise ValueError('image too small')
  photo=ImageOps.fit(photo,(400,400),method=Image.Resampling.LANCZOS)
  photo.save(TARGET/(item['name']+'.jpg'),quality=87,optimize=True)
  item['downloaded']=True
 except Exception as e:
  item['downloaded']=False;item['error']=str(e)[:160]
 print('PORTRAIT',item['name'],'OK' if item['downloaded'] else 'FALLBACK',flush=True)
(ROOT/'photo_license_report.json').write_text(json.dumps(ITEMS,ensure_ascii=False,indent=2),encoding='utf8')
credits=ROOT/'app/src/main/assets/portrait_credits.txt'
credits.write_text('\n'.join(f"{p['name']}: {p['author']} | {p['license']} | {p['source']} | downloaded={p['downloaded']}" for p in ITEMS)+'\n',encoding='utf8')
