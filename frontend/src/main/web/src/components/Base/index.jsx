import React, { PropTypes } from 'react'
import warning from 'warning'
import { merge, cloneDeep } from 'lodash'
import { flattenThemeClasses } from '../../utils/styleUtils'

const Base = ({
  atomic = {},
  children,
  className,
  componentName,
  states,
  tagName,
  theme = {},
  ...props
}) => {
  const Component = tagName || 'div'
  const clonedTheme = cloneDeep(theme)
  if (states && clonedTheme.states) {
    const themeStates = clonedTheme.states
    delete clonedTheme.states
    Object.keys(states).forEach((state) => {
      clonedTheme.base = merge({},
        clonedTheme.base,
        states[state] && themeStates[state]
      )
    })
  }
  const classes = flattenThemeClasses(clonedTheme, atomic)
  warning(!className,
    'Please use `theme` instead of `className` to style `' +
    componentName || 'Undefined Component' + '` with `' + className + '`.')
  return (
    <Component
      {...props}
      className={classes}
    >
      {children}
    </Component>
  )
}

Base.propTypes = {
  /**
   * An object of [atomic classes](acss.io/reference) that override any theme
   * based classes. This is useful for one of styles like margin or padding.
   */
  atomic: PropTypes.object,
  children: PropTypes.node,
  /**
   * This should not be used.
   * Prefer theme or [atomic classes](acss.io/reference) object over classes.
   */
  className: PropTypes.string,
  /**
   * The system name for the component.
   * Used for warnings and references.
   */
  componentName: PropTypes.string,
  /**
   * An object of states and whether they are true or not
   * This will be used to merge state styles
   */
  states: PropTypes.object,
  /**
  * HTML element string or React component to render.
  */
  tagName: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.func,
    PropTypes.element
  ]),
  /**
   * Based on an [atomic classes](acss.io/reference) object.
   * This should be merged to a single object before it is passed
   * into the base component.
   * Each component can have it's own structure for it's theme object.
   */
  theme: PropTypes.object
}

export default Base
