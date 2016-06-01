import React, { PropTypes } from 'react'
import { flattenThemeClasses } from '../utils/styleUtils'
import { Link as RouterLink } from 'react-router'

const classes = {
  base: {
    c: 'C(pri)',
    td: 'Td(n)',
    trs: 'Trs(aeo)',
    hover: {
      filter: 'Brightness(.75):h'
    },
    focus: {
      filter: 'Brightness(.75):f'
    },
    active: {
      filter: 'Brightness(.5):a'
    }
  }
}

/**
 * Common link component which generates <a href> or in-page navigation link
 * based on useHref.
 */
const Link = ({
  id,
  children,
  theme,
  link,
  useHref,
  ...props
}) => {
  if (useHref) {
    return (
      <a href={link} id={id}
        className={flattenThemeClasses(classes, theme)} {...props}>
        {children}
      </a>
    )
  }
  return (
    <RouterLink
      id={id}
      to={link}
      className={flattenThemeClasses(classes, theme)}
      {...props}
    >
      {children}
    </RouterLink>
  )
}

Link.propTypes = {
  id: PropTypes.string,
  theme: PropTypes.object,
  link: PropTypes.string,
  useHref: PropTypes.bool
}

export default Link
