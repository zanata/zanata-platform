module.exports = (port) => ({
  contentBase: 'dist/',
  port,
  historyApiFallback: {
    // serve the index file instead of 404, needed to load the app using
    // paths other than /
    index: 'index.html',
    // Anything other than bundle.js and bundle.css should get the app.
    // This is needed in addition to specifying the index file - I would not
    // expect it to be, but paths ending with a document name still get 404
    // when this is not included.
    //   regex notes:
    //     - negative lookahead (?!(frontend|editor)(\.min)?\.(js|css))
    //       checks that the current character is not the start of something
    //       like "frontend.min.js" or "editor.css"
    //     - the "." after the lookahead will match any single character
    //       (when the negative lookahead did not match)
    //     - the outer non-capturing group repeats the above any number of times
    //     - wrapped in ^ and $ so it must match the whole string
    rewrites: [
      {
        from: /^(?:(?!(frontend|editor)(\.min)?\.(js|css)).)*$/,
        to: '/index.html'
      }
    ]
  },
  stats: {
    colors: true,
    chunks: false,
    modules: false
  }
})
