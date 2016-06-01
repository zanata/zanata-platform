import React, { PropTypes } from 'react'
import { Base } from '../'

const classes = {
  c: 'C(pri)',
  fz: 'Fz(ms3)',
  fw: 'Fw(300)',
  lh: 'Lh(1)'
}

const ModalTitle = ({
  children,
  ...props
}) => {
  return (
    <Base tagName='h2' {...props}
      theme={classes}>
      {children}
    </Base>
  )
}

ModalTitle.propTypes = {
  children: PropTypes.oneOfType([
    PropTypes.arrayOf(PropTypes.node),
    PropTypes.node]
  )
}

export default ModalTitle
