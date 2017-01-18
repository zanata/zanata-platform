import React from 'react'
import Loading from 'react-loading'

const Loader = ({
  ...props
}) => {
  return (
    <span componentName='Loader' className='loader'
      {...props}>
      <span>
        <Loading type='bubbles' color='#546677' />
      </span>
    </span>
  )
}

export default Loader
