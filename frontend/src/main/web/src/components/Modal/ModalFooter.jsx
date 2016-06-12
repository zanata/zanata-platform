import React, { PropTypes } from 'react'
import { Base } from '../'

const classes = {
  bgc: 'Bgc(#000.05)',
  flx: 'Flx(n)',
  p: 'Px(r1) Py(r3q)',
  ta: 'Ta(c)'
}

const ModalFooter = ({
  children,
  ...props
}) => {
  return (
    <Base tagName='footer'
      theme={classes}
      {...props}
    >
      {children}
    </Base>
  )
}

ModalFooter.propTypes = {
  children: PropTypes.oneOfType([
    PropTypes.arrayOf(PropTypes.node),
    PropTypes.node]
  )
}

export default ModalFooter
