'use strict';

// create the root namespace and making sure we're not overwriting it
var zanata = zanata || {};

zanata.createNS = function (namespace) {
  var nsparts = namespace.split('.');
  var parent = zanata;

  // we want to be able to include or exclude the root namespace so we strip
  // it if it's in the namespace
  if (nsparts[0] === 'zanata') {
    nsparts = nsparts.slice(1);
  }

  // loop through the parts and create a nested namespace if necessary
  for (var i = 0; i < nsparts.length; i++) {
    var partname = nsparts[i];
    // check if the current parent already has the namespace declared
    // if it isn't, then create it
    if (typeof parent[partname] === 'undefined') {
        parent[partname] = {};
    }
    // get a reference to the deepest element in the hierarchy so far
    parent = parent[partname];
  }
  // the parent is now constructed with empty namespaces and can be used.
  // we return the outermost namespace
  return parent;
};

jQuery(function () {
  zanata.tooltip.init('[title]');

  // Make touches as fast as desktop clicks
  FastClick.attach(document.body);
});
