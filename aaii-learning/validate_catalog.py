import json,re
from pathlib import Path
p=Path(__file__).resolve().parent/'app/src/main/assets'
a=json.loads((p/'courses.json').read_text(encoding='utf-8'))
b=json.loads((p/'roadmaps.json').read_text(encoding='utf-8'))
c=json.loads((p/'programs_public.json').read_text(encoding='utf-8'))
t=json.loads((p/'teacher_paths.json').read_text(encoding='utf-8'))
assert len(a)==337 and len(b)==18 and len(c)==55 and len(t)>=5
assert sum(x['type']=='Specialization' for x in c)==37 and sum(x['type']=='Professional Certificate' for x in c)==18
for f in p.glob('*.json'):
 s=f.read_text(encoding='utf-8')
 assert '/account/accomplishments/' not in s, f
 assert 'certificateId' not in s and 'certificateUrl' not in s, f
 json.loads(s)
print('PASS: 337 courses, 37 specializations, 18 professional, teacher learning paths. No private URLs.')
