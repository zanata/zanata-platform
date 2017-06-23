import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Provider } from 'react-redux'
import { Router, Route } from 'react-router'
import ProjectVersion from '../containers/ProjectVersion'

const App = (props) => {
  return (<div className='container-sidebar'>{props.children}</div>)
}

App.propTypes = {
  children: PropTypes.element
}

export default class JsfRoot extends Component {
  static propTypes = {
    store: PropTypes.object.isRequired,
    history: PropTypes.object.isRequired
  }

  render () {
    const {
      store,
      history
    } = this.props
    return (
      <Provider store={store}>
        <Router history={history}>
          <Route component={App} >
            <Route path='iteration/view/:project/:version'
              component={ProjectVersion} />
          </Route>
        </Router>
      </Provider>
    )
  }
}
