/* eslint-env node */
/* eslint-disable semi, one-var, indent, space-before-function-paren,
                  key-spacing, space-before-blocks, padded-blocks,
                  spaced-comment */
'use strict';

var env = process.env.NODE_ENV || 'development',
    gulp = require('gulp'),
    gulpif = require('gulp-if'),
    notify = require('gulp-notify'),
    paths = require('./gulpfile.paths.js'),
    replace = require('gulp-replace-task'),
    webserver = require('gulp-webserver');

function notifyError(err) {

  notify.onError({
    title:    'Gulp',
    subtitle: 'Failure!',
    message:  '<%= error.name %>: [<%= error.plugin %>] <%= error.message %>',
    sound:    'Beep'
  })(err);

  this.emit('end');

}

// Copy index.html into /dist
gulp.task('processhtml', function () {

  return gulp.src(paths.app + '/index.html')
    .pipe(gulp.dest(paths.app + '/dist'));
});

// configure config.json for dev or prod build
gulp.task('config', function() {
  var regex = new RegExp('\"baseUrl\".*,');
  gulp.src(paths.config)
    .pipe(gulpif(env === 'production', replace({
      patterns: [{
        match: regex,
        replacement: ''
      }]
    })))
    .pipe(gulp.dest(paths.build));
});

gulp.task('webserver', ['build'], function() {
  gulp.src('build')
    .pipe(webserver({
      livereload: true,
      host: '0.0.0.0',
      port: 8000
    }));
});

gulp.task('serve', ['webserver']);
