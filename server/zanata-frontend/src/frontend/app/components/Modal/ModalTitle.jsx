import React, { PropTypes } from 'react'

const ModalTitle = ({
  children,
  ...props
  }) => {
  return (
    <h2 {...props}
      className='modal-title'>
      {children}
    </h2>
  )
}

ModalTitle.propTypes = {
  children: PropTypes.oneOfType([
    PropTypes.arrayOf(PropTypes.node),
    PropTypes.node]
  )
}

export default ModalTitle
