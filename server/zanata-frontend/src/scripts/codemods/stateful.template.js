import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'

/**
 * Action button with an icon and title, unstyled.
 */
class COMPONENT_NAME_HERE extends Component {
  static propTypes = {
    /* Whether it should look fancy */
    fancy: PropTypes.bool.isRequired,
    /* What the cow says */
    noise: PropTypes.string,
    /* arguments: clickCount, sound */
    onClick: PropTypes.func.isRequired
  }

  static defaultProps = {
    noise: 'moo'
  }

  constructor (props) {
    super(props)
    this.state = {
      clicks: 0
    }
  }

  onClick = () => {
    this.setState(prevState => ({
      clicks: prevState.clicks + 1
    }))
    this.props.onClick(`The cow says ${this.props.noise}`)
  }

  render () {
    const { fancy, noise } = this.props
    return (
      <div style={fancy
        ? { color: 'rebeccapurple', fontWeight: 'bold' }
        : {}}>
        <p>TODO write this component</p>
        <button onClick={this.onClick}>Say {noise}!</button>
        <p>{this.state.clicks} {noise}s</p>
      </div>
    )
  }
}

export default COMPONENT_NAME_HERE
