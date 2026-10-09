"""Validate the shipped JAR's data, item assets, metadata and preserved source."""
from pathlib import Path
import json,zipfile,hashlib
root=Path(__file__).resolve().parents[1]
jar=root/'build/libs/canvas-studio-plus-26.2-2.1.2.jar'
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
  expected={
   'canvas':('minecraft:crafting_shapeless',['minecraft:leather','minecraft:paper','canvasstudio:quill']),
   'quill':('minecraft:crafting_shapeless',['minecraft:feather','minecraft:ink_sac']),
   'molder_canvas':('minecraft:crafting_shapeless',['canvasstudio:canvas','canvasstudio:molder_quill','minecraft:compass']),
   'infinity_canvas':('minecraft:crafting_shapeless',['canvasstudio:molder_canvas','canvasstudio:infinity_quill','minecraft:recovery_compass']),
   'molder_quill':(['IGI','GQG','IGI'],{'I':'minecraft:iron_ingot','G':'minecraft:gold_ingot','Q':'canvasstudio:quill'}),
   'infinity_quill':(['EDE','LQL','EDE'],{'E':'minecraft:echo_shard','D':'minecraft:diamond_block','L':'minecraft:lapis_block','Q':'canvasstudio:molder_quill'})}
  first,second=expected[name]
  if name.endswith('quill') and name!='quill':
   assert r['type']=='minecraft:crafting_shaped' and r['pattern']==first and r['key']==second
  else:assert r['type']==first and sorted(r['ingredients'])==sorted(second)
  if name.startswith('molder'):assert lang['item.canvasstudio.'+name]=='Molten '+('Canvas' if name.endswith('canvas') else 'Quill')
  a=json.loads(z.read(f'data/canvasstudio/advancement/recipes/{name}.json'))
  assert a['rewards']['recipes']==['canvasstudio:'+name]
  assert a['criteria']['has_the_recipe']['conditions']['recipes']==['canvasstudio:'+name]
 assert len(set(textures))==6
 # v2.1.2 replaces upgrade textures only; original painting items remain unchanged.
 texture_baseline={'canvas': '7abf44a79d555ea10ae9eea4a131dcd59a1de1f9a0b9d1fd9bd310f00e18de83', 'quill': '0c0c60c4d4f480057884d2142438309a792e779a494c43ebdda310507d97213e', 'molder_canvas': 'd93cf2fc68b222c47daa98bc19ea87b4dd41afb4e60261e83cb8c1412225c6b1', 'molder_quill': '23abffd9202a11e78a5af1f7effdc85ce9a2c7d0dce0cc8ca9e2375255b0a358', 'infinity_canvas': '13f9b484507ac7346b318be19c5ae9fa7cc889b123d393f829da4a1aea1225d6', 'infinity_quill': 'd77b3702b197fc022159a9ca25fe5539f4b8b7d4a428c6eb2623c07de9ea15dd'}
 for name,expected in texture_baseline.items():
  actual=hashlib.sha256(z.read(f"assets/canvasstudio/textures/item/{name}.png")).hexdigest()
  assert (actual==expected) if name in ('canvas','quill','infinity_canvas') else (actual!=expected)
 assert not any('canvas_with_feather' in f for f in files)
 metadata=z.read('META-INF/neoforge.mods.toml').decode()
 for required in ['version="2.1.2"','displayName="Canvas Studio+"','made by SuprixZ','[26.2.0.88]','[26.2]','[11,12)']:assert required in metadata
 assert 'canvas-studio-icon.png' in files
 for n in files:
  if n.endswith('.class'):assert int.from_bytes(z.read(n)[6:8],'big')==69, 'Java 25 class target'
baseline_hashes={'ImageImport.java': 'dd1ffa949194764c1d7c0611fdf9b654a1796171c21811e67ce1ea8d245b950e', 'ItemMatcher.java': '42295a9988a60738b4a8620d86ce3bfd8aa848ec4020522726cf91ad69ccff1f', 'ItemMatchCatalog.java': '5293134ee9a8967b7b318ec736c76368ff682577fde523b8881f7b9e371e8cae'}
for name,expected in baseline_hashes.items():
 assert hashlib.sha256((root/'src/main/java/studio/canvas/client'/name).read_bytes()).hexdigest()==expected, 'Preserved baseline '+name
print('PASS packaged JAR: all six item assets/names, distinct textures, recipes/results/advancements, shortcut removal, version/credit/range, Java 25 classes and unchanged importer/matcher/catalogue.')
print('JAR SHA-256:',hashlib.sha256(jar.read_bytes()).hexdigest())
