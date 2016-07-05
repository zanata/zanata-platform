/**
 * Component to dispatch props.params to get them into redux state.
 *
 * Cannot have any children - just put it as a sibling at the appropriate level
 * Only triggers an update when props.location.pathname has changed, since that
 * is where all the params should come from.
 *
 * This is for convenience, so that params do not have to be passed down through
 * multiple layers of props to be specified in all the dispatched actions that
 * require context information.
 */

import React from 'react'
import { connect } from 'react-redux'
import { routingParamsChanged } from '../actions'

class ParamPropDispatcher extends React.Component {
  componentWillMount () {
    // always dispatch for initial render
    // console.log('will mount')
    this.props.dispatchParams(this.props.params)
  }
  componentWillReceiveProps (newProps) {
    // console.log('willReceiveProps')
    // debugger
    const oldPathname = this.props.location && this.props.location.pathname
    const newPathname = newProps.location && newProps.location.pathname
    if (oldPathname !== newPathname) {
      this.props.dispatchParams(newProps.params)
    }
  }
  render () {
    // Just render the single child alone
    return React.Children.only(this.props.children)
  }
}

// this is where the action should be dispatched
function mapDispatchToProps (dispatch) {
  return {
    dispatchParams: params => {
      dispatch(routingParamsChanged(params))
    }
  }
}

export default connect(null, mapDispatchToProps)(ParamPropDispatcher)
