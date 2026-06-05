/**
 * 构建后脚本：给 index.html 中 JS/CSS URL 添加版本参数
 * 用于强制浏览器刷新缓存，解决 hash 不变时浏览器使用旧 chunk 的问题
 */
import { readFileSync, writeFileSync } from 'fs'
import { resolve, dirname } from 'path'
import { fileURLToPath } from 'url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const dist = resolve(__dirname, '../dist/index.html')
const ts = Date.now()

let html = readFileSync(dist, 'utf-8')

// 给所有 JS 和 CSS URL 加上 ?v=TIMESTAMP
html = html.replace(/(src=")([^"]+\.js)(")/g, (m, pre, url, post) => {
  return pre + url + '?v=' + ts + post
})
html = html.replace(/(href=")([^"]+\.css)(")/g, (m, pre, url, post) => {
  return pre + url + '?v=' + ts + post
})

writeFileSync(dist, html)
console.log('[deploy] Cache-busting version: ' + ts + ' injected into index.html')
