
(function(xhr) {
  // NB this doesn't handle setInterval or setTimeout
  // TODO handle XHR.abort (NB: readystatechange listener triggers on Chrome 45, despite MDN docs)
  // TODO handle send() being called again while request is open
  // http://stackoverflow.com/questions/4410218/trying-to-keep-track-of-number-of-outstanding-ajax-requests-in-firefox
  if (xhr.active === undefined) {
    console.debug('injecting ajax counter');
    xhr.active = 0;
    var pt = xhr.prototype;
    var _send = pt.send;
    pt.send = function() {
      if (this.hasActiveEventListener === undefined) {
        this.addEventListener('readystatechange', function() {
          if ( this.readyState == XMLHttpRequest.DONE ) {
            setTimeout(function() {
              xhr.active--;
            }, 1);
          }
        });
        this.hasActiveEventListener = true;
      }
      xhr.active++;
      _send.apply(this, arguments);
    }
  }
})(XMLHttpRequest);
/*
// This enables ChromeDriver to pick up stack traces in the browser logs
console.debug('injecting error stack logger');
window.addEventListener("error", function (e) {
  // For some reason, this form (with a comma) won't let WebDriver see the stack trace:
  // console.error('error stack:', e.error.stack.toString());
  console.error('error stack: ' + e.error.stack.toString());
});
*/
console.debug('finished injecting');
