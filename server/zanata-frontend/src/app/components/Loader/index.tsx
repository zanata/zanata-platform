import * as PropTypes from 'prop-types'
import React from 'react'
import Loading from 'react-loading'

const Loader: React.SFC<LoaderProps> = ({ className = 'loader' }) => {
  return (
    <span className={className}>
      <span>
        <Loading type='bubbles' color='#546677' />
      </span>
    </span>
  )
}

interface LoaderProps {
  className: string
}

Loader.propTypes = {
  className: PropTypes.string
}

export default Loader
