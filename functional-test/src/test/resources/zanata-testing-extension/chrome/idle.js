var s = document.createElement('script');
s.src = chrome.extension.getURL('finished.js');
s.setAttribute('defer', 'defer');
s.onload = function() {
  this.parentNode.removeChild(this);
};
(document.body || document.documentElement).appendChild(s);
