import React from 'react'
import * as PropTypes from 'prop-types'
import { Loader } from '../../components'

/** @type
    { React.StatelessComponent<{
        children: any,
        loading: boolean,
        loadingText: string,
        props?: any}>
    } */
const LoaderText = ({
  children,
  loading,
  loadingText,
  ...props
}) => {
  return (
    <span className='loaderText' {...props}>
          {loading
            ? <p className='tc f5 b'>{loadingText}<Loader /></p>
            : children
          }
    </span>
  )
}

LoaderText.propTypes = {
  /**
   * The text/element that is display when there **is no** loading
   */
  children: PropTypes.node,
  /**
   * The text that is displayed when there **is** loading
   */
  loadingText: PropTypes.string,
  /**
   * If the component is loading or not
   */
  loading: PropTypes.bool
}

LoaderText.defaultProps = {
  loading: false,
  loadingText: 'Loading'
}

export default LoaderText
