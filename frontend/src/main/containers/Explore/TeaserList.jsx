import React, { PropTypes } from 'react'
import { View } from 'zanata-ui'
import TeaserListHeader from './TeaserListHeader'
import ProjectTeaser from './ProjectTeaser'
import GroupTeaser from './GroupTeaser'
import LanguageTeamTeaser from './LanguageTeamTeaser'
import UserTeaser from './UserTeaser'

/**
 * A section of search results with TeaserListHeader and
 * list of results (TeaserComponent)
 */
const TeaserList = ({
  children,
  title,
  totalCount,
  items,
  type,
  sizePerPage,
  page,
  updatePage,
  loading,
  ...props
}) => {
  let TeaserComponent
  const teaserListTheme = {
    base: {
      m: 'Mb(r1h)'
    }
  }
  const listTheme = {
    base: {
      m: 'Mt(r1)'
    }
  }
  switch (type) {
    case 'Project':
      TeaserComponent = ProjectTeaser
      break
    case 'LanguageTeam':
      TeaserComponent = LanguageTeamTeaser
      break
    case 'Person':
      TeaserComponent = UserTeaser
      break
    case 'Group':
      TeaserComponent = GroupTeaser
      break
    default:
      console.error('Unknown teaser type', type)
      TeaserComponent = undefined
      break
  }

  return (
    <View theme={teaserListTheme}>
      <TeaserListHeader title={title} type={type}
        sizePerPage={sizePerPage} page={page}
        totalCount={totalCount} updatePage={updatePage} loading={loading} />
      <View theme={listTheme} id={'explore_' + type + '_result'}>
        {!items || items.length <= 0
          ? (<p className={'C(muted)'}>No Results</p>)
          : (items.map((item, key) => (
            <TeaserComponent details={item} key={key} name='entry' />
        )))
        }
      </View>
    </View>
  )
}

TeaserList.propTypes = {
  title: PropTypes.string,
  totalCount: PropTypes.number,
  /**
   * See ProjectTeaser, LanguageTeamTeaser, GroupTeaser for object type
   */
  items: PropTypes.array,
  type: PropTypes.oneOf(
    ['Project', 'LanguageTeam', 'Person', 'Group']
  ),
  sizePerPage: PropTypes.number,
  page: PropTypes.number,
  updatePage: PropTypes.func,
  loading: PropTypes.bool,
  children: PropTypes.node
}

export default TeaserList
