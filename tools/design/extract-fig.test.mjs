import test from 'node:test';
import assert from 'node:assert/strict';
import {fileURLToPath} from 'node:url';
import {selectFrames, assertPrivateOutput} from './extract-fig.mjs';
const n = (id, type, parent, name='') => ({guid:{sessionID:1,localID:id},type,name,
  parentIndex:{guid:{sessionID:1,localID:parent}}});
test('selects page and section frames without counting nested controls as screens', () => {
  const nodes=[n(1,'CANVAS',0),n(2,'FRAME',1,'1.3'),n(3,'FRAME',2,'Input'),n(4,'SECTION',1),n(5,'FRAME',4,'1.3')];
  assert.deepEqual(selectFrames(nodes).map(x=>x.node.guid.localID),[2,5]);
});
test('keeps duplicate numbered screen variants distinguished by node id', () => {
  assert.equal(selectFrames([n(1,'CANVAS',0),n(2,'FRAME',1,'2.1'),n(3,'FRAME',1,'2.1')]).length,2);
});
test('rejects public output and directory escape', () => {
  assert.throws(()=>assertPrivateOutput('docs/design/raw'));
  assert.throws(()=>assertPrivateOutput('.local/../docs'));
  assert.doesNotThrow(()=>assertPrivateOutput(fileURLToPath(new URL('../../.local/design/private-export', import.meta.url))));
});
