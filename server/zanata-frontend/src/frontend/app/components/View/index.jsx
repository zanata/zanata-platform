import React, { PropTypes } from 'react'
/**
 * A flexbox component in div element
 */
const View = ({
  name,
  children,
  ...props
}) => (
  <div name={name}
    className='view'
    {...props}>
    {children}
  </div>
)

View.propTypes = {
  /**
   * Name attribute
   */
  name: PropTypes.string,
  /**
   * The content for this flexbox
   */
  children: PropTypes.node
}

export default View
