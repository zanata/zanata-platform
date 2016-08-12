import React, { PropTypes } from 'react'
import {
  Icon,
  ButtonLink,
  Loader,
  Heading,
  View
} from 'zanata-ui'

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
  const viewTheme = {
    base: {
      fld: '',
      ai: 'Ai(c)'
    }
  }

  const icons = {
    Project: 'project',
    LanguageTeam: 'language',
    Person: 'user',
    Group: 'folder'
  }
  const totalPage = Math.floor(totalCount / sizePerPage) +
    (totalCount % sizePerPage > 0 ? 1 : 0)
  const headerIcon = type
    ? <Icon name={icons[type]} theme={{ base: { m: 'Mend(rq)' } }} /> : null
  const currentPage = page ? parseInt(page) : 1

  /* eslint-disable react/jsx-no-bind */
  return (
    <View theme={viewTheme}>
      {headerIcon}
      <Heading
        level='2'
        theme={{ base: { c: 'C(dark)', tt: 'Tt(u)' } }}>
        {title}
        <span className='C(muted) Mstart(rq)' title='Total records'>
          {totalCount}
        </span>
      </Heading>
      {totalPage > 1 && (
        <div className='Mstart(rh) C(pri) D(f) Ai(c)'>
          <ButtonLink disabled={currentPage === 1}
            onClick={() => { updatePage(type, currentPage, totalPage, false) }}>
            <Icon name='chevron-left' size='1' />
          </ButtonLink>
          <span className='C(muted) Mx(re)'>{currentPage} of {totalPage}</span>
          <ButtonLink disabled={currentPage === totalPage}
            onClick={() => { updatePage(type, currentPage, totalPage, true) }}>
            <Icon name='chevron-right' size='1' />
          </ButtonLink>
        </div>
      )}
      {loading && <Loader theme={{ base: { fz: 'Fz(ms1)', m: 'MStart(rh)' } }}
        size='1' loading name='loader' />}
    </View>
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
