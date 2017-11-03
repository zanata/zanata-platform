import React from 'react'
import PropTypes from 'prop-types'

const ModalBody = ({
  children,
  ...props
}) => {
  return (
    <div {...props}
      className='modal-body scrollable'>
      {children}
    </div>
  )
}

ModalBody.propTypes = {
  children: PropTypes.oneOfType([
    PropTypes.arrayOf(PropTypes.node),
    PropTypes.node]
  )
}

export default ModalBody
