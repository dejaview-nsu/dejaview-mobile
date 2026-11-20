/* ZIP STORE writer, UTF-8 names; no network or third-party runtime. */
function privateZip(entries) {
  const encoder = new TextEncoder();
  const parts = [], directory = [];
  const names = new Set();
  let offset = 0, total = 0;
  const crc32 = bytes => {
    let crc = 0xffffffff;
    for (const byte of bytes) {
      crc ^= byte;
      for (let bit = 0; bit < 8; bit++) crc = (crc >>> 1) ^ ((crc & 1) ? 0xedb88320 : 0);
    }
    return (crc ^ 0xffffffff) >>> 0;
  };
  for (const entry of entries) {
    if (!/^[a-zA-Z0-9_./-]+$/.test(entry.name) || entry.name.startsWith('/') || entry.name.split('/').includes('..') || names.has(entry.name)) throw Error('Invalid archive path');
    names.add(entry.name);
    const name = encoder.encode(entry.name);
    const data = entry.bytes instanceof Uint8Array ? entry.bytes : new Uint8Array(entry.bytes);
    total += data.length;
    if (total > 256 * 1024 * 1024 || entries.length > 65535 || name.length > 65535) throw Error('Archive limit exceeded; export one page at a time');
    const crc = crc32(data);
    const local = new Uint8Array(30 + name.length), lv = new DataView(local.buffer);
    lv.setUint32(0, 0x04034b50, true); lv.setUint16(4, 20, true); lv.setUint16(6, 0x800, true);
    lv.setUint16(12, 33, true); // 1980-01-01; no wall-clock/user metadata.
    lv.setUint32(14, crc, true); lv.setUint32(18, data.length, true); lv.setUint32(22, data.length, true);
    lv.setUint16(26, name.length, true); local.set(name, 30);
    const central = new Uint8Array(46 + name.length), cv = new DataView(central.buffer);
    cv.setUint32(0, 0x02014b50, true); cv.setUint16(4, 20, true); cv.setUint16(6, 20, true); cv.setUint16(8, 0x800, true);
    cv.setUint16(14, 33, true); cv.setUint32(16, crc, true); cv.setUint32(20, data.length, true); cv.setUint32(24, data.length, true);
    cv.setUint16(28, name.length, true); cv.setUint32(42, offset, true); central.set(name, 46);
    parts.push(local, data); directory.push(central); offset += local.length + data.length;
  }
  const size = directory.reduce((sum, p) => sum + p.length, 0);
  const end = new Uint8Array(22), ev = new DataView(end.buffer);
  ev.setUint32(0, 0x06054b50, true); ev.setUint16(8, entries.length, true); ev.setUint16(10, entries.length, true);
  ev.setUint32(12, size, true); ev.setUint32(16, offset, true);
  return new Blob([...parts, ...directory, end], {type:'application/zip'});
}
