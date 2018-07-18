import ActivitySelectList from "../components/ActivitySelectList"
import CommentBox from "../components/CommentBox"
import ActivityFeedItem from "../components/ActivityFeedItem"
import Pager from "../components/Pager"
import {
  ActivityItemList, activityTypes, ActivityFilterUnion, ActivityFilter
} from "../utils/activity-util"
import { transUnitStatusToPhraseStatus } from "../utils/status-util"
import { ALL, COMMENTS, UPDATES } from "../utils/activity-util"
import { isUndefined, isEmpty, orderBy } from "lodash"
import React from "react"
import { commentTextLimit } from "./RejectTranslation"
import * as t from 'io-ts'
import { getPropTypes } from 'prop-types-ts'

const DO_NOT_RENDER = undefined

// Number of Activity Items to display per paginated page
const COUNT_PER_PAGE = 10

const  ActivityTabStateProps = t.partial({
  transHistory: t.any,
  selectedActivites: ActivityFilterUnion
})

const ActivityTabProps = t.intersection([
  t.interface({
    postComment: t.Function,
    selectActivityTypeFilter: t.Function,
  }), ActivityTabStateProps
])

// this interface partly repeats the t.interface props above, but they don't
// include the Function's signatures
interface ActivityTabDispatchProps {
  postComment: (text: string) => void,
  selectActivityTypeFilter: (text: ActivityFilter) => void,
}

interface ActivityTabProps extends t.TypeOf<typeof ActivityTabStateProps>, ActivityTabDispatchProps {}

type Filter = (transHistory: any) => ActivityItemList

// @ts-ignore any
const commentFilter: Filter = ({reviewComments}) => reviewComments.map((value) => {
  return {
    type: activityTypes.comment,
    content: value.comment,
    lastModifiedTime: (new Date(value.creationDate)),
    user: {
      name: value.commenterName,
      username: value.username
    }
  }
})

// @ts-ignore any
const historyFilter: Filter = ({historyItems, latest}) => ({...(historyItems.map((historyItem) => {
  const lastModified = new Date(historyItem.modifiedDate)
  return {
    type: activityTypes.revision,
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

// @ts-ignore any
const latestHistoryAsItem = (latest) => ({
  type: activityTypes.revision,
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
  ActivityItems: ActivityItemList,
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

  // @ts-ignore any
  constructor (props) {
    super(props)
    this.state = this.defaultState
  }

  public render () {
    const { ActivityItems, pageCount} = this.props
    const { currentPage } = this.state
    const startSlice = currentPage * COUNT_PER_PAGE
    const paginatedActivityItems = ActivityItems
      .slice(startSlice, startSlice + COUNT_PER_PAGE)
    const pager = (pageCount <= 1)
      ? DO_NOT_RENDER
      : <Pager
        intl={undefined}
        firstPage={this.firstPage}
        previousPage={this.previousPage}
        nextPage={this.nextPage}
        lastPage={this.lastPage}
        pageNumber={currentPage + 1}
        pageCount={pageCount}
      />
    return (
      <>
        {pager}
        {paginatedActivityItems}
      </>
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
// const ActivityTab = sfc<typeof ActivityTabStateProps, ActivityTabProps>(ActivityTabProps, ({
const ActivityTab: React.SFC<ActivityTabProps> = ({
  transHistory,
  postComment,
  selectActivityTypeFilter,
  selectedActivites
}) => {
  const {reviewComments, latest} = transHistory
  /* Returns Activity Items list filtered by comments and updates */
  // @ts-ignore any
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
  const ActivityPager = (isUndefined(ActivityItems))
    ? DO_NOT_RENDER
    : <ActivityItemsPager
      // @ts-ignore
      ActivityItems={ActivityItems}
      pageCount={pageCount}
      countPerPage={COUNT_PER_PAGE}
    />
  // Do not show the comment box if no translations to comment on
  const commentBox = isEmpty(latest)
    ? DO_NOT_RENDER
    : <CommentBox postComment={postComment} maxLength={commentTextLimit} />
  return (
    <>
      <div className="SidebarEditor-wrapper" id="SidebarEditorTabs-pane2">
        <ActivitySelectList selectItem={selectActivityTypeFilter}
          selected={selectedActivites} />
      </div>
      <div className="SidebarActivity">
        {commentBox}
        {ActivityPager}
      </div>
    </>
  )
}

ActivityTab.propTypes = getPropTypes(ActivityTabProps) as any

export default ActivityTab
