import cx from 'classnames'
import React, { PropTypes } from 'react'

/**
 * Button that can be disabled.
 */
const Button = React.createClass({

  propTypes: {
    title: PropTypes.string,
    onClick: PropTypes.func,
    disabled: PropTypes.bool,
    children: PropTypes.node,
    className: PropTypes.string
  },

  getDefaultProps: () => {
    return {
      disabled: false
    }
  },

  render: function () {
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
})

export default Button
