import React, { PropTypes } from 'react'
import { flattenThemeClasses } from '../utils/styleUtils'

const classes = {
  base: {
    ai: 'Ai(st)',
    d: 'D(f)',
    fld: 'Fld(c)',
    flxs: 'Flxs(0)'
  }
}
/**
 * A flexbox component in div element
 */
const View = ({
  name,
  children,
  theme,
  ...props
}) => (
  <div name={name}
    className={flattenThemeClasses(classes, theme)}
    {...props}>
    {children}
  </div>
)

View.propTypes = {
  name: PropTypes.string,
  children: PropTypes.node,
  theme: PropTypes.object
}

export default View
