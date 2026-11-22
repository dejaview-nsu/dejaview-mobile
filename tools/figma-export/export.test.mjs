import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import vm from 'node:vm';
import {spawnSync} from 'node:child_process';
const source=name=>fs.readFileSync(new URL(name,import.meta.url),'utf8');

test('the UI displays progress from the Figma sandbox bridge during export', () => {
  const elements = new Map(['start', 'save', 'status', 'page'].map(id => [id, {}]));
  const context = vm.createContext({
    document: { getElementById: id => elements.get(id) },
    parent: { postMessage() {} },
    onmessage: null,
  });
  const script = source('ui.html').split('<script>')[1].split('</script>')[0];
  vm.runInContext(script, context);

  const data = { pluginMessage: { type: 'progress', done: 1, total: 4 } };
  const bridge = {};
  context.onmessage({ source: bridge, data });
  assert.equal(elements.get('status').textContent, undefined);
  elements.get('start').onclick();
  context.onmessage({ source: bridge, data: null });
  assert.equal(elements.get('status').textContent, 'Читаются страницы и слои…');
  context.onmessage({ source: bridge, data });
  assert.equal(elements.get('status').textContent, 'Фреймы: 1/4');
});

test('ZIP is readable by an independent implementation; rejects traversal and duplicates',async()=>{
  const context=vm.createContext({Blob,TextEncoder,Uint8Array,DataView,Set,Error});
  vm.runInContext(source('zip.js'),context);
  const entries=[{name:'screens/1-2.json',bytes:new TextEncoder().encode('{"text":"Кино"}')}, {name:'screens/1-2.png',bytes:new Uint8Array([0,1,2,255])}];
  const blob=context.privateZip(entries);
  const data=Buffer.from(await blob.arrayBuffer());
  const script='import sys,io,zipfile,json; z=zipfile.ZipFile(io.BytesIO(sys.stdin.buffer.read())); assert z.testzip() is None; assert len(z.namelist())==2; assert json.loads(z.read("screens/1-2.json"))["text"]=="Кино"; assert z.read("screens/1-2.png")==bytes([0,1,2,255])';
  const result=spawnSync('python',['-c',script],{input:data});
  assert.equal(result.status,0,result.stderr.toString());
  assert.throws(()=>context.privateZip([{name:'../escape',bytes:new Uint8Array()}]));
  assert.throws(()=>context.privateZip([entries[0],entries[0]]));
});

test('exports only outer frames; preserves text, partial failures and variable data',async()=>{
  const messages=[];
  const text=Object.freeze({id:'2:2',type:'TEXT',name:'label',characters:'Synthetic text',getStyledTextSegments:()=>[{characters:'Synthetic text',fontSize:14}]});
  const inner=Object.freeze({id:'2:3',type:'FRAME',name:'nested',children:[]});
  const frame=Object.freeze({id:'2:1',type:'FRAME',name:'screen',width:412,height:917,children:[text,inner],exportAsync:async options=>{if(options.format==='SVG')throw Error('fixture');return new Uint8Array([1,2]);}});
  const section=Object.freeze({id:'3:1',type:'SECTION',children:[frame]});
  const page=Object.freeze({id:'1:1',name:'Page',children:[section]});
  let loaded=0;
  const figma={showUI(){},closePlugin(){},ui:{postMessage:m=>messages.push(m)},loadAllPagesAsync:async()=>loaded++,root:{children:[page]},currentPage:page,
    variables:{getLocalVariablesAsync:async()=>[{id:'v1',name:'space',resolvedType:'FLOAT',valuesByMode:{m:8}}],getLocalVariableCollectionsAsync:async()=>[]},
    getLocalTextStylesAsync:async()=>[],getLocalPaintStylesAsync:async()=>[],getLocalEffectStylesAsync:async()=>[],getLocalGridStylesAsync:async()=>[]};
  vm.runInNewContext(source('code.js'),{figma,__html__:'',Uint8Array});
  await figma.ui.onmessage({type:'export'});
  assert.equal(loaded,1);
  const json=name=>JSON.parse(messages.find(m=>m.name===name).text);
  const manifest=json('manifest.json');
  assert.equal(manifest.frames.length,1);
  assert.equal(manifest.frames[0].svg,null);
  assert.equal(manifest.errors.length,1);
  assert.equal(json('screens/2-1.json').children[0].characters,'Synthetic text');
  assert.equal(json('variables.json')[0].valuesByMode.m,8);
  assert.deepEqual(messages.filter(m=>m.type==='progress').map(m=>m.done),[0,1]);
  assert.equal(messages.at(-1).type,'done');
});
