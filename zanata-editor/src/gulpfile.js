/* eslint-env node */
/* eslint-disable semi, one-var, indent, space-before-function-paren,
                  key-spacing, space-before-blocks, padded-blocks,
                  spaced-comment */
'use strict';

var env = process.env.NODE_ENV || 'development',
    gulp = require('gulp'),
    gulpif = require('gulp-if'),
    imagemin = require('gulp-imagemin'),
    inject = require('gulp-inject'),
    notify = require('gulp-notify'),
    paths = require('./gulpfile.paths.js'),
    plumber = require('gulp-plumber'),
    rename = require('gulp-rename'),
    replace = require('gulp-replace-task'),
    svgSprite = require('gulp-svg-sprite'),
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

// Copy index.html into /build
gulp.task('processhtml', function () {

  return gulp.src(paths.app + '/index.html')
    .pipe(gulp.dest(paths.app + '/build'));
});

// similar to 'icons' but makes a static icons file for use in the storybook
// since there is no access to inject them into its index file.
// ( See .storybook/README.md )
gulp.task('storybook-icons', function () {
  return gulp.src(paths.icons.app)
    .pipe(plumber({errorHandler: notifyError}))
    .pipe(svgSprite({
      mode: {
        symbol: {
          inline: false
        }
      }
    }))
    .pipe(rename('icons.svg'))
    .pipe(gulp.dest(paths.app + '/build'));
});

gulp.task('images', function(){
  return gulp.src(paths.images.app)
    .pipe(plumber({errorHandler: notifyError}))
    // TODO Clean build first
    .pipe(imagemin({ optimizationLevel: 5,
      progressive: true, interlaced: true }))
    .pipe(rename(function(path) {
      path.dirname = path.dirname.replace('components/', '');
    }))
    .pipe(gulp.dest(paths.build + '/images'));
});

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

gulp.task('copyIndex', ['icons']);

gulp.task('build',
  [
    'icons',
    'images',
    'config'
  ]);

gulp.task('webserver', ['build'], function() {
  gulp.src('build')
    .pipe(webserver({
      livereload: true,
      host: '0.0.0.0',
      port: 8000
    }));
});

gulp.task('serve', ['webserver']);

gulp.task('watch', ['serve'], function(){
  gulp.watch(paths.images.app, ['images']);
  gulp.watch(paths.app + '/index.html', ['copyIndex']);
});

gulp.task('default', ['build']);
