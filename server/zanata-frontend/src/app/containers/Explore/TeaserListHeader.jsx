// @ts-nocheck
import React from 'react'
import * as PropTypes from 'prop-types'
import { Icon, Loader } from '../../components'
import Button from 'grommet/components/Button'
import Previous from 'grommet/components/icons/base/Previous'
import Next from 'grommet/components/icons/base/Next'

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
    ? <Icon name={icons[type]} className='iconsHeader s1' /> : null
  const currentPage = page ? parseInt(page) : 1

  /* eslint-disable react/jsx-no-bind */
  return (
    <div className='teaserHeader'>
      {headerIcon}
      <h2 className='u-textDark u-textUppercase'>
        {title}
        <span className='u-textMutedLeft' title='Total records'>
          {totalCount}
        </span>
      </h2>
      {totalPage > 1 && (
        <div className='teaserHeader-inner'>
          <Button icon={<Previous size='xsmall' />}
            onClick={() => { updatePage(type, currentPage, totalPage, false) }}
            plain />
          <span className='pageCurrent'>{currentPage} of {totalPage}</span>
          <Button icon={<Next size='xsmall' />}
            onClick={() => { updatePage(type, currentPage, totalPage, true) }}
            plain />
        </div>
      )}
      {loading && <Loader className='headerLoader s1' />}
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
  loading: PropTypes.bool,
  disabled: PropTypes.bool
}

export default TeaserListHeader
