import React, { PropTypes } from 'react'

/* const classes = {
  p: 'P(r1)'
} */

const ModalHeader = ({
  children,
  ...props
}) => {
  return (
    <div className='modal-header' {...props}>
      {children}
    </div>
  )
}

ModalHeader.propTypes = {
  children: PropTypes.oneOfType([
    PropTypes.arrayOf(PropTypes.node),
    PropTypes.node]
  )
}

export default ModalHeader
