"""Static integrity checks; deliberately do not claim to validate rendering or gameplay."""
import json,math,struct,zlib
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'src/main/resources'
ASSETS=RES/'assets/truchosafio4'
checks=0
def check(condition,message):
 global checks
 checks+=1
 if not condition: raise AssertionError(message)
def png(path):
 data=path.read_bytes();check(data[:8]==b'\x89PNG\r\n\x1a\n',str(path))
 at=8;payload=b'';width=height=None
 while at<len(data):
  size=struct.unpack('>I',data[at:at+4])[0];kind=data[at+4:at+8];chunk=data[at+8:at+8+size]
  check(zlib.crc32(kind+chunk)&0xffffffff==struct.unpack('>I',data[at+8+size:at+12+size])[0],f'PNG CRC {path}')
  if kind==b'IHDR':width,height=struct.unpack('>II',chunk[:8])
  if kind==b'IDAT':payload+=chunk
  at+=12+size
 check(bool(zlib.decompress(payload)),f'PNG data {path}')
 return width,height
for path in RES.rglob('*.json'):json.loads(path.read_text())
models=list((ASSETS/'geo').glob('*.geo.json'));check([p.name for p in models]==['flashbang.geo.json'],'only Flashbang model')
for model in models:
 name=model.name.split('.')[0];geo=json.loads(model.read_text())['minecraft:geometry'][0]
 bones={b['name'] for b in geo['bones']};check(len(bones)==len(geo['bones']),f'duplicate bones {name}')
 for bone in geo['bones']:
  check('parent' not in bone or bone['parent'] in bones,f'bone parent {name}')
  seen=set();current=bone
  while current.get('parent'):
   check(current['name'] not in seen,f'cyclic hierarchy {name}');seen.add(current['name'])
   current=next(b for b in geo['bones'] if b['name']==current['parent'])
 texture=ASSETS/'textures/entity'/f'{name}.png';dims=png(texture)
 check(dims==(geo['description']['texture_width'],geo['description']['texture_height']),f'atlas size {name}')
 animations=json.loads((ASSETS/'animations'/f'{name}.animation.json').read_text())['animations']
 for key in ['fly','walk','special']:check('animation.creature.'+key in animations,f'missing animation {name}/{key}')
 for key,anim in animations.items():
  check(anim['animation_length']>0,f'duration {name}/{key}')
  for bone,channels in anim['bones'].items():
   check(bone in bones,f'unknown animated bone {name}/{key}/{bone}')
   for channel,frames in channels.items():
    check(channel in ['rotation','position','scale'],f'channel {channel}')
    for time,value in frames.items():
     check(0<=float(time)<=anim['animation_length'],f'time {name}/{key}')
     vector=value['vector'] if isinstance(value,dict) else value
     check(len(vector)==3 and all(isinstance(n,(int,float)) and math.isfinite(n) for n in vector),f'keyframe {name}/{key}')
flash=json.loads((ASSETS/'geo/flashbang.geo.json').read_text())['minecraft:geometry'][0]
cubes=[c for b in flash['bones'] for c in b.get('cubes',[])]
check(len(cubes)==2 and all(c['size'].count(0)==1 for c in cubes),'Flashbang has zero thickness on both planes')
faces={side:uv for c in cubes for side,uv in c['uv'].items()}
check(set(faces)=={'north','south','east','west'},'4 directional faces')
check(len({tuple(v['uv']) for v in faces.values()})==4,'4 distinct UV regions')
conf=json.loads((ROOT/'config-ejemplo/truchosafio4.json').read_text())
check(not conf['natural_spawning_enabled'],'natural spawn off')
check(all(not m['natural_spawn_enabled'] and not any(m['dimension_enabled'].values()) for m in conf['mobs'].values()),'all dimension spawns off')
check(set(conf['mobs'])=={'flashbang'} and 'qumara' not in conf,'only Flashbang configuration')
check(not (RES/'data/truchosafio4/dimension').exists(),'no Limbo dimension')
mod=json.loads((RES/'fabric.mod.json').read_text())
for classes in mod['entrypoints'].values():
 for cls in classes:check((ROOT/'src/main/java'/Path(cls.replace('.','/')+'.java')).exists(),f'entrypoint {cls}')
for mixin in mod.get('mixins',[]):
 cfg=json.loads((RES/mixin).read_text())
 for cls in cfg.get('client',[]):check((ROOT/'src/main/java'/Path((cfg['package']+'.'+cls).replace('.','/')+'.java')).exists(),f'mixin {cls}')
for sound in (ASSETS/'sounds').glob('*.ogg'):check(sound.read_bytes().startswith(b'OggS'),f'Ogg signature {sound}')

check({p.name for p in (ASSETS/'textures/entity').glob('*.png')}=={'flashbang.png'},'only Flashbang entity texture')
check({p.name for p in (ASSETS/'models/item').glob('*.json')}=={'flashbang_spawn_egg.json'},'only Flashbang egg model')
check(not mod.get('mixins'),'no control mixins')
print(f'PASS: {checks} Flashbang resource integrity assertions; not an in-game render or gameplay test')
