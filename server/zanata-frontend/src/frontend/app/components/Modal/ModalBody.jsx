import React, { PropTypes } from 'react'

/*
const classes = {
  base: {
    p: 'Px(r1) Pt(r1) Pb(r2)',
    t: 'Ta(s)'
  },
  scrollable: {
    ov: 'Ov(a) Ovx(h)'
  }
}
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
