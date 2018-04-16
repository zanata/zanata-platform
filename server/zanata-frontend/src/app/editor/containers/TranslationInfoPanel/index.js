import React from 'react'
import * as PropTypes from 'prop-types'
import { setSidebarVisibility } from '../../actions'
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

const historyShape = PropTypes.shape({
  contents: PropTypes.arrayOf(PropTypes.string),
  modifiedBy: PropTypes.string,
  modifiedDate: PropTypes.number,
  optionalTag: PropTypes.string,
  revisionComment: PropTypes.string,
  status: PropTypes.string,
  versionNum: PropTypes.string
})

/* Panel displaying info, glossary, activity, etc. */
class TranslationInfoPanel extends React.Component {
  static propTypes = {
    /* close the sidebar */
    close: PropTypes.func.isRequired,
    glossaryCount: PropTypes.number.isRequired,
    hasSelectedPhrase: PropTypes.bool.isRequired,
    localeId: PropTypes.string.isRequired,
    transUnitId: PropTypes.number,
    postReviewComment: PropTypes.func.isRequired,
    transHistory: PropTypes.shape({
      historyItems: PropTypes.arrayOf(historyShape),
      reviewComments: PropTypes.arrayOf(
        PropTypes.shape({
          comment: PropTypes.string,
          commenterName: PropTypes.string,
          creationDate: PropTypes.number,
          id: PropTypes.shape({id: PropTypes.number, value: PropTypes.number})
        })
      ),
      latestHistoryItem: historyShape
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
    const { glossaryCount } = this.props
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
    return (
      <div>
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
        <Tabs activeKey={this.state.key}
          onSelect={this.handleSelectTab}
          id="SidebarEditor-tabsPane1">
          <Tab eventKey={1} title={activityTitle}>
            <ActivityTab
              // @ts-ignore
              activeKey={this.state.key}
              transHistory={this.props.transHistory}
              selectedActivites={this.state.selectedActivites}
              selectActivityTypeFilter={this.selectActivityTypeFilter}
              postComment={this.postComment} />
          </Tab>
          <Tab eventKey={2} title={glossaryTitle}>
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
  const { detail, selectedPhraseId } = phrases
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
  const newProps = {
    glossaryCount,
    hasSelectedPhrase,
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
      dispatch(postReviewComment(dispatch, reviewData))
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(
  TranslationInfoPanel)
