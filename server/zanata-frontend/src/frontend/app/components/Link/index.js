import React, { PropTypes } from 'react'
import { Link as RouterLink } from 'react-router'

/**
 * Common link component which generates <a href> or in-page navigation link
 * based on useHref.
 */
const Link = ({
  id,
  children,
  link,
  useHref = false,
  ...props
}) => {
  if (useHref) {
    return (
      <a href={link} id={id}
        {...props}>
        {children}
      </a>
    )
  }
  return (
    <RouterLink
      id={id}
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
