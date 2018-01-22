import * as React from 'react'
import * as PropTypes from 'prop-types'

const ModalFooter: React.StatelessComponent<{children, props?}> = ({
  children,
  ...props
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
    PropTypes.node]
  )
}

export default ModalFooter
