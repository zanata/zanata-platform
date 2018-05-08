// @ts-nocheck
import React from 'react'
import * as PropTypes from 'prop-types'
import { Loader, Icon } from '../../components'
import { Button, Layout } from 'antd'
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
      <Layout>
        {headerIcon}
        <h2 className='u-textDark u-textUppercase'>
          {title}
          <span className='u-textMutedLeft' title='Total records'>
            {totalCount}
          </span>
        </h2>
        {totalPage > 1 && (
          <div className='teaserHeader-inner'>
            <div className='teaserHeader-pagination'>
              <Button icon='left' className='btn-link iconsHeader'
                aria-label='button'
                disabled={currentPage === 1}
                onClick={() => {
                  updatePage(type, currentPage, totalPage, false)
                }} />
              <span className='pageCurrent'>{currentPage} of {totalPage}</span>
              <Button icon='right' className='btn-link iconsHeader'
                aria-label='button'
                disabled={currentPage === totalPage}
                onClick={() => {
                  updatePage(type, currentPage, totalPage, false)
                }} />
            </div>
          </div>
        )}
        {loading && <Loader className='headerLoader s1' />}
      </Layout>
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
