import React, { PropTypes } from 'react'
import { Loader } from '../../components'

const LoaderText = ({
  children,
  loading,
  loadingText,
  ...props
}) => {
  return (
    <span className='loader-text'
      componentName='LoaderText' {...props}>
          {loading
            ? <span>{loadingText}
              <Loader /></span>
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
