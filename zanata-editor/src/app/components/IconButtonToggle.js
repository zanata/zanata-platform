import cx from 'classnames'
import IconButton from './IconButton'
import React, { PropTypes } from 'react'

/**
 * An action button with an icon, title and background styling.
 *
 * Like IconButton but changes colour based on 'active' prop.
 *
 * props.className is applied to the icon
 */
const IconButtonToggle = React.createClass({

  propTypes: {
    icon: PropTypes.string.isRequired,
    title: PropTypes.string.isRequired,
    onClick: PropTypes.func.isRequired,
    active: PropTypes.bool.isRequired,
    disabled: PropTypes.bool,
    className: PropTypes.string
  },

  getDefaultProps: () => {
    return {
      active: false
    }
  },

  render: function () {
    const buttonClass = cx(this.props.buttonClass,
      'Button Button--snug u-roundish Button--invisible',
      { 'is-active': this.props.active })

    return (
      <IconButton
        {...this.props}
        iconClass={this.props.className}
        buttonClass={buttonClass}/>
    )
  }
})

export default IconButtonToggle
