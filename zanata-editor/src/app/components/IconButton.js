import cx from 'classnames'
import Button from './Button'
import Icon from './Icon'
import React, { PropTypes } from 'react'

/**
 * Action button with an icon and title, unstyled.
 */
const IconButton = React.createClass({

  propTypes: {
    icon: PropTypes.string.isRequired,
    title: PropTypes.string.isRequired,
    onClick: PropTypes.func.isRequired,
    disabled: PropTypes.bool,
    iconClass: PropTypes.string,
    buttonClass: PropTypes.string
  },

  render: function () {
    const iconClass = cx('Icon--sm', this.props.iconClass)

    return (
      <Button
        className={this.props.buttonClass}
        disabled={this.props.disabled}
        onClick={this.props.onClick}
        title={this.props.title}>
        <Icon
          name={this.props.icon}
          title={this.props.title}
          className={iconClass}/>
      </Button>
    )
  }
})

export default IconButton
