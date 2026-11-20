import fs from 'node:fs';
import path from 'node:path';
import {fileURLToPath} from 'node:url';
import {parseFig, nodeId} from 'openfig-core';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../..');
export function selectFrames(nodes) {
  const map = new Map(nodes.map(n => [nodeId(n), n]));
  const selected = [];
  for (const n of nodes) {
    if (n.type !== 'FRAME' || n.phase === 'REMOVED') continue;
    let parent = map.get(nodeId({guid:n.parentIndex?.guid}));
    let section = null;
    const seen = new Set([nodeId(n)]);
    while (parent && parent.type !== 'CANVAS') {
      if (parent.type !== 'SECTION' || seen.has(nodeId(parent))) break;
      seen.add(nodeId(parent));
      section = parent;
      parent = map.get(nodeId({guid:parent.parentIndex?.guid}));
    }
    if (parent?.type === 'CANVAS') selected.push({node:n, page:parent, section});
  }
  return selected;
}

export function assertPrivateOutput(out) {
  const base = path.join(root, '.local');
  const resolved = path.resolve(out);
  const rel = path.relative(base, resolved);
  if (!rel || rel.startsWith('..') || path.isAbsolute(rel)) throw new Error('Output must be inside the ignored .local directory');
  // Reject symlink/junction parents that could escape the private directory.
  let cursor = resolved;
  while (cursor !== root) {
    if (fs.existsSync(cursor) && fs.lstatSync(cursor).isSymbolicLink()) throw new Error('Output cannot traverse a symlink');
    const next = path.dirname(cursor);
    if (next === cursor) throw new Error('Output root is invalid');
    cursor = next;
  }
  return resolved;
}

export function extract(input, output) {
  const out = assertPrivateOutput(output);
  if (fs.existsSync(out) && fs.readdirSync(out).length) throw new Error('Use a new empty output directory; existing exports are not overwritten');
  const doc = parseFig(new Uint8Array(fs.readFileSync(input)));
  fs.mkdirSync(path.join(out, 'screens'), {recursive:true});
  const json = (name, value) => fs.writeFileSync(path.join(out, name), JSON.stringify(value, null, 2) + '\n');
  const children = new Map();
  for (const n of doc.nodes) {
    const id = nodeId({guid:n.parentIndex?.guid});
    if (!children.has(id)) children.set(id, []);
    children.get(id).push(n);
  }
  const descend = (n, seen = new Set()) => {
    const id = nodeId(n);
    if (seen.has(id)) throw new Error('Cycle in source node tree');
    seen.add(id);
    return [n, ...(children.get(id) || []).flatMap(c => descend(c, seen))];
  };
  const manifest = selectFrames(doc.nodes).map(({node, page, section}) => {
    const id = nodeId(node);
    const key = id.replace(':', '-');
    const subtree = descend(node);
    json(`screens/${key}.json`, {nodeId:id, nodes:subtree});
    return {nodeId:id, key, name:node.name, pageId:nodeId(page), page:page.name,
      section:section?.name || null, screenId:node.name.match(/^\d+\.\d+[а-яa-z]?/iu)?.[0] || null,
      width:node.size?.x, height:node.size?.y, nodeCount:subtree.length,
      outsideMvp: /вне MVP/iu.test(`${section?.name || ''} ${node.name}`)};
  });
  json('manifest.json', {format:'private-fig-extract-v1', parser:'openfig-core@0.4.1', header:doc.header,
    nodeCount:doc.nodes.length, imageCount:doc.images.size, frames:manifest});
  json('document.json', doc.message);
  json('schema.json', doc.schema);
  json('variables.json', doc.nodes.filter(n => ['VARIABLE','VARIABLE_SET'].includes(n.type)));
  json('text-styles.json', doc.nodes.filter(n => n.styleType));
  if (doc.images.size) {
    fs.mkdirSync(path.join(out, 'images'));
    for (const [name, data] of doc.images) {
      if (!/^[a-f0-9]+$/i.test(name)) throw new Error('Unexpected image key');
      fs.writeFileSync(path.join(out, 'images', name), data);
    }
  }
  console.log(JSON.stringify({frames:manifest.length, numberedScreens:manifest.filter(f=>f.screenId).length,
    nodes:doc.nodes.length, variables:doc.nodes.filter(n=>n.type==='VARIABLE').length,
    styles:doc.nodes.filter(n=>n.styleType).length, embeddedImages:doc.images.size}));
  return manifest;
}

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  try {
    if (process.argv.length !== 4) throw new Error('Usage: node tools/design/extract-fig.mjs INPUT.fig .local/design/new-export');
    extract(process.argv[2], process.argv[3]);
  } catch (error) {
    // Avoid dumping absolute input paths or binary decoder contents.
    console.error(error.message.startsWith('Usage:') ? error.message : 'Extraction failed; check input format and empty private output directory.');
    process.exitCode = 1;
  }
}
