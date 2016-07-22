import Button from './Button'
import { Icon } from 'zanata-ui'
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
    iconSize: PropTypes.string,
    className: PropTypes.string
  },

  render: function () {
    return (
      <Button
        className={this.props.className}
        disabled={this.props.disabled}
        onClick={this.props.onClick}
        title={this.props.title}>
        <Icon
          size={this.props.iconSize || '1'}
          name={this.props.icon}
          title={this.props.title} />
      </Button>
    )
  }
})

export default IconButton
