// @ts-check
import React from 'react'
import * as PropTypes from 'prop-types'
import Loading from 'react-loading'

/** @type { React.StatelessComponent<{className}> } */
const Loader = ({ className = 'loader' }) => {
  return (
    <span className={className}>
      <span>
        <Loading type='bubbles' color='#546677' />
      </span>
    </span>
  )
}

Loader.propTypes = {
  className: PropTypes.string
}

export default Loader
