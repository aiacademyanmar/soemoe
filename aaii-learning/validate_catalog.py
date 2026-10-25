import json,re
from pathlib import Path
d=Path(__file__).resolve().parent/'app/src/main/assets'
c=json.loads((d/'courses.json').read_text(encoding='utf-8'))
r=json.loads((d/'roadmaps.json').read_text(encoding='utf-8'))
assert len(c)==337,len(c)
assert len(r)==18,len(r)
for x in (c,r):
  for o in x:
    assert set(o.keys()).isdisjoint({'certificateUrl','certificateId','notificationDate','completionDate','verifiedInCoursera','source','page'}),o
    assert not any('/account/accomplishments/' in str(v) for v in o.values()),o
    u=o.get('url','')
    assert not u or bool(re.match(r'https://www\.coursera\.org/(learn|specializations|professional-certificates)/',u))
print('Validated:',len(c),'public course titles,',len(r),'public roadmaps, no personal certificate IDs')
