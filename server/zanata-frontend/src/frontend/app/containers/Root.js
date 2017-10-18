import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Provider } from 'react-redux'
import { Router, Route, Redirect } from 'react-router'
import App from '../containers/App'
import Glossary from '../containers/Glossary'
import Languages from '../containers/Languages'
import ProjectVersion from '../containers/ProjectVersion'
import Explore from '../containers/Explore'
import UserProfile from '../containers/UserProfile'
import { user } from '../config'
import AdminReview from '../containers/Admin/Review'

export default class Root extends Component {
  static propTypes = {
    store: PropTypes.object.isRequired,
    history: PropTypes.object.isRequired
  }

  render () {
    const username = user.username
    const {
      store,
      history
    } = this.props
    return (
      <Provider store={store}>
        <Router history={history}>
          <Route component={App} >
            <Route path='explore' component={Explore} />
            <Route path='glossary/project/:projectSlug'
              component={Glossary} />
            <Route path='glossary' component={Glossary} />
            <Route path='languages' component={Languages} />
            <Route path='project/:project/version/:version'
              component={ProjectVersion} />
            <Route path='admin/review' component={AdminReview} />
            <Route path='profile/view/:username' component={UserProfile} />
            <Redirect from='profile' to={`profile/view/${username}`} />
          </Route>
        </Router>
      </Provider>
    )
  }
}
