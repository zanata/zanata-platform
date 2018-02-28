import React from 'react'
import * as PropTypes from 'prop-types'

/**
 * @type { React.StatelessComponent<{children}> }
 */
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
