import React, { PropTypes } from 'react'
import { merge } from 'lodash'
import { Base } from '../'

export const classes = {
  base: {
    whs: 'Whs(nw)',
    c: 'C(i)',
    o: 'O(n)',
    ap: 'Ap(n)',
    disabled: {
      cur: 'Cur(d):di',
      op: 'Op(.6):di',
      pe: 'Pe(n):di'
    }
  }
}

const Button = ({
  children,
  theme = {},
  ...props
}) => {
  const themed = merge({}, classes, theme)
  return (
    <Base
      componentName='Button'
      {...props}
      tagName='button'
      theme={themed}
    >
      {children}
    </Base>
  )
}

Button.propTypes = {
  children: PropTypes.node,
  /**
   * Toggle whether the button is disabled or not. Default is 'false'
   */
  disabled: PropTypes.bool,
  /**
   * Used to override the default theme
   */
  theme: PropTypes.object
}

export default Button
