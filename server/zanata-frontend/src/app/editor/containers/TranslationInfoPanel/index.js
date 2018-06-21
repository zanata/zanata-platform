import React from 'react'
import * as PropTypes from 'prop-types'
import { setSidebarVisibility } from '../../actions'
import { toggleGlossary, toggleActivity } from '../../actions/header-actions'
import { getGlossaryVisible, getActivityVisible } from '../../reducers'
import { postReviewComment } from '../../actions/review-trans-actions'
import Icon from '../../../components/Icon'
import { connect } from 'react-redux'
import { FormattedMessage } from 'react-intl'
import { isUndefined } from 'lodash'
import GlossaryTab from '../GlossaryTab'
import ActivityTab from '../ActivityTab'
import DetailsPane from './DetailsPane'
import Input from 'antd/lib/input'
import 'antd/lib/input/style/css'
import Tabs from 'antd/lib/tabs'
import 'antd/lib/tabs/style/css'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Tag from 'antd/lib/tag'
import 'antd/lib/tag/style/css'
import Notification from 'antd/lib/notification'
import 'antd/lib/notification/style/css'

/* Tab keys for tracking active Tab */
const activityTabKey = '1'
const glossaryTabKey = '2'
const { TextArea } = Input
const TabPane = Tabs.TabPane

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
      description: PropTypes.any,
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
      <span className="trans-link">
        <span>
          <Input addonAfter={
            <Icon name="copy"
              className="s1" />}
          />
          <TextArea />
        </span>
      </span>
    )
  }
  render () {
    const { activityVisible, glossaryVisible, glossaryCount } = this.props
    const glossaryCountDisplay = glossaryCount > 0
      ? <Tag color="#20718A">{this.props.glossaryCount}</Tag>
      : undefined
    /* Activity Panel is open as default case, but not always visible.
     * eg: when entire info panel is hidden. */
    const activePanelKey =
      activityVisible ? activityTabKey
      : glossaryVisible ? glossaryTabKey
      : activityTabKey
    return (
      <div>
        <h1 className="SidebarEditor-heading">
          <Icon name="info" className="s1" parentClassName='details-svg' />
          <span className="hide-md">
            <FormattedMessage id='TranslationInfoPanel.detailsTitle'
              description={'Title for the Details Panel'}
              defaultMessage='Details' />
          </span>
          <span className="u-pullRight s1">
            <Button className="btn-link transparent" onClick={this.props.close}>
              <Icon name='cross' />
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

        <span id="SidebarEditor-tabsPane1">
          <Tabs defaultActiveKey={activePanelKey}
            onTabClick={this.handleSelectTab}>
            <TabPane key={activityTabKey} tab={
              <span>
                <Icon name="clock" className="s1 gloss-tab-svg" />
                <span className="hide-md">
                  <FormattedMessage id='TranslationInfoPanel.activityTitle'
                    description={'Title for the Activity Panel'}
                    defaultMessage='Activity' />
                </span>
              </span>
            }>
              <ActivityTab
                // @ts-ignore
                activeKey={this.state.key}
                transHistory={this.props.transHistory}
                selectedActivites={this.state.selectedActivites}
                selectActivityTypeFilter={this.selectActivityTypeFilter}
                postComment={this.postComment} />
            </TabPane>
            <TabPane key={glossaryTabKey} tab={
              <span><Icon name="glossary" className="s1 gloss-tab-svg" />
                <span className="hide-md mr2">
                  <FormattedMessage id='TranslationInfoPanel.glossaryTitle'
                    description={'Title for the Glossary Panel'}
                    defaultMessage='Glossary' />
                </span>{glossaryCountDisplay}
              </span>
              }>
              <GlossaryTab
                // @ts-ignore
                activeKey={this.state.key} />
            </TabPane>
          </Tabs>
        </span>
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
