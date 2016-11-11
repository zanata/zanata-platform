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

import { assign } from 'lodash'
import React, { PropTypes } from 'react'
import { connect } from 'react-redux'
import { routingParamsChanged } from '../actions'

class ParamPropDispatcher extends React.Component {
  componentWillMount () {
    const { dispatchParamsAndQuery, params, location } = this.props
    // always dispatch for initial render
    dispatchParamsAndQuery(params, location.query)
  }
  componentWillReceiveProps (newProps) {
    const { location } = this.props
    const newLocation = newProps.location

    const oldPath = location && (location.pathname + location.search)
    const newPath = newLocation && (newLocation.pathname + newLocation.search)
    if (oldPath !== newPath) {
      this.props.dispatchParamsAndQuery(newProps.params, newLocation.query)
    }
  }
  render () {
    // Just render the single child alone
    return React.Children.only(this.props.children)
  }
}

ParamPropDispatcher.propTypes = {
  children: PropTypes.node,
  dispatchParamsAndQuery: PropTypes.func.isRequired,
  location: PropTypes.shape({
    pathname: PropTypes.string
  }),
  params: PropTypes.object.isRequired
}

function mapDispatchToProps (dispatch) {
  return {
    dispatchParamsAndQuery: (params, query) => {
      // params.splat is captured from ** at the end of the path (react-router)
      dispatch(routingParamsChanged(
        assign({ docId: params.splat }, query, params)))
    }
  }
}

export default connect(null, mapDispatchToProps)(ParamPropDispatcher)
