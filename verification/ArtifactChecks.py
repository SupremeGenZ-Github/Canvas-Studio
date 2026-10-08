"""Validate the shipped JAR's data, item assets, metadata and preserved source."""
from pathlib import Path
import json,zipfile,hashlib
root=Path(__file__).resolve().parents[1]
jar=root/'build/libs/canvas-studio-plus-26.3-2.1.0.jar'
names=['canvas','molder_canvas','infinity_canvas','quill','molder_quill','infinity_quill']
with zipfile.ZipFile(jar) as z:
 assert z.testzip() is None
 files=set(z.namelist());lang=json.loads(z.read('assets/canvasstudio/lang/en_us.json'))
 textures=[]
 for name in names:
  assert lang['item.canvasstudio.'+name]
  item=json.loads(z.read(f'assets/canvasstudio/items/{name}.json'))
  assert item['model']['model']==f'canvasstudio:item/{name}'
  model=json.loads(z.read(f'assets/canvasstudio/models/item/{name}.json'))
  assert model['textures']['layer0']==f'canvasstudio:item/{name}'
  textures.append(hashlib.sha256(z.read(f'assets/canvasstudio/textures/item/{name}.png')).hexdigest())
  r=json.loads(z.read(f'data/canvasstudio/recipe/{name}.json'))
  assert r['result']=={'id':'canvasstudio:'+name,'count':1}
  if name.endswith('canvas') or name=='canvas':
   assert r['type']=='minecraft:crafting_shapeless'
   q='quill' if name=='canvas' else name.replace('canvas','quill')
   assert sorted(r['ingredients'])==sorted(['minecraft:leather','minecraft:paper','canvasstudio:'+q])
  elif name!='quill':
   assert r['type']=='minecraft:crafting_shaped' and r['pattern']==['MMM','MQM','MMM']
   assert r['key']=={'M':'minecraft:'+('iron_nugget' if name=='molder_quill' else 'diamond'),'Q':'canvasstudio:quill'}
  a=json.loads(z.read(f'data/canvasstudio/advancement/recipes/{name}.json'))
  assert a['rewards']['recipes']==['canvasstudio:'+name]
  assert a['criteria']['has_the_recipe']['conditions']['recipes']==['canvasstudio:'+name]
 assert len(set(textures))==6
 assert not any('canvas_with_feather' in f for f in files)
 metadata=z.read('META-INF/neoforge.mods.toml').decode()
 for required in ['version="2.1.0"','displayName="Canvas Studio+"','made by SuprixZ','[26.3-alpha,26.4-alpha)','[26.3]']:assert required in metadata
 assert 'canvas-studio-icon.png' in files
 for n in files:
  if n.endswith('.class'):assert int.from_bytes(z.read(n)[6:8],'big')==69, 'Java 25 class target'
baseline_hashes={'ImageImport.java': 'dd1ffa949194764c1d7c0611fdf9b654a1796171c21811e67ce1ea8d245b950e', 'ItemMatcher.java': '42295a9988a60738b4a8620d86ce3bfd8aa848ec4020522726cf91ad69ccff1f', 'ItemMatchCatalog.java': '5293134ee9a8967b7b318ec736c76368ff682577fde523b8881f7b9e371e8cae'}
for name,expected in baseline_hashes.items():
 assert hashlib.sha256((root/'src/main/java/studio/canvas/client'/name).read_bytes()).hexdigest()==expected, 'Preserved baseline '+name
print('PASS packaged JAR: all six item assets/names, distinct textures, recipes/results/advancements, shortcut removal, version/credit/range, Java 25 classes and unchanged importer/matcher/catalogue.')
print('JAR SHA-256:',hashlib.sha256(jar.read_bytes()).hexdigest())
