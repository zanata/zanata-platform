import React, { PropTypes } from 'react'
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
    <div className='glossaryhead-wrapper'>
      <div className='glossaryhead-base'>
        <div className='inner-view-theme'>
          <Link link='/' className='logo-link-theme'>
            <Icon name='zanata' className='s3' />
          </Link>
          <h1 className='glossaryhead-title'>
            <Row>
              {icon}{title || 'Title'}
              {extraHeadingElements}
            </Row>
          </h1>
          <div className='glossaryhead-actions-theme'>
            {extraElements}
          </div>
        </div>
        {children && (
          <div className='inner-view-theme'>
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
