import React from 'react'
import * as PropTypes from 'prop-types'
import { setSidebarVisibility } from '../../actions'
import { toggleGlossary, toggleActivity } from '../../actions/header-actions'
import { getGlossaryVisible, getActivityVisible } from '../../reducers'
import { postReviewComment } from '../../actions/review-trans-actions'
import { Tabs, FormGroup, InputGroup, InputGroupAddon,
  FormControl, Button, Tab } from 'react-bootstrap'
import Icon from '../../../components/Icon'
import { connect } from 'react-redux'
import { FormattedMessage } from 'react-intl'
import { isUndefined } from 'lodash'
import GlossaryTab from '../GlossaryTab'
import ActivityTab from '../ActivityTab'
import DetailsPane from './DetailsPane'
import ConcurrentModal from '../../components/ConcurrentModal'
import Notification from 'antd/lib/notification'
import 'antd/lib/notification/style/css'

/* React Bootstrap Tab keys for tracking active Tab */
const activityTabKey = 1
const glossaryTabKey = 2

const historyShape = PropTypes.shape({
  contents: PropTypes.arrayOf(PropTypes.string),
  modifiedBy: PropTypes.string,
  modifiedDate: PropTypes.number,
  optionalTag: PropTypes.string,
  revisionComment: PropTypes.string,
  status: PropTypes.string,
  versionNum: PropTypes.string
})

const commentShape = PropTypes.shape({
  comment: PropTypes.string,
  commenterName: PropTypes.string,
  creationDate: PropTypes.number,
  id: PropTypes.shape({id: PropTypes.number, value: PropTypes.number})
})

