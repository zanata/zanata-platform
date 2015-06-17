$(function() {

  var reader = new commonmark.Parser();
  var writer = new commonmark.HtmlRenderer();

  function mdRender(src) {
    var unsafeHtml = writer.render(reader.parse(src));
    // Run the HTML through Google Caja JsHtmlSanitizer
    var safeHtml = html_sanitize(unsafeHtml);
    return safeHtml;
  }

  var $allEditors = $('.js-commonmark__editor');

  function preview($editors) {
    $editors.each(function(index, editor) {
      var $editor = $(editor);
      var rendered = mdRender($editor.val());
      $editor.parents('.js-commonmark').find('.js-commonmark__preview').html(rendered);
    });
  }

  preview($allEditors);

  var typewatch = (function(){
    var timer = 0;
    return function(callback, ms){
      clearTimeout (timer);
      timer = setTimeout(callback, ms);
    };
  })();

  $allEditors.on('input', function() {
    typewatch(preview($(this)), 200);
  });

});
