import React, { PropTypes } from 'react'

/* const classes = {
  bgc: 'Bgc(#000.05)',
  flx: 'Flx(n)',
  p: 'Px(r1) Py(r3q)',
  ta: 'Ta(c)'
} */

const ModalFooter = ({
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
