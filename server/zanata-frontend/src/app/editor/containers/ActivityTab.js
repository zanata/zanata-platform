// @ts-nocheck
import React from 'react'
import * as PropTypes from 'prop-types'
import ActivitySelectList from '../components/ActivitySelectList'
// import LanguageSelectList from '../components/LanguageSelectList'
import CommentBox from '../components/CommentBox'
import ActivityFeedItem from '../components/ActivityFeedItem'
import { isEmpty } from 'lodash'

const DO_NOT_RENDER = undefined
/*
 * ActivityTab for Sidebar, displays TransUnit History, CommentBox
 * for entering new TransUnit comments.
 * TODO: Implement or remove LanguageSelectList:
 * <LanguageSelectList selectItem={selectLanguageFilter}
 * selected={selectedLanguages} />
 */
const ActivityTab = ({
  activityItems,
  postComment,
  selectActivityTypeFilter,
  selectedActivites
}) => {
  const ActivityItems = (isEmpty(activityItems))
    ? DO_NOT_RENDER
    : activityItems.map((item, index) => (
      <ActivityFeedItem key={index} {...item} />))
  return (
    <div>
      <div className='SidebarEditor-wrapper' id='SidebarEditorTabs-pane2'>
        <ActivitySelectList selectItem={selectActivityTypeFilter}
          selected={selectedActivites} />
      </div>
      <div className='SidebarActivity'>
        <CommentBox postComment={postComment} />
        {ActivityItems}
      </div>
    </div>
  )
}

ActivityTab.propTypes = {
  activityItems: PropTypes.arrayOf(PropTypes.shape({
    type: PropTypes.oneOf(['comment', 'revision']).isRequired,
    content: PropTypes.string.isRequired,
    lastModifiedTime: PropTypes.instanceOf(Date).isRequired,
    // TODO damason define type for status
    status: PropTypes.oneOf(['translated', 'needswork', 'approved',
    'rejected', 'untranslated']),
    user: PropTypes.shape({
      name: PropTypes.string.isRequired
    }).isRequired
  })),
  postComment: PropTypes.func.isRequired,
  selectActivityTypeFilter: PropTypes.func.isRequired,
  selectedActivites: ActivitySelectList.idType
  // selectLanguageFilter: PropTypes.func.isRequired,
  // selectedLanguages: LanguageSelectList.idType
}

export default ActivityTab
