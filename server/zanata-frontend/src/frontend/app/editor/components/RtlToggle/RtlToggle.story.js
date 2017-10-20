import IconButton from '../IconButton'
import React from 'react'
import PropTypes from 'prop-types'

render () {
  return (
      <IconButton
          icon="translate"
          title="RTL"
          onClick={this.props.onClick}
          className="EditorButton Button--snug u-roundish Button--invisible"></IconButton>
  )
}
