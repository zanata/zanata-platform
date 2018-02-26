// @ts-nocheck
import React from 'react'
import * as PropTypes from 'prop-types'
import NavItem from './NavItem'
import { getDswid } from '../../utils/UrlHelper'
import { remove } from 'lodash'
import { allowRegister, isLoggedIn, isAdmin, user } from '../../config'

const dswid = getDswid()

/**
 * Item properties:
 *
 * - link: URL path for the page.
 * - jsPage: indicator for ReactJS page. Force to use href for their links.
 *
 */
const items = [
  {
    icon: 'zanata',
    link: '/',
    href: '/',
    title: 'Zanata',
    auth: 'public',
    id: 'nav_home'
  },
  {
    icon: 'search',
    link: '/explore' + dswid,
    jsPage: true,
    title: 'Explore',
    auth: 'public',
    id: 'nav_search'
  },
  {
    small: true,
    icon: 'import',
    link: '/login',
    title: 'Log In',
    auth: 'loggedout',
    id: 'nav_login'
  },
  {
    small: true,
    icon: 'upload',
    link: '/signup',
    title: 'Sign Up',
    auth: 'loggedout',
    id: 'nav_sign_up'
  },
  {
    small: true,
    icon: 'dashboard',
    link: '/dashboard' + dswid,
    title: 'Dashboard',
    auth: 'loggedin',
    id: 'nav_dashboard'
  },
  {
    small: true,
    icon: 'user',
    link: '/profile' + dswid,
    jsPage: true,
    title: 'Profile',
    auth: 'loggedin',
    id: 'nav_profile'
  },
  {
    icon: 'glossary',
    link: '/glossary' + dswid,
    jsPage: true,
    title: 'Glossary',
    auth: 'loggedin',
    id: 'nav_glossary'
  },
  {
    icon: 'language',
    link: '/languages' + dswid,
    jsPage: true,
    title: 'Languages',
    auth: 'loggedin',
    id: 'nav_language'
  },
  {
    icon: 'settings',
    link: '/dashboard/settings' + dswid,
    title: 'Settings',
    auth: 'loggedin',
    id: 'nav_settings'
  },
  {
    icon: 'admin',
    link: '/admin/home' + dswid,
    title: 'Admin',
    auth: 'admin',
    id: 'nav_admin'
  },
  {
    icon: 'logout',
    link: '/account/sign_out' + dswid,
    title: 'Log Out',
    auth: 'loggedin',
    id: 'nav_logout'
  },
  {
    small: true,
    icon: 'ellipsis',
    link: '/info' + dswid,
    title: 'More',
    auth: 'public',
    id: 'nav_more'
  }
]

/**
 * Generates side menu bar with icons
 */
const Nav = ({
  active,
  links,
  isJsfPage,
  loading,
  ...props
}) => {
  let auth = 'loggedout'
  if (isLoggedIn) {
    auth = isAdmin ? 'admin' : 'loggedin'
  }
  const admin = (auth === 'admin')
  const username = user ? user.username : ''
  if (!allowRegister) {
    // we don't allow public registration
    remove(items, item => item.id === 'nav_sign_up')
  }

  return (
    <nav
      {...props}
      id='nav'
      name={username}
      className='nav-bar'>
      {items.map((item, itemId) => {
        if (((item.auth === 'public') || (item.auth === auth) ||
          (item.auth === 'loggedin' && admin))) {
          let link
          if (isJsfPage) {
            // jsf pages
            link = links[item.link]
              ? (links.context + links[item.link])
              : (links.context + item.link)
          } else {
            // react pages, /app/index.xhtml
            link = links[item.link]
                ? (links.context + links[item.link])
                : (links.context + item.link)
          }

          const useHref = isJsfPage || !item.jsPage
          let linkWithoutDswid = link.replace(dswid, '')

          /**
           * TODO: remove this check, need better handling of
           * selected page for side navigation
           *
           * This is to handle profile page selection as url
           * in server will be rewritten with /profile/username
           */
          if (linkWithoutDswid === '/profile' && username) {
            linkWithoutDswid += '/view/' + username
          }
          const isActive = active === linkWithoutDswid
          return <NavItem key={itemId}
            loading={loading}
            id={item.id}
            small={item.small}
            active={isActive}
            link={link}
            useHref={useHref}
            icon={item.icon}
            tooltip={item.tooltip}
            title={item.title} />
        }
        return null
      })}
    </nav>
  )
}

Nav.propTypes = {
  /**
   * Current active path
   */
  active: PropTypes.string,
  /**
   * Object of links
   * FIXME this looks wrong, 'context' is prepended to all links so they
   *       cannot include the protocol, server or port.
   * e.g.
   * {
   * 'context': http://localhost:8080,
   * 'helpPage': http://localhost/help
   * }
   */
  links: PropTypes.object,
  /**
   * If true, all links will be using href
   * If false, RouterLink will be use
   */
  isJsfPage: PropTypes.bool,
  loading: PropTypes.bool
}

export default Nav
