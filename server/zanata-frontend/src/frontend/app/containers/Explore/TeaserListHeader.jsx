import React, { PropTypes } from 'react'
import { Icon, Loader } from '../../components'
import { Button } from 'react-bootstrap'

/**
 * Header with icon and paging for the TeaserList.
 */
const TeaserListHeader = ({
  title,
  type,
  totalCount,
  sizePerPage,
  page,
  updatePage,
  loading,
  ...props
}) => {
  const icons = {
    Project: 'project',
    LanguageTeam: 'language',
    Person: 'user',
    Group: 'folder'
  }
  const totalPage = Math.floor(totalCount / sizePerPage) +
    (totalCount % sizePerPage > 0 ? 1 : 0)
  const headerIcon = type
    ? <Icon name={icons[type]} className='header-icons s1' /> : null
  const currentPage = page ? parseInt(page) : 1

  /* eslint-disable react/jsx-no-bind */
  return (
    <div className='teaser-header-view-theme'>
      {headerIcon}
      <h2 className='text-dark text-uppercase'>
        {title}
        <span className='record-count' title='Total records'>
          {totalCount}
        </span>
      </h2>
      {totalPage > 1 && (
        <div className='teaser-header-inner'>
          <Button bsStyle='link' disabled={currentPage === 1}
            onClick={() => { updatePage(type, currentPage, totalPage, false) }}>
            <Icon className='headericons s1' name='chevron-left' />
          </Button>
          <span className='current-page'>{currentPage} of {totalPage}</span>
          <Button bsStyle='link' disabled={currentPage === totalPage}
            onClick={() => { updatePage(type, currentPage, totalPage, true) }}>
            <Icon className='headericons s1' name='chevron-right' />
          </Button>
        </div>
      )}
      {loading && <Loader className='header-loader s1' loading name='loader' />}
    </div>
  )
  /* eslint-enable react/jsx-no-bind */
}

TeaserListHeader.propTypes = {
  title: PropTypes.string,
  type: PropTypes.oneOf(
    ['Project', 'LanguageTeam', 'Person', 'Group']
  ),
  totalCount: PropTypes.number,
  sizePerPage: PropTypes.number,
  page: PropTypes.number,
  updatePage: PropTypes.func,
  loading: PropTypes.bool
}

export default TeaserListHeader
