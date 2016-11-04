import React from 'react'
import { Icon } from '../../components'

const Loader = ({
  ...props
}) => {
  return (
    <span className='loader'
      componentName='Loader'
      {...props}>
      <Icon name='dot' className='dot dot-first' />
      <Icon name='dot' className='dot dot-second' />
      <Icon name='dot' className='dot dot-third' />
    </span>
  )
}

Loader.defaultProps = {
  className: 'n1'
}
export default Loader
