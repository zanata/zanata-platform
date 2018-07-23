import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Tooltip from 'antd/lib/tooltip'
import 'antd/lib/tooltip/style/css'
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
      <Tooltip placement="top" title={this.props.title}>
        <Button
          className={this.props.className}
          disabled={this.props.disabled}
          onClick={this.props.onClick}>
          <Icon
            className={iconSize}
            name={this.props.icon} />
        </Button>
      </Tooltip>
    )
  }
}

export default IconButton
