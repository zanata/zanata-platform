import React from 'react';
import Router from 'react-router';
import StringUtils from '../utils/StringUtils';
import RootContent from '../components/RootContent';
import UserProfile from '../components/UserProfile';
import SystemGlossary from '../components/SystemGlossary';

var views = {
  USER_PROFILE: 'USER_PROFILE',
  GLOSSARY: "GLOSSARY",

  getView: function(value) {
    switch (value) {
      case 'profile':
        return this.USER_PROFILE; break;
      case 'glossary':
        return this.GLOSSARY; break;
      default:
        console.debug('Invalid view (might be dev environment view)', value);
        return; break;
    }
  },

  getRoutes: function(view, isDev) {
    var Route = Router.Route,
      DefaultRoute = Router.DefaultRoute,
      NotFoundRoute = Router.NotFoundRoute;

    /**
     * if is development environment, return options to select view,
     * view ignored.
     */
    if(isDev) {
      var routes = [];
      routes.push(<Route path="glossary" key="glossaryView" handler={SystemGlossary}/>);
      routes.push(<Route path="profile" key="profileView" handler={UserProfile}/>);
      return this.routeWith(<DefaultRoute handler={UserProfile}/>, routes);
    }
    switch (view) {
      case views.USER_PROFILE:
        return this.routeWith(<DefaultRoute handler={UserProfile}/>);
        break;
      case views.GLOSSARY:
        return this.routeWith(<DefaultRoute handler={SystemGlossary}/>);
        break;
      default :
        return this.routeWith(<DefaultRoute handler={RootContent}/>);
        break;
    }
  },

  routeWith: function (defaultRoute, routes) {
    var Route = Router.Route,
      DefaultRoute = Router.DefaultRoute,
      NotFoundRoute = Router.NotFoundRoute;
    return (
      <Route handler={RootContent}>
        {routes}
        {defaultRoute}
        <NotFoundRoute handler={RootContent} />
      </Route>
    )
  }
};
export default views;
