'use strict';
const http = require('http');
const fs = require('fs');

const filename = '../app/src/main/res/raw/templates.json';
const fileContent = fs.readFileSync(filename);
let templates = JSON.parse(fileContent);
console.log('Templates loaded: ' + templates.length);

process.on('SIGINT', () => {
  const jsonTemplates = JSON.stringify(templates);
  console.log('Process is ending: saving data ...');
  fs.writeFileSync(filename, jsonTemplates);
  process.exit();
});

const server = http.createServer((req, res) => {
  console.log('I have got a req!')
  if (req.method === 'GET') {
    console.log('GET');
    res.writeHead(200, {'Content-Type': 'text/plain'});
    res.end(JSON.stringify(templates));
  } else if (req.method === 'POST') {
    let data = '';
    req.setEncoding('utf8');
    req.on('data', chunk => data += chunk.toString());
    req.on('end', () => {
      const parsedData = JSON.parse(data);
      if (Array.isArray(parsedData)) templates.concat(parsedData);
      else templates.push(parsedData);
      console.log('Templates came: ' + templates.length);
      res.end();
    });
  }
});

server.listen(8080);

