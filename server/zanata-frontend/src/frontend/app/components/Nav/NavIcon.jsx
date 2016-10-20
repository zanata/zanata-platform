import React, { PropTypes } from 'react'
import { Icon } from 'zanata-ui'

const classes = {
  base: {
    d: 'D(b)'
  }
}

/**
 * Icon styled and used in side bar menu. See NavItem for usage.
 */
const NavIcon = ({
  icon,
  size,
  ...props
}) => (
  <Icon
    name={icon}
    size={size}
    theme={classes}
    {...props}
  />
)

NavIcon.propTypes = {
  icon: PropTypes.string,
  size: PropTypes.oneOf(
    ['n2', 'n1', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '10']
  )
}

export default NavIcon
