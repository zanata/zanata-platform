import Button from '../Button'
import { Icon } from '../../../components'
import React from 'react'
import * as PropTypes from 'prop-types'

/**
 * Action button with an icon and title, unstyled.
 */
class IconButton extends React.Component {
  static propTypes = {
    icon: PropTypes.string.isRequired,
    title: PropTypes.string.isRequired,
    onClick: PropTypes.func.isRequired,
    disabled: PropTypes.bool,
    iconSize: PropTypes.string,
    className: PropTypes.string
  }

  render () {
    const iconSize = this.props.iconSize || 's1'
    return (
      <Button
        className={this.props.className}
        disabled={this.props.disabled}
        onClick={this.props.onClick}
        title={this.props.title}>
        <Icon
          title={this.props.title}
          className={iconSize}
          name={this.props.icon} />
      </Button>
    )
  }
}

export default IconButton
