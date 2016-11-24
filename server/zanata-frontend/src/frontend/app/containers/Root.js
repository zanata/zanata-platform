import React, { Component, PropTypes } from 'react'
import { Provider } from 'react-redux'
import { Router, Route, Redirect } from 'react-router'
import App from '../containers/App'
import Glossary from '../containers/Glossary'
import Languages from '../containers/Languages'
import Explore from '../containers/Explore'
import UserProfile from '../containers/UserProfile'
import { View } from 'zanata-ui'
import StyleGuide from '../containers/StyleGuide'

export default class Root extends Component {
  render () {
    const username = window.config.user.username
    const {
      store,
      history
    } = this.props
    return (
      <Provider store={store}>
        <View>
          <Router history={history}>
            <Route component={App} >
              <Route path='explore' component={Explore} />
              <Route path='glossary/project/:projectSlug'
                component={Glossary} />
              <Route path='glossary' component={Glossary} />
              <Route path='languages' component={Languages} />
              <Route path='profile/view/:username' component={UserProfile} />
              <Redirect from='profile' to={`profile/view/${username}`} />
              <Route path='styleguide' component={StyleGuide} />
            </Route>
          </Router>
        </View>
      </Provider>
    )
  }
}

Root.propTypes = {
  store: PropTypes.object.isRequired,
  history: PropTypes.object.isRequired
}
