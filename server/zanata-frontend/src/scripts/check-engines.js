const checkEngines = require('check-engines');

checkEngines(err => {
  if (err) {
    console.error(err)
    process.exitCode = 1
  }
});