/* Panel displaying info, glossary, activity, etc. */
class TranslationInfoPanel extends React.Component {
  static propTypes = {
    activityVisible: PropTypes.bool.isRequired,
    /* close the sidebar */
    close: PropTypes.func.isRequired,
    conflict: PropTypes.any,
    glossaryCount: PropTypes.number.isRequired,
    glossaryVisible: PropTypes.bool.isRequired,
    hasSelectedPhrase: PropTypes.bool.isRequired,
    localeId: PropTypes.string.isRequired,
    transUnitId: PropTypes.number,
    postReviewComment: PropTypes.func.isRequired,
    toggleGlossary: PropTypes.func.isRequired,
    toggleActivity: PropTypes.func.isRequired,
    transHistory: PropTypes.shape({
      historyItems: PropTypes.arrayOf(historyShape),
      reviewComments: PropTypes.arrayOf(commentShape),
      latestHistoryItem: historyShape
    }),
    notification: PropTypes.shape({
      severity: PropTypes.string,
      message: PropTypes.string,
      description: PropTypes.string,
      duration: PropTypes.number
    }),
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
    isRTL: PropTypes.bool.isRequired
  }
  constructor (props) {
    super(props)
    this.handleSelectTab = this.handleSelectTab.bind(this)
    this.selectActivityTypeFilter =
      this.selectActivityTypeFilter.bind(this)
    this.state = {
      key: activityTabKey,
      selectedActivites: 'all'
    }
  }
  componentDidUpdate (prevProps) {
    const { notification } = this.props
    if (notification && prevProps.notification !== notification) {
      Notification[notification.severity]({
        message: notification.message,
        description: notification.description,
        duration: null
      })
    }
  }
  handleSelectTab (key) {
    if (key === activityTabKey) {
      this.props.toggleActivity()
    } else {
      if (key === glossaryTabKey) this.props.toggleGlossary()
    }
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
  /* URL of the selected phrase, with copy button. */
  phraseLink = () => {
    return (
      <FormGroup className="trans-link">
        <InputGroup>
          <InputGroupAddon>
            <Icon name="copy"
              className="s1" />
          </InputGroupAddon>
          <FormControl type="text" />
        </InputGroup>
      </FormGroup>
    )
  }
  render () {
    const { activityVisible, glossaryVisible, glossaryCount } = this.props
    const glossaryCountDisplay = glossaryCount > 0
      // TODO kgough display as a badge instead of text in parens
      ? <span className="badge">{this.props.glossaryCount}</span>
      : undefined
    const glossaryTitle = (
      <span>
        <Icon name="glossary" className="s1" parentClassName="gloss-tab-svg" />
        <span className="hide-md">
          <FormattedMessage id='TranslationInfoPanel.glossaryTitle'
            description={'Title for the Glossary Panel'}
            defaultMessage='Glossary' />
        </span>{glossaryCountDisplay}
      </span>
    )
    // Use this when activity tab is activated
    const activityTitle = (
      <span>
        <Icon name="clock" className="s1 gloss-tab-svg" />
        <span className="hide-md">
          <FormattedMessage id='TranslationInfoPanel.activityTitle'
            description={'Title for the Activity Panel'}
            defaultMessage='Activity' />
        </span>
      </span>
    )
    /* Activity Panel is open as default case, but not always visible.
     * eg: when entire info panel is hidden. */
    const activePanelKey =
      activityVisible ? activityTabKey
      : glossaryVisible ? glossaryTabKey
      : activityTabKey
    return (
      <div>
        <ConcurrentModal />
        <h1 className="SidebarEditor-heading">
          <Icon name="info" className="s1" parentClassName='details-svg' />
          <span className="hide-md">
            <FormattedMessage id='TranslationInfoPanel.detailsTitle'
              description={'Title for the Details Panel'}
              defaultMessage='Details' />
          </span>
          <span className="s1 u-pullRight">
            <Button bsStyle="link" onClick={this.props.close}>
              <Icon name="cross" />
            </Button>
          </span>
        </h1>
        <div className="SidebarEditor-wrapper">
          <DetailsPane
            // @ts-ignore
            hasSelectedPhrase={this.props.hasSelectedPhrase}
            selectedPhrase={this.props.selectedPhrase}
            isRTL={this.props.isRTL} />
        </div>
        <Tabs activeKey={activePanelKey}
          onSelect={this.handleSelectTab}
          id="SidebarEditor-tabsPane1">
          <Tab eventKey={activityTabKey} title={activityTitle}>
            <ActivityTab
              // @ts-ignore
              activeKey={this.state.key}
              transHistory={this.props.transHistory}
              selectedActivites={this.state.selectedActivites}
              selectActivityTypeFilter={this.selectActivityTypeFilter}
              postComment={this.postComment} />
          </Tab>
          <Tab eventKey={glossaryTabKey} title={glossaryTitle}>
            <GlossaryTab
              // @ts-ignore
              activeKey={this.state.key} />
          </Tab>
        </Tabs>
      </div>
    )
  }
}
function mapStateToProps (state) {
  const { glossary, phrases, context, activity } = state
  const { detail, selectedPhraseId, notification } = phrases
  const selectedPhrase = detail[selectedPhraseId]
  const { results, searchText } = glossary
  const glossaryResults = results.get(searchText)
  const glossaryCount = glossaryResults ? glossaryResults.length : 0
  const transHistory = activity.transHistory
  const transUnitId = state.phrases.selectedPhraseId
  const localeId = state.context.lang
  // Need to check whether phrase itself is undefined since the detail may not
  // yet have been fetched from the server.
  const hasSelectedPhrase = !isUndefined(selectedPhraseId) &&
      !isUndefined(selectedPhrase)
  const isRTL = context.sourceLocale.isRTL
  const glossaryVisible = getGlossaryVisible(state)
  const activityVisible = getActivityVisible(state)
  const newProps = {
    activityVisible,
    glossaryVisible,
    glossaryCount,
    hasSelectedPhrase,
    notification,
    transHistory,
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
    // @ts-ignore
    close: () => dispatch(setSidebarVisibility(false)),
    postReviewComment: (reviewData) =>
      dispatch(postReviewComment(dispatch, reviewData)),
    toggleActivity: () => dispatch(toggleActivity()),
    toggleGlossary: () => dispatch(toggleGlossary())
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(
  TranslationInfoPanel)
