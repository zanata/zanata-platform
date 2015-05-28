/*
This node script will loop through all sibling directories and invoke npm command on each one of them.
To use this script, run: node index.js install or node index.js run build
 */
var fs = require('fs');
var path = require('path');
var spawn = require('child_process').spawn;
var currentDir = __dirname;

var scriptName = path.basename(process.argv[1]);
var restArgs = process.argv.slice(2);
if (!restArgs.length) {
  console.warn('> You did not give a npm command to run!!!');
  console.info('> Usage: %s %s <some command you want to run in each sub directory>', process.argv[0],
    scriptName);
  console.info('e.g. %s %s install', process.argv[0], scriptName);
  process.exit(1);
}
var npmCommand = restArgs.join(' ');
console.info(">> about to invoke [npm %s] on each sub directory/module", npmCommand);

var subModules = [];

fs.readdirSync(currentDir).filter(function(file) {
  var dir = path.join(currentDir, file);
  if (fs.statSync(dir).isDirectory()) {
    if (fs.readdirSync(dir).indexOf('package.json') >= 0) {
      console.log('>>> found module: ' + file);
      subModules.push(file);
    }
  }
});


subModules.forEach(function(subModule) {
  // TODO this will invoke global npm not the local one
  var child = spawn('npm', restArgs, {
    cwd: path.join(currentDir, subModule),
    stdio: [0, 1, 2]
  });

  child.on('close', function (code) {
    console.log('child process exited with code ' + code);
    if (code !== 0) {
      process.exit(code);
    }
  });
});

