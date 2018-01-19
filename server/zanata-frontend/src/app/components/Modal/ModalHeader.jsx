import * as React from 'react'
import * as PropTypes from 'prop-types'

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
