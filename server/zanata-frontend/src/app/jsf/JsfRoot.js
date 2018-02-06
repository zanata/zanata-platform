// @ts-check
import React from 'react'
import * as PropTypes from 'prop-types'
import { Provider } from 'react-redux'
import { Router, Route } from 'react-router'
import ProjectVersion from '../containers/ProjectVersion'
import Project from '../containers/Project'
import Admin from '../containers/Admin'

/** @type { React.StatelessComponent<{children}> } */
const App = ({children}) => {
  return (<div className='containerSidebar'>{children}</div>)
}
App.propTypes = {
  children: PropTypes.element
}

/** @type { React.StatelessComponent<{store, history}> } */
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
