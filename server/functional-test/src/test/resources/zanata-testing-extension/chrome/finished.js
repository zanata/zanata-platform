// If this is the last defer script on the page, this should only be
// set after all other synchronous/defer scripts have executed.
// (NB: Async scripts, timers and AJAX requests may still be ongoing.)
window.deferScriptsFinished = true;
console.debug('deferScriptsFinished = true');
