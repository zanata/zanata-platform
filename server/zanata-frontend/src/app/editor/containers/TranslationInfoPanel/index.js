// @ts-nocheck
import React from 'react'
import * as PropTypes from 'prop-types'
import { setSidebarVisibility } from '../../actions'
import { postReviewComment } from '../../actions/review-trans-actions'
import { Tabs, FormGroup, InputGroup,
  FormControl, Button, Tab } from 'react-bootstrap'
import Icon from '../../../components/Icon'
import { connect } from 'react-redux'
import { isEmpty, isUndefined, orderBy } from 'lodash'
import { FormattedDate, FormattedTime } from 'react-intl'
import { transUnitStatusToPhraseStatus } from '../../utils/status-util'
import { ALL, COMMENTS, UPDATES } from '../../utils/activity-util'
import GlossaryTab from '../GlossaryTab'
import ActivityTab from '../ActivityTab'

/* Panel displaying info, glossary, activity, etc. */
class TranslationInfoPanel extends React.Component {
  static propTypes = {
    /* close the sidebar */
    close: PropTypes.func.isRequired,
    glossaryCount: PropTypes.number.isRequired,
    hasSelectedPhrase: PropTypes.bool.isRequired,
    localeId: PropTypes.string.isRequired,
    transUnitId: PropTypes.number.isRequired,
    postReviewComment: PropTypes.func.isRequired,
    selectedPhrase: PropTypes.shape({
      msgctxt: PropTypes.string,
      resId: PropTypes.string.isRequired,
      sourceComment: PropTypes.string,
      sourceFlags: PropTypes.string,
      sourceReferences: PropTypes.string,
      lastModifiedBy: PropTypes.string,
      lastModifiedTime: PropTypes.instanceOf(Date),
      revision: PropTypes.number
    }),
    historyItems: PropTypes.arrayOf(
      PropTypes.shape({
        contents: PropTypes.arrayOf(PropTypes.string),
        modifiedBy: PropTypes.string,
        modifiedDate: PropTypes.number,
        optionalTag: PropTypes.string,
        revisionComment: PropTypes.string,
        status: PropTypes.string,
        versionNum: PropTypes.string
      })
    ),
    reviewComments: PropTypes.arrayOf(
      PropTypes.shape({
        comment: PropTypes.string,
        commenterName: PropTypes.string,
        creationDate: PropTypes.number,
        id: PropTypes.shape({id: PropTypes.number, value: PropTypes.number})
      })
    ),
    latestHistoryItem: PropTypes.shape({
      contents: PropTypes.arrayOf(PropTypes.string),
      modifiedBy: PropTypes.string,
      modifiedDate: PropTypes.number,
      optionalTag: PropTypes.string,
      revisionComment: PropTypes.string,
      status: PropTypes.string,
      versionNum: PropTypes.string
    }),
    isRTL: PropTypes.bool.isRequired
  }
  constructor (props) {
    super(props)
    this.handleSelectTab = this.handleSelectTab.bind(this)
    this.selectActivityTypeFilter =
      this.selectActivityTypeFilter.bind(this)
    this.state = {
      key: 1,
      selectedActivites: 'all'
    }
  }
  handleSelectTab (key) {
    this.setState({ key })
  }
  selectActivityTypeFilter (activityFilterType) {
    this.setState(({ selectedActivites: activityFilterType }))
  }
  postComment = (postComment) => {
    const reviewData = {
      localeId: this.props.localeId,
      transUnitId: this.props.transUnitId,
      revision: this.props.selectedPhrase.revision,
      criteriaId: undefined,
      reviewComment: postComment,
      phrase: this.props.selectedPhrase
    }
    this.props.postReviewComment(reviewData)
  }
  sidebarDetails = () => {
    if (!this.props.hasSelectedPhrase) {
      return <span>Select a phrase to see details.</span>
    }
    const {
      msgctxt,
      resId,
      sourceComment,
      sourceFlags,
      sourceReferences,
      lastModifiedBy,
      lastModifiedTime
    } = this.props.selectedPhrase
    const directionClass = this.props.isRTL ? 'rtl' : 'ltr'
    return (
      <ul className={directionClass + ' SidebarEditor-details'}>
        {this.detailItem('Resource ID', resId)}
        {this.detailItem('Message Context', msgctxt)}
        {this.detailItem('Reference', sourceReferences)}
        {this.detailItem('Flags', sourceFlags)}
        {this.detailItem('Source Comment', sourceComment)}
        {this.detailItem('Last Modified',
            this.lastModifiedDisplay(lastModifiedBy, lastModifiedTime))}
      </ul>
    )
  }
  detailItem = (label, value) => {
    const valueDisplay = isEmpty(value)
        ? <span className="SidebarEditor-details--nocontent">No content</span>
        : <span className="SidebarEditor-details--content">{value}</span>
    return (
      <li>
        <span>{label}</span> {valueDisplay}
      </li>
    )
  }
  lastModifiedDisplay = (lastModifiedBy, lastModifiedTime) => {
    if (isUndefined(lastModifiedBy) && isUndefined(lastModifiedTime)) {
      return undefined
    }
    const modifiedByIcon = isUndefined(lastModifiedBy) ? undefined
        : <Icon name="user" className="n1" />
    const modifiedTimeIcon = isUndefined(lastModifiedTime) ? undefined
        : <Icon name="clock" className="n1" />
    const modifiedDate = isUndefined(lastModifiedTime) ? undefined
        : <FormattedDate value={lastModifiedTime} format="medium" />
    const modifiedTime = isUndefined(lastModifiedTime) ? undefined
        : <FormattedTime value={lastModifiedTime} />
    return (
      <span>
        {modifiedByIcon} {lastModifiedBy} {modifiedTimeIcon
        } {modifiedDate} {modifiedTime}
      </span>
    )
  }
  /* URL of the selected phrase, with copy button. */
  phraseLink = () => {
    // TODO need to set up phrase ID in the URL first
    return (
      <FormGroup className="trans-link">
        <InputGroup>
          <InputGroup.Addon><Icon name="copy"
            className="s1" />
          </InputGroup.Addon>
          <FormControl type="text" />
        </InputGroup>
      </FormGroup>
    )
  }
  // Format a reviewCommentsList from reviewComments
  reviewCommentsList = () => {
    return this.props.reviewComments.map((value) => {
      const lastModified = new Date(value.creationDate)
      return {
        type: 'comment',
        content: value.comment,
        lastModifiedTime: lastModified,
        user: {
          name: value.commenterName
        }
      }
    })
  }
  // Format a historyItemsList from historyItems
  historyItemsList = () => {
    const { historyItems, latestHistoryItem } = this.props
    const historyActivityItems = historyItems.map((historyItem) => {
      const lastModified = new Date(historyItem.modifiedDate)
      return {
        type: 'revision',
        content: historyItem.contents[0],
        commentText: historyItem.revisionComment,
        lastModifiedTime: lastModified,
        status: transUnitStatusToPhraseStatus(historyItem.status),
        user: {
          name: historyItem.modifiedBy
        }
      }
    })
    const latestLastModified = new Date(latestHistoryItem.modifiedDate)
    const latestHistoryActivityItem = {
      type: 'revision',
      content: latestHistoryItem.contents[0],
      commentText: latestHistoryItem.revisionComment,
      lastModifiedTime: latestLastModified,
      status: transUnitStatusToPhraseStatus(latestHistoryItem.status),
      user: {
        name: latestHistoryItem.modifiedBy
      }
    }
    return historyActivityItems.concat(latestHistoryActivityItem)
  }
  /* Returns Activity Items list filtered by comments and updates */
  filterActivityItems = (activityFilterType) => {
    const { reviewComments, historyItems } = this.props
    if (isEmpty(reviewComments) && isEmpty(historyItems)) {
      return undefined
    }
    switch (activityFilterType) {
      case ALL:
        return orderBy(this.reviewCommentsList()
          .concat(this.historyItemsList()), ['lastModifiedTime'], ['desc'])
      case COMMENTS:
        return orderBy(this.reviewCommentsList(),
          ['lastModifiedTime'], ['desc'])
      case UPDATES:
        return orderBy(this.historyItemsList(),
          ['lastModifiedTime'], ['desc'])
      default:
        return undefined
    }
  }
  render () {
    const { glossaryCount } = this.props
    const glossaryCountDisplay = glossaryCount > 0
      // TODO kgough display as a badge instead of text in parens
      ? <span className="badge">{this.props.glossaryCount}</span>
      : undefined
    const glossaryTitle = (
      <span>
        <Icon name="glossary" className="s1" parentClassName="gloss-tab-svg" />
        <span className="hide-md">Glossary</span>{glossaryCountDisplay}
      </span>
    )
    // Use this when activity tab is activated
    const activityTitle = (
      <span>
        <Icon name="clock" className="s1 gloss-tab-svg" />
        <span className="hide-md">Activity</span>
      </span>
    )
    const activityItems = this.filterActivityItems(this.state.selectedActivites)
    return (
      <div>
        <h1 className="SidebarEditor-heading">
          <Icon name="info" className="s1" parentClassName='details-svg' />
          <span className="hide-md">Details</span>
          <span className="s1 u-pullRight">
            <Button bsStyle="link" onClick={this.props.close}>
              <Icon name="cross" />
            </Button>
          </span>
        </h1>
        <div className="SidebarEditor-wrapper">
          {this.sidebarDetails()}
        </div>
        <Tabs activeKey={this.state.key}
          onSelect={this.handleSelectTab}
          id="SidebarEditor-tabsPane1">
          <Tab eventKey={1} title={activityTitle}>
            <ActivityTab
              activeKey={this.state.key}
              activityItems={activityItems}
              selectedActivites={this.state.selectedActivites}
              selectActivityTypeFilter={this.selectActivityTypeFilter}
              postComment={this.postComment} />
          </Tab>
          <Tab eventKey={2} title={glossaryTitle}>
            <GlossaryTab
              activeKey={this.state.key} />
          </Tab>
        </Tabs>
      </div>
    )
  }
}
function mapStateToProps (state) {
  const { glossary, phrases, context, activity } = state
  const { detail, selectedPhraseId } = phrases
  const selectedPhrase = detail[selectedPhraseId]
  const { results, searchText } = glossary
  const glossaryResults = results.get(searchText)
  const glossaryCount = glossaryResults ? glossaryResults.length : 0
  const historyItems = activity.transHistory.historyItems
  const reviewComments = activity.transHistory.reviewComments
  const latestHistoryItem = activity.transHistory.latest
  const transUnitId = state.phrases.selectedPhraseId
  const localeId = state.context.lang
  // Need to check whether phrase itself is undefined since the detail may not
  // yet have been fetched from the server.
  const hasSelectedPhrase = !isUndefined(selectedPhraseId) &&
      !isUndefined(selectedPhrase)
  const isRTL = context.sourceLocale.isRTL
  const newProps = {
    glossaryCount,
    hasSelectedPhrase,
    historyItems,
    reviewComments,
    latestHistoryItem,
    transUnitId,
    localeId,
    isRTL
  }
  if (hasSelectedPhrase) {
    newProps.selectedPhrase = selectedPhrase
  }
  return newProps
}

function mapDispatchToProps (dispatch) {
  return {
    close: () => dispatch(setSidebarVisibility(false)),
    postReviewComment: (reviewData) => dispatch(postReviewComment(reviewData))
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(
  TranslationInfoPanel)
