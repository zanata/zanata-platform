import ActivitySelectList from '../components/ActivitySelectList'
// import LanguageSelectList from '../components/LanguageSelectList'
import CommentBox from '../components/CommentBox'
import ActivityFeedItem from '../components/ActivityFeedItem'
import {
  ActivityFilter, filterActivityTypes
} from '../utils/activity-util'
// ActivityItemList, activityTypes,
// import { statuses } from '../utils/phrase'
import { transUnitStatusToPhraseStatus } from '../utils/status-util'
import { ALL, COMMENTS, UPDATES } from '../utils/activity-util'
import { isEmpty, orderBy } from 'lodash'
import React from 'react'
import * as PropTypes from 'prop-types'
import { commentTextLimit } from './RejectTranslation'

const DO_NOT_RENDER = undefined

interface ActivityTabProps {
  transHistory?: any,
  postComment: (text: string) => void
  selectActivityTypeFilter: (text: string) => void,
  selectedActivites?: ActivityFilter
}

/*
 * ActivityTab for Sidebar, displays TransUnit History, CommentBox
 * for entering new TransUnit comments.
 * TODO: Implement or remove LanguageSelectList
 */
const ActivityTab: React.SFC<ActivityTabProps> = ({
  transHistory,
  postComment,
  selectActivityTypeFilter,
  selectedActivites
}) => {
  const {historyItems, reviewComments, latest} = transHistory
  // Format a latestHistoryActivityItem from transHistory.latestHistoryItem
  const latestHistoryActivityItem = (latest) => ({
    type: 'revision',
    content: latest.contents[0],
    commentText: latest.revisionComment,
    lastModifiedTime: (new Date(latest.modifiedDate)),
    status: transUnitStatusToPhraseStatus(latest.status),
    user: {
      name: latest.modifiedByPersonName,
      username: latest.modifiedBy
    }
  })
  // Format a reviewCommentsList from reviewComments
  const reviewCommentsList = (reviewComments) => reviewComments.map((value) => {
    return {
      type: 'comment',
      content: value.comment,
      lastModifiedTime: (new Date(value.creationDate)),
      user: {
        name: value.commenterName,
        username: value.username
      }
    }
  })
  // Format a historyItemsList from historyItems
  const historyActivityItems = (historyItems) => historyItems.map((historyItem) => {
    const lastModified = new Date(historyItem.modifiedDate)
    return {
      type: 'revision',
      content: historyItem.contents[0],
      commentText: historyItem.revisionComment,
      lastModifiedTime: lastModified,
      status: transUnitStatusToPhraseStatus(historyItem.status),
      user: {
        name: historyItem.modifiedByPersonName,
        username: historyItem.modifiedBy
      }
    }
  })
  /* Returns Activity Items list filtered by comments and updates */
  const filterActivityItems = (activityFilterType) => {
    if (isEmpty(reviewComments) && isEmpty(latest)) {
        return DO_NOT_RENDER
    }
    const latestItem = latestHistoryActivityItem(latest)
    const historyItemsList = {
      ...historyActivityItems(historyItems), latestItem}
    switch (activityFilterType) {
      case ALL:
        return orderBy({...reviewCommentsList(reviewComments), ...historyItemsList},
          ['lastModifiedTime'], ['desc'])
      case COMMENTS:
        return orderBy(reviewCommentsList(reviewComments), ['lastModifiedTime'], ['desc'])
      case UPDATES:
        return orderBy(historyItemsList, ['lastModifiedTime'], ['desc'])
      default:
        return undefined
    }
  }
  const filteredActivityItems = filterActivityItems(selectedActivites)
  const ActivityItems = filteredActivityItems.map((item, index) => (
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
        <CommentBox postComment={postComment} maxLength={commentTextLimit} />
        {ActivityItems}
      </div>
    </div>
  )
}

ActivityTab.propTypes = {
   transHistory: PropTypes.any,
   postComment: PropTypes.func.isRequired,
   selectActivityTypeFilter: PropTypes.func.isRequired,
   selectedActivites: PropTypes.oneOf(filterActivityTypes),
}

export default ActivityTab
