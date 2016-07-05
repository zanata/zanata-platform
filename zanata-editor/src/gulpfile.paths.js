/**
 * Path Definitions
 */

/* eslint-disable semi */

// FIXME use path.join in all cases

var paths = {};

paths.app = './app';
paths.build = './build';
paths.config = paths.app + '/config.json';
paths.icons = {
  app: paths.app + '/components/Icon/images/*.svg'
};
paths.images = {
  app: [
    '!' + paths.icons.app,
    paths.app + '/**/*.svg',
    paths.app + '/**/*.jpg',
    paths.app + '/**/*.png',
    paths.app + '/**/*.gif'
  ]
};

module.exports = paths;
