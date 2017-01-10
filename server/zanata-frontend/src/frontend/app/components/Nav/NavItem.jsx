import React, { PropTypes } from 'react'
import NavIcon from './NavIcon'
import { LogoLoader } from '../../components'
import { merge } from 'lodash'
import { Link } from '../../components'
/**
 * Item of side menu. See Nav.jsx for usage.
 */
const NavItem = ({
  id,
  link,
  icon,
  active,
  title,
  useHref,
  loading,
  ...props
}) => {
  const isLogo = (icon === 'zanata')
  const isSearchLink = (link === '/search')

  let cssClass = 'nav-link' + (active ? ' active' : '')
  cssClass += (isSearchLink ? ' search' : '')

  const text = isLogo ? (<span className='nav-logo'>{title}</span>) : title
  const inverted = false

  const child = isLogo
    ? <LogoLoader inverted={inverted} loading={loading} />
    : <NavIcon name={icon} className='s1' />

  return (
    <Link {...props} id={id} link={link} className={cssClass + ' small'} useHref={useHref}>
      {child}
      {text}
    </Link>
  )
}

NavItem.propTypes = {
  id: PropTypes.string,
  link: PropTypes.string,
  icon: PropTypes.string,
  active: PropTypes.bool,
  title: PropTypes.string,
  useHref: PropTypes.bool,
  loading: PropTypes.bool
}

export default NavItem
