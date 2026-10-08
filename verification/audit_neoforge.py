"""Audit compiled mod references against every published NeoForge 26.3 artifact.
Checks binary method/field signatures and class availability; not a gameplay test.
"""
from pathlib import Path
import sys,concurrent.futures,urllib.request,xml.etree.ElementTree as ET,json,zipfile,struct,hashlib
ROOT=Path(__file__).resolve().parents[1]
CACHE=Path(sys.argv[2]) if len(sys.argv)>2 else ROOT/'.compat-cache';CACHE.mkdir(exist_ok=True)
BASE='https://maven.neoforged.net/releases/'

def fetch(url,path):
 if path.exists():return path.read_bytes()
 with urllib.request.urlopen(url,timeout=90) as r:b=r.read()
 path.write_bytes(b);return b

class Class:
 def __init__(self,b):
  self.b=b;self.p=8;count=self.u2();self.cp=[None]*count;i=1
  while i<count:
   t=self.u1()
   if t==1:self.cp[i]=(t,self.take(self.u2()).decode('utf-8',errors='replace'))
   elif t in (7,8,16,19,20):self.cp[i]=(t,self.u2())
   elif t in (9,10,11,12,17,18):self.cp[i]=(t,self.u2(),self.u2())
   elif t==15:self.cp[i]=(t,self.u1(),self.u2())
   else:self.cp[i]=(t,self.take({3:4,4:4,5:8,6:8}[t]));i+=int(t in (5,6))
   i+=1
  self.flags=self.u2();self.name=self.cls(self.u2());self.parent=self.cls(self.u2());self.interfaces=[self.cls(self.u2()) for _ in range(self.u2())]
  self.fields=self.members();self.methods=self.members()
 def take(self,n):x=self.b[self.p:self.p+n];self.p+=n;return x
 def u1(self):return self.take(1)[0]
 def u2(self):return struct.unpack('>H',self.take(2))[0]
 def u4(self):return struct.unpack('>I',self.take(4))[0]
 def utf(self,i):return self.cp[i][1]
 def cls(self,i):return self.utf(self.cp[i][1]) if i else None
 def members(self):
  out={}
  for _ in range(self.u2()):
   flags,n,d=self.u2(),self.utf(self.u2()),self.utf(self.u2());out[(n,d)]=flags
   for _ in range(self.u2()):self.u2();self.take(self.u4())
  return out
 def refs(self):
  for c in self.cp:
   if c and c[0] in (9,10,11):
    nt=self.cp[c[2]];yield(c[0],self.cls(c[1]),self.utf(nt[1]),self.utf(nt[2]))

metadata=fetch(BASE+'net/neoforged/neoforge/maven-metadata.xml',CACHE/'maven-metadata.xml')
versions=[e.text for e in ET.fromstring(metadata).findall('./versioning/versions/version') if e.text.startswith('26.3.')]
NS={'m':'http://maven.apache.org/POM/4.0.0'}

def download(v):
 prefix=f'{BASE}net/neoforged/neoforge/{v}/neoforge-{v}'
 jar=fetch(prefix+'-universal.jar',CACHE/(v+'.jar'));pom=fetch(prefix+'.pom',CACHE/(v+'.pom'))
 root=ET.fromstring(pom);deps={}
 for d in root.findall('./m:dependencies/m:dependency',NS):
  group=d.findtext('m:groupId',namespaces=NS);name=d.findtext('m:artifactId',namespaces=NS);dv=d.findtext('m:version',namespaces=NS)
  if (group,name) in [('net.neoforged.fancymodloader','loader'),('net.neoforged','bus'),('net.neoforged','neoform'),('org.apache.maven','maven-artifact')]:deps[name]=(group,name,dv)
 return v,hashlib.sha256(jar).hexdigest(),deps
with concurrent.futures.ThreadPoolExecutor(max_workers=8) as pool:downloads=list(pool.map(download,versions))
print(f'Downloaded {len(versions)} NeoForge builds',flush=True)
unique={coord for _,_,deps in downloads for name,coord in deps.items() if name in ('loader','bus')}
with concurrent.futures.ThreadPoolExecutor(max_workers=6) as pool:
 list(pool.map(lambda c:fetch(BASE+f'{c[0].replace(".","/")}/{c[1]}/{c[2]}/{c[1]}-{c[2]}.jar',CACHE/f'{c[1]}-{c[2]}.jar'),unique))

mods={'plus':Path(sys.argv[1]) if len(sys.argv)>1 else ROOT/'build/libs/canvas-studio-plus-26.3-2.1.0.jar'}
refs={};used_classes={}
for name,path in mods.items():
 refs[name]=set();used_classes[name]=set()
 with zipfile.ZipFile(path) as z:
  for n in z.namelist():
   if n.endswith('.class'):
    c=Class(z.read(n));refs[name].update(r for r in c.refs() if r[1].startswith('net/neoforged/'))
    used_classes[name].update(c.cls(i) for i,e in enumerate(c.cp) if e and e[0]==7 and c.cls(i).startswith('net/neoforged/'))
report={'checked_at':'2026-10-08','scope':'Binary class and method/field reference audit; not Minecraft launch/gameplay validation','versions':[]}
minecraft_classes={}
with zipfile.ZipFile(ROOT/'build/vanilla-validation/minecraft-26.3.jar') as z:
 for n in z.namelist():
   if n.startswith('net/minecraft/') and n.endswith('.class'):
    c=Class(z.read(n));minecraft_classes[c.name]=c
for v,digest,deps in downloads:
 classes=dict(minecraft_classes)
 paths=[CACHE/(v+'.jar')]+[CACHE/f'{n}-{coord[2]}.jar' for n,coord in deps.items() if n in ('loader','bus')]
 for path in paths:
  with zipfile.ZipFile(path) as z:
   for n in z.namelist():
    if n.startswith('net/neoforged/') and n.endswith('.class'):
     c=Class(z.read(n));classes[c.name]=c
 def has(owner,name,desc,field,visited=None):
  if visited is None:visited=set()
  if not owner or owner in visited or owner not in classes:return False
  visited.add(owner);c=classes[owner]
  if (name,desc) in (c.fields if field else c.methods):return True
  return any(has(parent,name,desc,field,visited) for parent in [c.parent]+c.interfaces)
 issues={}
 for edition in mods:
  missing=[f'class {n}' for n in sorted(used_classes[edition]) if n not in classes]
  missing += [f'{owner}.{name}{desc}' for t,owner,name,desc in sorted(refs[edition]) if not has(owner,name,desc,t==9)]
  issues[edition]=missing
 row={'neoforge':v,'neoform':deps['neoform'][2],'loader':deps['loader'][2],'bus':deps['bus'][2],'sha256':digest,'missing_references':issues}
 report['versions'].append(row)
 if any(issues.values()):print(json.dumps(row),flush=True)
report['reference_counts']={e:{'members':len(refs[e]),'classes':len(used_classes[e])} for e in mods}
report['mod_sha256']={name:hashlib.sha256(path.read_bytes()).hexdigest() for name,path in mods.items()}
report['passed']=all(not any(row['missing_references'].values()) for row in report['versions'])
(ROOT/'verification/neoforge-2.1-audit.json').write_text(json.dumps(report,indent=2)+'\n')
print(json.dumps({'passed':report['passed'],'versions':len(versions),'reference_counts':report['reference_counts'],'neoform_versions':sorted({x['neoform'] for x in report['versions']})}),flush=True)
if not report['passed']:raise SystemExit(1)
