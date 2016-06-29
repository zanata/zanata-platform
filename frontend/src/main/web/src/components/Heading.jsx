import React, { PropTypes } from 'react'
import { flattenThemeClasses } from '../utils/styleUtils'

const classes = {
  base: {
    c: 'C(pri)',
    m: 'M(0)',
    fz: 'Fz(ms0)',
    fw: 'Fw(i)',
    lh: 'Lh(1)'
  }
}
/**
 * Generates <h1> html element
*/
const Heading = ({
  children,
  theme,
  ...props
}) => {
  const headingClasses = flattenThemeClasses(classes, theme)
  return <h1 {...props} className={headingClasses} >
      {children}
  </h1>
}

Heading.propTypes = {
  children: PropTypes.node,
  theme: PropTypes.object
}

export default Heading
