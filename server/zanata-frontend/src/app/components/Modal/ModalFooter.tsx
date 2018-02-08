import * as PropTypes from 'prop-types'
import React from 'react'

const ModalFooter: React.StatelessComponent<{children: any, props?: any}> = ({
  children,
  ...props,
}) => {
  return (
    <div className='modal-footer'
      {...props}
    >
      {children}
    </div>
  )
}

ModalFooter.propTypes = {
  children: PropTypes.oneOfType([
    PropTypes.arrayOf(PropTypes.node),
    PropTypes.node,
  ])
}

export default ModalFooter
