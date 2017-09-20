import React from 'react'
import PropTypes from 'prop-types'
import { Provider } from 'react-redux'
import { Router, Route } from 'react-router'
import ProjectVersion from '../containers/ProjectVersion'
import Project from '../containers/Project'
import Admin from '../containers/Admin'

const App = ({children}) => {
  return (<div className='container-sidebar'>{children}</div>)
}
App.propTypes = {
  children: PropTypes.element
}

const JsfRoot = ({store, history}) => {
  return (
    <Provider store={store}>
      <Router history={history}>
        <Route component={App} >
          <Route path='iteration/view/:project/:version*'
            component={ProjectVersion} />
          <Route path='project/view/:project' component={Project} />
          <Route path='admin/home' component={Admin} />
          <Route path='admin/review' component={Admin} />
        </Route>
      </Router>
    </Provider>
  )
}
JsfRoot.propTypes = {
  store: PropTypes.object.isRequired,
  history: PropTypes.object.isRequired
}

export default JsfRoot
