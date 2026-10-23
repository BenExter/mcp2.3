"""Export editable Blockbench projects; canonical runtime inputs remain geo/animation JSONs."""
import base64,json,uuid
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'src/main/resources/assets/truchosafio4'
OUT=ROOT/'modelos-blockbench'
OUT.mkdir(exist_ok=True)
def uid(name):return str(uuid.uuid5(uuid.NAMESPACE_URL,'truchosafio4:'+name))
for path in sorted((ASSETS/'geo').glob('*.geo.json')):
 name=path.name.split('.')[0];geo=json.loads(path.read_text())['minecraft:geometry'][0]
 texture=(ASSETS/'textures/entity'/f'{name}.png').read_bytes()
 elements=[];groups={};roots=[]
 for bone in geo['bones']:
  group={'name':bone['name'],'origin':bone['pivot'],'rotation':bone.get('rotation',[0,0,0]),'uuid':uid(name+'/'+bone['name']),'export':True,'isOpen':True,'children':[]}
  groups[bone['name']]=group
  for i,cube in enumerate(bone.get('cubes',[])):
   key=uid(name+'/'+bone['name']+'/'+str(i));origin=cube['origin'];size=cube['size'];uv=cube['uv']
   if isinstance(uv,list):
    u,v=uv;dx,dy,dz=size
    uv={k:{'uv':at,'uv_size':s} for k,at,s in [('east',[u,v+dz],[dz,dy]),('north',[u+dz,v+dz],[dx,dy]),('west',[u+dz+dx,v+dz],[dz,dy]),('south',[u+2*dz+dx,v+dz],[dx,dy]),('up',[u+dz,v],[dx,dz]),('down',[u+dz+dx,v+dz],[dx,-dz])]}
   faces={k:{'uv':[d['uv'][0],d['uv'][1],d['uv'][0]+d['uv_size'][0],d['uv'][1]+d['uv_size'][1]],'texture':0} for k,d in uv.items()}
   for side in ['north','south','east','west','up','down']:faces.setdefault(side,{'uv':[0,0,0,0],'texture':None})
   elements.append({'name':bone['name']+'_'+str(i),'type':'cube','uuid':key,'from':origin,'to':[a+b for a,b in zip(origin,size)],'origin':cube.get('pivot',bone['pivot']),'rotation':cube.get('rotation',[0,0,0]),'inflate':cube.get('inflate',0),'box_uv':False,'faces':faces})
   group['children'].append(key)
 for bone in geo['bones']:
  group=groups[bone['name']]
  if bone.get('parent'):groups[bone['parent']]['children'].append(group)
  else:roots.append(group)
 animations=[]
 for anim_name,animation in json.loads((ASSETS/'animations'/f'{name}.animation.json').read_text())['animations'].items():
  animators={}
  for bone,channels in animation['bones'].items():
   frames=[]
   for channel,keyframes in channels.items():
    for time,value in keyframes.items():
     vector=value.get('vector') if isinstance(value,dict) else value
     frames.append({'channel':channel,'data_points':[dict(zip(['x','y','z'],map(str,vector)))],'uuid':uid(name+anim_name+bone+channel+time),'time':float(time),'color':-1,'interpolation':'catmullrom' if isinstance(value,dict) else 'linear'})
   animators[groups[bone]['uuid']]={'name':bone,'type':'bone','keyframes':frames}
  animations.append({'uuid':uid(name+anim_name),'name':anim_name,'loop':'loop' if animation.get('loop') else 'once','override':False,'length':animation['animation_length'],'snapping':20,'animators':animators})
 model={'meta':{'format_version':'4.10','model_format':'bedrock','box_uv':False},'name':name,'model_identifier':'truchosafio4.'+name,'visible_box':[geo['description'].get('visible_bounds_width',4),geo['description'].get('visible_bounds_height',4),geo['description'].get('visible_bounds_offset',[0,0,0])[1]],'resolution':{'width':geo['description']['texture_width'],'height':geo['description']['texture_height']},'elements':elements,'outliner':roots,'textures':[{'name':name+'.png','uuid':uid(name+'/texture'),'id':'0','mode':'bitmap','source':'data:image/png;base64,'+base64.b64encode(texture).decode()}],'animations':animations}
 (OUT/f'{name}.bbmodel').write_text(json.dumps(model,indent=2)+'\n')
print('Exported 8 editable Blockbench projects with embedded textures and animations')
