import cx from 'classnames'
import React from 'react'
import * as PropTypes from 'prop-types'

/**
 * Button that can be disabled.
 */
class Button extends React.Component {
  static propTypes = {
    title: PropTypes.string,
    onClick: PropTypes.func,
    disabled: PropTypes.bool,
    children: PropTypes.node,
    className: PropTypes.string
  }

  static defaultProps = {
    disabled: false
  }

  render () {
    const className = cx(this.props.className,
      { 'is-disabled': this.props.disabled })

    return (
      <button
        className={className}
        disabled={this.props.disabled}
        onClick={this.props.disabled ? undefined : this.props.onClick}
        title={this.props.title}>
        {this.props.children}
      </button>
    )
  }
}

export default Button
