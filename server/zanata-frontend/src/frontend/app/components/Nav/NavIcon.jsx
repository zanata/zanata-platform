import React, { PropTypes } from 'react'
import { Icon } from '../../components'

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
    className={size + ' nav-icon'}
    {...props}
  />
)

NavIcon.propTypes = {
  icon: PropTypes.string,
  size: PropTypes.oneOf(
    ['n2', 'n1', 's0', 's1', 's2', 's3', 's4', 's5', 's6', 's7', 's8',
     's9', 's10']
  )
}

export default NavIcon
