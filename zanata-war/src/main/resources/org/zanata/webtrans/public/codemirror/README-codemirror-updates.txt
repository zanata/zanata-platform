The codemirror version number is incorporated in the filename to enable
correct caching in browsers.

When updating codemirror, be sure to change the filenames for
codemirror-*.cache.css and codemirror-compressed-*.cache.js and the
references to them in Application.xhtml and Dummy.html.


Recipe for codemirror-compressed.js:

codemirror-compressed.js is generated at http://codemirror.net/doc/compress.html
includes:
- codemirror.js
- css.js
- htmlmixed.js
- javascript.js
- xml.js
- runmode.js
- searchcursor.js (to enable highlight search term within codemirror"
- overlay.js (to support visible space)

added following custom code when generating the codemirror-compressed.js (to support visible space):

CodeMirror.defineMode("visibleSpace", function(config, parserConfig) {
  var visibleSpaceOverlay = {
    token: function(stream, state) {
      var ch = stream.next();
      if (ch == " ") {
        return "space";
      } else {
        return null;
      }
    }
  };
  return CodeMirror.overlayMode(CodeMirror.getMode(config, parserConfig.backdrop || "htmlmixed"), visibleSpaceOverlay);
});
