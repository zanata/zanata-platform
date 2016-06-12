import React, { PropTypes } from 'react'
import { merge } from 'lodash'
import { Base } from '../'

const classes = {
  base: {
    p: 'Px(r1) Pt(r1) Pb(r2)',
    t: 'Ta(s)'
  },
  scrollable: {
    ov: 'Ov(a) Ovx(h)'
  }
}

const ModalBody = ({
  children,
  scrollable,
  theme,
  ...props
}) => {
  const themed = merge({},
    classes,
    theme
  )
  const themedState = merge({},
    themed.base,
    scrollable && themed.scrollable
  )
  return (
    <Base {...props}
      theme={themedState}>
      {children}
    </Base>
  )
}

ModalBody.propTypes = {
  children: PropTypes.oneOfType([
    PropTypes.arrayOf(PropTypes.node),
    PropTypes.node]
  ),
  /**
   * Wether to allow the body to be scrollable
   */
  scrollable: PropTypes.bool,
  theme: PropTypes.object
}
ModalBody.defaultProps = {
  scrollable: true
}

export default ModalBody
