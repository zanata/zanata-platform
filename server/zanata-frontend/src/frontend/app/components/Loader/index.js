import React, { PropTypes } from 'react'
import Icon from '../../components'

const Loader = ({
  ...props
}) => {
  return (
    <span
      componentName='Loader'
      {...props}>
      <Icon name='dot' className='dot dot-first' />
      <Icon name='dot' className='dot dot-second' />
      <Icon name='dot' className='dot dot-third' />
    </span>
  )
}

Loader.propTypes = {

}


export default Loader
