import React from 'react'
import PropTypes from 'prop-types'
import { Provider } from 'react-redux'
import { Router, Route } from 'react-router'
import ProjectVersion from '../containers/ProjectVersion'

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
