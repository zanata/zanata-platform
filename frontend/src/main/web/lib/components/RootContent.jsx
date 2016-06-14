import React from 'react';
import Router from 'react-router';

var RootContent = React.createClass({
  render: function() {

    //views are handled in index.js
    var RouteHandler = Router.RouteHandler;
    return ( <RouteHandler/>);
  }
});

export default RootContent;
