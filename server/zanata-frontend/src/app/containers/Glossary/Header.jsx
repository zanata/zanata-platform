import React from 'react'
import PropTypes from 'prop-types'
import { Icon, Link } from '../../components'
import { Row } from 'react-bootstrap'

/**
 * Page top header with Zanata logo
 */
const Header = ({
  children,
  title,
  icon,
  extraElements,
  extraHeadingElements,
  ...props
}) => {
  return (
    <div className='glossaryHeader-wrapper'>
      <div className='glossaryHeader-base'>
        <div className='innerView'>
          <Link link='/' className='logoLink'>
            <Icon name='zanata' className='s3' />
          </Link>
          <h1 className='glossaryHeader-title'>
            <Row>
              {icon}{title || 'Title'}
              {extraHeadingElements}
            </Row>
          </h1>
          <div className='glossaryHeader-actions'>
            {extraElements}
          </div>
        </div>
        {children && (
          <div className='innerView'>
            {children}
          </div>
        )}
      </div>
    </div>
  )
}

Header.propTypes = {
  children: PropTypes.node,
  title: PropTypes.string,
  icon: PropTypes.node,
  tooltip: PropTypes.string,
  /**
   * Extra react node (html element) which will be display in the header
   */
  extraElements: PropTypes.oneOfType([
    PropTypes.arrayOf(PropTypes.node),
    PropTypes.node]
  ),
  extraHeadingElements: PropTypes.object
}

export default Header
