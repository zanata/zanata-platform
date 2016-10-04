import React, { Component, PropTypes } from 'react'
import { Provider } from 'react-redux'
import { Router, Route, Redirect } from 'react-router'
import App from '../containers/App'
import Glossary from '../containers/Glossary'
import Languages from '../containers/Languages'
import Explore from '../containers/Explore'
import UserProfile from '../containers/UserProfile'
import { View } from 'zanata-ui'

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
              <Route path='glossary' component={Glossary} />
              <Route path='languages' component={Languages} />
              <Route path='profile/view/:username' component={UserProfile} />
              <Route path='project/:projectSlug/glossary'
                component={Glossary} />
              <Redirect from='profile' to={`profile/view/${username}`} />
              <Redirect from='/' to={`profile/view/${username}`} />
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
