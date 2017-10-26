// TODO damason split this into separate components for each tab and panel
import React from 'react'
import PropTypes from 'prop-types'
import { setSidebarVisibility } from '../../actions'
import { Tabs, FormGroup, InputGroup,
  FormControl, Button } from 'react-bootstrap'
import Icon from '../../../components/Icon'
import { connect } from 'react-redux'
import { isEmpty, isUndefined } from 'lodash'
import { FormattedDate, FormattedTime } from 'react-intl'
import GlossaryTab from '../GlossaryTab'
// Use this when the activity tab is activated
// import ActivityTab from './ActivityTab'

/* Panel displaying info, glossary, activity, etc. */
class TranslationInfoPanel extends React.Component {
  static propTypes = {
    /* close the sidebar */
    close: PropTypes.func.isRequired,
    glossaryCount: PropTypes.number.isRequired,
    hasSelectedPhrase: PropTypes.bool.isRequired,
    selectedPhrase: PropTypes.shape({
      msgctxt: PropTypes.string,
      resId: PropTypes.string.isRequired,
      sourceComment: PropTypes.string,
      sourceFlags: PropTypes.string,
      sourceReferences: PropTypes.string,
      lastModifiedBy: PropTypes.string,
      lastModifiedTime: PropTypes.instanceOf(Date)
    }),
    isRTL: PropTypes.bool.isRequired
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

  render () {
    const { glossaryCount } = this.props
    const glossaryCountDisplay = glossaryCount > 0
      // TODO kgough display as a badge instead of text in parens
      ? <span>({this.props.glossaryCount})</span>
      : undefined
    const glossaryTitle = (
      <span>
        <Icon name="glossary" className="s1 gloss-tab-svg" />
        <span className="hide-md">Glossary{glossaryCountDisplay}</span>
      </span>
    )

    // Use this when activity tab is activated
    // const activityTitle = (
    //   <span>
    //     <Icon name="clock" className="s1 gloss-tab-svg" />
    //     <span className="hide-md">Activity</span>
    //   </span>
    // )

    return (
      <div>
        <h1 className="SidebarEditor-heading">
          <Icon name="info" className="details-svg s1" />
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
        <Tabs id="sidebartabs" defaultActiveKey={1}>
          { /* <Tab eventKey={2} title={activityTitle}>
            <div className="sidebar-wrapper" id="tab1">
              Tab 1 content
            </div>
          </Tab> */ }
          <GlossaryTab eventKey={1} title={glossaryTitle} />
          {/* Use this when activity tab is activated
            <ActivityTab eventKey={2} title={activityTitle} /> */}

        </Tabs>
      </div>
    )
  }
}

function mapStateToProps (state) {
  const { glossary, phrases, context } = state
  const { detail, selectedPhraseId } = phrases
  const selectedPhrase = detail[selectedPhraseId]

  const { results, searchText } = glossary
  const glossaryResults = results.get(searchText)
  const glossaryCount = glossaryResults ? glossaryResults.length : 0

  // Need to check whether phrase itself is undefined since the detail may not
  // yet have been fetched from the server.
  const hasSelectedPhrase = !isUndefined(selectedPhraseId) &&
      !isUndefined(selectedPhrase)

  const isRTL = context.sourceLocale.isRTL

  const newProps = {
    glossaryCount,
    hasSelectedPhrase,
    isRTL
  }

  if (hasSelectedPhrase) {
    newProps.selectedPhrase = selectedPhrase
  }

  return newProps
}

function mapDispatchToProps (dispatch) {
  return {
    close: () => dispatch(setSidebarVisibility(false))
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(
  TranslationInfoPanel)
