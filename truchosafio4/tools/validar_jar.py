"""Reject accidental leftover creatures, items or systems in the actual remapped delivery."""
from pathlib import Path
import zipfile,json
root=Path(__file__).resolve().parents[1]
jar=root/'build/libs/truchosafio4-flashbang-0.3.0-flashbang.jar'
allowed={
 'Truchosafio4','client/CreatureGeoModel','client/CreatureGeoRenderer','client/FlashOverlay','client/Truchosafio4Client',
 'command/ModCommands','config/ModConfig','config/ModConfig$MobRule','entity/AnimatedHostileEntity',
 'entity/FlashbangEntity','entity/FlashbangEntity$Chase','network/FlashPayload','registry/ModEntities','registry/ModItems','system/SpawnManager'}
with zipfile.ZipFile(jar) as z:
 assert z.testzip() is None
 names=z.namelist()
 classes={n.removeprefix('dev/truchosafio4/').removesuffix('.class') for n in names if n.startswith('dev/truchosafio4/') and n.endswith('.class')}
 assert classes==allowed,(classes-allowed,allowed-classes)
 for directory,suffix,expected in [('geo','.geo.json',{'flashbang.geo.json'}),('animations','.animation.json',{'flashbang.animation.json'}),('textures/entity','.png',{'flashbang.png'}),('models/item','.json',{'flashbang_spawn_egg.json'})]:
  prefix='assets/truchosafio4/'+directory+'/'
  assert {n.removeprefix(prefix) for n in names if n.startswith(prefix) and n.endswith(suffix)}==expected
 mod=json.loads(z.read('fabric.mod.json'));assert not mod.get('mixins')
 assert mod['version']=='0.3.0-flashbang'
 assert not any(n.startswith('dev/truchosafio4/smoke/') for n in names)
print('PASS: remapped JAR contains exactly the 15 Flashbang classes and exclusive resources; no other mod systems or test code')
