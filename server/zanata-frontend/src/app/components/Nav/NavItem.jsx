import * as React from 'react'
import * as PropTypes from 'prop-types'
import NavIcon from './NavIcon'
import { LogoLoader, Link } from '../../components'
/**
 * Item of side menu. See Nav.jsx for usage.
 */
const NavItem = ({
  id,
  link,
  small,
  icon,
  active,
  title,
  useHref,
  loading,
  // FIXME tooltip was being passed in ...props and never reaching a valid place
  //       should remove ...props and ensure each prop is handled properly.
  //       Leaving the prop being passed in to keep track of what is intended in
  //       the meantime.
  tooltip,
  ...props
}) => {
  const isLogo = (icon === 'zanata')
  const isSearchLink = (link === '/search')

  let cssClass = 'nav-link' + (active ? ' active' : '')
  cssClass += (small ? ' small' : '')
  cssClass += (isSearchLink ? ' search' : '')

  const text = isLogo
    ? (<span className='navBar-navLogo'>{title}</span>) : title
  const inverted = false

  const child = isLogo
    ? <LogoLoader inverted={inverted} loading={loading} />
    : <NavIcon name={icon} className='s1' />

  return (
    <Link {...props} id={id} link={link} className={cssClass} useHref={useHref}>
      {child}
      {text}
    </Link>
  )
}

NavItem.propTypes = {
  // FIXME are these really optional? Should add .isRequired if not
  id: PropTypes.string,
  link: PropTypes.string,
  small: PropTypes.bool,
  icon: PropTypes.string,
  active: PropTypes.bool,
  title: PropTypes.string,
  useHref: PropTypes.bool,
  loading: PropTypes.bool,
  tooltip: PropTypes.string
}

export default NavItem
