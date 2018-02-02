// @ts-check
import React from 'react'
import * as PropTypes from 'prop-types'
import { Link as RouterLink } from 'react-router'

/**
 * Common link component which generates <a href> or in-page navigation link
 * based on useHref.
 * @type { React.StatelessComponent<{id, children, link, useHref, props?}> } */
const Link = ({
  children,
  link,
  useHref = false,
  ...props
}) => {
  if (useHref) {
    return (
      <a href={link}
        {...props}>
        {children}
      </a>
    )
  }
  return (
    <RouterLink
      to={link}
      {...props}
    >
      {children}
    </RouterLink>
  )
}

Link.propTypes = {
  /**
   * id attribute
   */
  id: PropTypes.string,
  /**
   * HTML url or location#hash
   */
  link: PropTypes.string,
  /**
   * Toggle whether to use <a href> or in-page navigation. Default is 'false'
   */
  useHref: PropTypes.bool,
  children: PropTypes.node
}

export default Link
