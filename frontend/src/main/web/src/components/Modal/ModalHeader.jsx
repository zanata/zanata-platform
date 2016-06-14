import React, { PropTypes } from 'react'
import { Base } from '../'

const classes = {
  p: 'P(r1)'
}

const ModalHeader = ({
  children,
  ...props
}) => {
  return (
    <Base tagName='header' {...props}
      theme={classes}>
      {children}
    </Base>
  )
}

ModalHeader.propTypes = {
  children: PropTypes.oneOfType([
    PropTypes.arrayOf(PropTypes.node),
    PropTypes.node]
  )
}

export default ModalHeader
