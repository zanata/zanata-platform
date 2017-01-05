import React from 'react'
import Loading from 'react-loading'

const Loader = ({
  ...props
}) => {
  return (
    <span componentName='Loader' className='loader'
      {...props}>
      <span>
        <Loading type='bubbles' color='#e3e3e3' />
      </span>
    </span>
  )
}

export default Loader
