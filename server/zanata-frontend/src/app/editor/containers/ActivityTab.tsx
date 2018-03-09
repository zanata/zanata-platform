import ActivitySelectList from '../components/ActivitySelectList'
// import LanguageSelectList from '../components/LanguageSelectList'
import CommentBox from '../components/CommentBox'
import ActivityFeedItem from '../components/ActivityFeedItem'
import {
  ActivityFilter, ActivityItemList, activityTypes, filterActivityTypes
} from '../utils/activity-util'
import { statuses } from '../utils/phrase'
import { isEmpty } from 'lodash'
import React from 'react'
import * as PropTypes from 'prop-types'

const DO_NOT_RENDER = undefined

interface ActivityTabProps {
  activityItems: ActivityItemList,
  postComment: (text: string) => void
  selectActivityTypeFilter: (text: string) => void,
  selectedActivites: ActivityFilter
}

/*
 * ActivityTab for Sidebar, displays TransUnit History, CommentBox
 * for entering new TransUnit comments.
 * TODO: Implement or remove LanguageSelectList
 */
const ActivityTab: React.SFC<ActivityTabProps> = ({
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
        {/* <LanguageSelectList selectItem={selectLanguageFilter}
          *  selected={selectedLanguages} /> */}
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
     type: PropTypes.oneOf(activityTypes).isRequired,
     content: PropTypes.string.isRequired,
     lastModifiedTime: PropTypes.instanceOf(Date).isRequired,
     status: PropTypes.oneOf(statuses),
     user: PropTypes.shape({
       name: PropTypes.string.isRequired
     }).isRequired
   })).isRequired,
   postComment: PropTypes.func.isRequired,
   selectActivityTypeFilter: PropTypes.func.isRequired,
   selectedActivites: PropTypes.oneOf(filterActivityTypes),
}

export default ActivityTab
