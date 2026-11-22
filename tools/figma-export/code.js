/* Read-only exporter. No node setters, account APIs, storage or network calls. */
figma.showUI(__html__, {width:440, height:340});
let running = false;
const fields = ['id','type','name','visible','locked','x','y','width','height','rotation','opacity',
  'relativeTransform','absoluteTransform','constraints','layoutMode','layoutWrap','layoutAlign',
  'layoutGrow','layoutPositioning','layoutSizingHorizontal','layoutSizingVertical','itemSpacing',
  'primaryAxisAlignItems','counterAxisAlignItems','primaryAxisSizingMode','counterAxisSizingMode',
  'paddingTop','paddingRight','paddingBottom','paddingLeft','minWidth','maxWidth','minHeight','maxHeight',
  'fills','strokes','strokeWeight','strokeAlign','dashPattern','effects','blendMode','cornerRadius',
  'topLeftRadius','topRightRadius','bottomLeftRadius','bottomRightRadius','clipsContent',
  'characters','fontName','fontSize','fontWeight','lineHeight','letterSpacing','textAlignHorizontal',
  'textAlignVertical','textAutoResize','textCase','textDecoration','paragraphSpacing','paragraphIndent',
  'textStyleId','fillStyleId','strokeStyleId','effectStyleId','reactions','overflowDirection',
  'boundVariables','explicitVariableModes','resolvedVariableModes','componentProperties','componentPropertyDefinitions',
  'variantProperties','vectorPaths','exportSettings'];
const plain = value => JSON.parse(JSON.stringify(value, (_, v) => typeof v === 'symbol' ? {mixed:true} : v));
const key = id => String(id).replace(/[^a-zA-Z0-9_-]/g, '-');
function framesFrom(parent) {
  const frames = [];
  for (const child of parent.children || []) {
    if (child.type === 'FRAME') frames.push(child);
    else if (child.type === 'SECTION') frames.push(...framesFrom(child));
  }
  return frames;
}
async function snapshot(node, errors, imageHashes) {
  const result = {};
  for (const field of fields) {
    try {
      const value = node[field];
      if (value !== undefined) result[field] = plain(value);
    } catch (_) { errors.push({nodeId:node.id, field, reason:'unavailable-property'}); }
  }
  for (const paint of [...(Array.isArray(result.fills) ? result.fills : []), ...(Array.isArray(result.strokes) ? result.strokes : [])]) {
    if (paint.type === 'IMAGE' && paint.imageHash) imageHashes.add(paint.imageHash);
  }
  if (node.type === 'TEXT') {
    try { result.textSegments = plain(node.getStyledTextSegments(['fontName','fontSize','fills','lineHeight','letterSpacing','textCase','textDecoration','textStyleId','fillStyleId'])); }
    catch (_) { errors.push({nodeId:node.id, field:'textSegments', reason:'unavailable-property'}); }
  }
  if (node.type === 'INSTANCE') {
    try { const component = await node.getMainComponentAsync(); result.mainComponentId = component ? component.id : null; }
    catch (_) { errors.push({nodeId:node.id, field:'mainComponent', reason:'unavailable-property'}); }
  }
  if ('children' in node) {
    result.children = [];
    for (const child of node.children) result.children.push(await snapshot(child, errors, imageHashes));
  }
  return result;
}
figma.ui.onmessage = async message => {
  if (message.type === 'close') { figma.closePlugin(); return; }
  if (message.type !== 'export' || running) return;
  running = true;
  const errors = [], imageHashes = new Set();
  const send = (name, value) => figma.ui.postMessage({type:'json', name, text:JSON.stringify(value, null, 2)});
  const bytes = (name, data) => figma.ui.postMessage({type:'bytes', name, bytes:data});
  try {
    figma.ui.postMessage({type:'loading', currentPage:message.currentPage});
    if (!message.currentPage) await figma.loadAllPagesAsync();
    const pages = message.currentPage ? [figma.currentPage] : figma.root.children;
    const frames = pages.flatMap(page => framesFrom(page).map(node => ({node, page})));
    figma.ui.postMessage({type:'progress', done:0, total:frames.length});
    const manifest = {format:'private-figma-export-v1', scope:message.currentPage ? 'current-page' : 'all-pages',
      frames:[], errors, omissions:['comments','edit-history','account-identities','remote-library-definitions','font-binaries']};
    let index = 0;
    for (const {node, page} of frames) {
      const base = 'screens/' + key(node.id);
      const item = {nodeId:node.id, name:node.name, pageId:page.id, page:page.name, width:node.width, height:node.height,
        json:base+'.json', png:null, svg:null};
      send(item.json, await snapshot(node, errors, imageHashes));
      for (const format of ['PNG','SVG']) {
        try {
          const options = format === 'PNG' ? {format, constraint:{type:'SCALE',value:1}} : {format, svgOutlineText:true};
          const data = await node.exportAsync(options);
          const filename = base+'.'+format.toLowerCase();
          bytes(filename, data); item[format.toLowerCase()] = filename;
        } catch (_) { errors.push({nodeId:node.id, format, reason:'render-failed'}); }
      }
      manifest.frames.push(item);
      figma.ui.postMessage({type:'progress', done:++index, total:frames.length});
    }
    for (const hash of imageHashes) {
      try {
        if (!/^[a-f0-9]+$/i.test(hash)) throw Error('invalid-key');
        const image = figma.getImageByHash(hash);
        if (!image) throw Error('missing');
        bytes('images/'+hash+'.bin', await image.getBytesAsync());
      } catch (_) { errors.push({field:'image', reason:'image-unavailable'}); }
    }
    const collect = async (name, action, props) => {
      try {
        const items = await action();
        const values = items.map(item => {
          const value = {};
          for (const prop of props) if (item[prop] !== undefined) value[prop] = plain(item[prop]);
          return value;
        });
        send(name+'.json', values);
      } catch (_) { errors.push({field:name, reason:'collection-unavailable'}); }
    };
    await collect('variables', () => figma.variables.getLocalVariablesAsync(), ['id','name','resolvedType','variableCollectionId','valuesByMode','scopes']);
    await collect('variable-collections', () => figma.variables.getLocalVariableCollectionsAsync(), ['id','name','modes','defaultModeId','variableIds']);
    await collect('text-styles', () => figma.getLocalTextStylesAsync(), ['id','name','fontName','fontSize','lineHeight','letterSpacing','textCase','textDecoration','paragraphSpacing']);
    await collect('paint-styles', () => figma.getLocalPaintStylesAsync(), ['id','name','paints']);
    await collect('effect-styles', () => figma.getLocalEffectStylesAsync(), ['id','name','effects']);
    await collect('grid-styles', () => figma.getLocalGridStylesAsync(), ['id','name','layoutGrids']);
    send('manifest.json', manifest);
    figma.ui.postMessage({type:'done', frames:frames.length, errors:errors.length});
  } catch (_) { figma.ui.postMessage({type:'failed', reason:'Export interrupted; no complete archive was produced'}); }
  finally { running = false; }
};
