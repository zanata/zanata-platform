import ActivitySelectList from "../components/ActivitySelectList"
// import LanguageSelectList from "../components/LanguageSelectList"
import CommentBox from "../components/CommentBox"
import ActivityFeedItem from "../components/ActivityFeedItem"
import Pager from "../components/Pager"
import {
  ActivityFilter, filterActivityTypes
} from "../utils/activity-util"
import { transUnitStatusToPhraseStatus } from "../utils/status-util"
import { ALL, COMMENTS, UPDATES } from "../utils/activity-util"
import { isUndefined, isEmpty, orderBy } from "lodash"
import React from "react"
import * as PropTypes from "prop-types"
import { commentTextLimit } from "./RejectTranslation"

const DO_NOT_RENDER = undefined

// Number of Activity Items to display per paginated page
const COUNT_PER_PAGE = 10

interface ActivityTabProps {
  transHistory?: any,
  postComment: (text: string) => void
  selectActivityTypeFilter: (text: string) => void,
  selectedActivites?: ActivityFilter
}

// interface FilterItem {
//   type: string,
//   content: string,
//   lastModifiedTime: any,
//   user: any
// }

type Filter = (transHistory: any) => any

const commentFilter: Filter = ({reviewComments}) => reviewComments.map((value) => {
  return {
    type: "comment",
    content: value.comment,
    lastModifiedTime: (new Date(value.creationDate)),
    user: {
      name: value.commenterName,
      username: value.username
    }
  }
})

const historyFilter: Filter = ({historyItems, latest}) => ({...(historyItems.map((historyItem) => {
  const lastModified = new Date(historyItem.modifiedDate)
  return {
    type: "revision",
    content: historyItem.contents[0],
    commentText: historyItem.revisionComment,
    lastModifiedTime: lastModified,
    status: transUnitStatusToPhraseStatus(historyItem.status),
    user: {
      name: historyItem.modifiedByPersonName,
      username: historyItem.modifiedBy
    }
  }
})), latest: latestHistoryAsItem(latest)})

const latestHistoryAsItem = (latest) => ({
  type: "revision",
  content: latest.contents[0],
  commentText: latest.revisionComment,
  lastModifiedTime: (new Date(latest.modifiedDate)),
  status: transUnitStatusToPhraseStatus(latest.status),
  user: {
    name: latest.modifiedByPersonName,
    username: latest.modifiedBy
  }
})

interface Props {
  ActivityItems?: any,
  pageCount: number,
  countPerPage: number
}

interface State {
  currentPage: number
}

/*
 * Helper Class to handle Pagination State
 */
class ActivityItemsPager extends React.Component<Props, State> {
  private defaultState = {
    currentPage: 0
  }

  constructor (props) {
    super(props)
    this.state = this.defaultState
  }

  public render () {
    const { ActivityItems, pageCount} = this.props
    const { currentPage } = this.state
    const startSlice = currentPage * COUNT_PER_PAGE
    const PaginatedActivityItems = (isUndefined(ActivityItems))
      ? DO_NOT_RENDER
      : ActivityItems.slice(startSlice, startSlice + COUNT_PER_PAGE)
    return (
      <div>
        <Pager
          intl={undefined}
          firstPage={this.firstPage}
          previousPage={this.previousPage}
          nextPage={this.nextPage}
          lastPage={this.lastPage}
          pageNumber={currentPage + 1}
          pageCount={pageCount}
        />
        {PaginatedActivityItems}
      </div>
    )
  }
  private firstPage = () => {
    this.setState({currentPage: 0})
  }
  private previousPage = () => {
    this.setState((prevState, _props) => ({
        currentPage: prevState.currentPage - 1
    }));
  }
  private nextPage = () => {
    this.setState((prevState, _props) => ({
        currentPage: prevState.currentPage + 1
    }));
  }
  private lastPage = () => {
    this.setState((_prevState, props) => ({
        currentPage: props.pageCount - 1
    }));
  }
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
  const {reviewComments, latest} = transHistory
  /* Returns Activity Items list filtered by comments and updates */
  const filterActivityItems = (activityFilterType) => {
    if (isEmpty(reviewComments) && isEmpty(latest)) {
        return DO_NOT_RENDER
    }
    let results: any[]
    switch (activityFilterType) {
      case ALL:
        results = {...historyFilter(transHistory), ...commentFilter(transHistory)}
        break
      case COMMENTS:
        results = commentFilter(transHistory)
        break
      case UPDATES:
        results = historyFilter(transHistory)
        break
      default:
        throw new Error()
    }
    return orderBy(results, ["lastModifiedTime"], ["desc"])
  }
  const filteredActivityItems = filterActivityItems(selectedActivites)
  const ActivityItems = (isUndefined(filteredActivityItems))
    ? DO_NOT_RENDER
    : filteredActivityItems.map((item, index) => (
      <ActivityFeedItem key={index} {...item} />))
  const pageCount = (isUndefined(ActivityItems))
    ? 0
    : Math.ceil(Object.keys(ActivityItems).length / COUNT_PER_PAGE)
  return (
    <div>
      <div className="SidebarEditor-wrapper" id="SidebarEditorTabs-pane2">
        <ActivitySelectList selectItem={selectActivityTypeFilter}
          selected={selectedActivites} />
      </div>
      <div className="SidebarActivity">
        <CommentBox postComment={postComment} maxLength={commentTextLimit} />
        <ActivityItemsPager
          ActivityItems={ActivityItems}
          pageCount={pageCount}
          countPerPage={COUNT_PER_PAGE}
        />
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
