import React, { PropTypes } from 'react'
import { Tabs, Tab, FormGroup, InputGroup,
  FormControl, Button, Table } from 'react-bootstrap'
import Icon from '../../components/Icon'
import { connect } from 'react-redux'
import { isEmpty, isUndefined } from 'lodash'
import { FormattedDate, FormattedTime } from 'react-intl'
import GlossarySearchInput from '../components/GlossarySearchInput'
import IconButton from '../components/IconButton'

// FIXME extract component for glossary
// FIXME use real data
const dummyData = [
  {
    source: 'dog',
    target: 'Hund'
  },
  {
    source: 'sausage',
    target: 'Wurst'
  },
  {
    source: 'incomprehensibilities',
    target: 'Unverständlichkeiten'
  },
  {
    source: 'tree',
    target: 'Baum'
  },
  {
    source: 'head district chimney sweep',
    target: 'Bezirksschornsteinfegermeister'
  },
  {
    source: 'German',
    target: 'Deutsche'
  }
]
const logDetailsClick = () => {
  console.log('Details button clicked...?')
}

/* var activityTitle = <span>
  <Icon name="clock" className="s1 act-tab-svg" />
  <span className="hide-md">Activity</span>
</span> */
var glossaryTitle = <span>
  <Icon name="glossary" className="s1 gloss-tab-svg" />
  <span className="hide-md">Glossary</span>
</span>

const SidebarContent = React.createClass({

  propTypes: {
    /* close the sidebar */
    close: PropTypes.func.isRequired,
    hasSelectedPhrase: PropTypes.bool.isRequired,
    selectedPhrase: PropTypes.shape({
      msgctxt: PropTypes.string,
      resId: PropTypes.string.isRequired,
      sourceComment: PropTypes.string,
      sourceFlags: PropTypes.string,
      sourceReferences: PropTypes.string,
      lastModifiedBy: PropTypes.string,
      lastModifiedTime: PropTypes.date
    })
  },

  sidebarDetails () {
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

    return (
      <ul className="sidebar-details">
        {this.detailItem('Resource ID', resId)}
        {this.detailItem('Message Context', msgctxt)}
        {this.detailItem('Reference', sourceReferences)}
        {this.detailItem('Flags', sourceFlags)}
        {this.detailItem('Source Comment', sourceComment)}
        {this.detailItem('Last Modified',
            this.lastModifiedDisplay(lastModifiedBy, lastModifiedTime))}
      </ul>
    )
  },

  detailItem (label, value) {
    const valueDisplay = isEmpty(value)
        ? <span className="details-nocontent">No content</span>
        : <span className="details-content">{value}</span>
    return (
      <li>
        <span>{label}</span> {valueDisplay}
      </li>
    )
  },

  lastModifiedDisplay (lastModifiedBy, lastModifiedTime) {
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
  },

  /* URL of the selected phrase, with copy button. */
  phraseLink () {
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
  },

  render () {
    const terms = dummyData

    const termsDisplay = terms.map((term, index) => {
      return (
        <tr key={index}>
          <td className="bold-text">{term.source}</td>
          <td className="bold-text">{term.target}</td>
          <td>
            <Button title="copy"
              className="Button Button--small u-rounded Button--primary">
              Copy
            </Button>
          </td>
          <td className="align-center">
            <IconButton
              icon="info"
              title="Details"
              className="Button--link"
              onClick={logDetailsClick}
            />
          </td>
        </tr>
      )
    })

    return (
      <div>
        <h1 className="sidebar-heading">
          <Icon name="info" className="s1" /> Details
          <span className="s1 pull-right">
            <Button bsStyle="link" onClick={this.props.close}>
              <Icon name="cross" />
            </Button>
          </span>
        </h1>
        <div className="sidebar-wrapper">
          {this.sidebarDetails()}
        </div>
        <Tabs id="sidebartabs" defaultActiveKey={1}>
          { /* <Tab eventKey={2} title={activityTitle}>
            <div className="sidebar-wrapper" id="tab1">
              Tab 1 content
            </div>
          </Tab> */ }
          <Tab eventKey={1} title={glossaryTitle}>
            <div className="sidebar-wrapper" id="tab2">
              <GlossarySearchInput />
            </div>
            <Table reponsive>
              <thead>
                <tr>
                  <th>Source term</th>
                  <th>Target term</th>
                  <th>
                  </th>
                  <th className="align-center">Details</th>
                </tr>
              </thead>
              <tbody>
                {termsDisplay}
              </tbody>
            </Table>
          </Tab>
        </Tabs>
      </div>
    )
  }
})

function mapStateToProps (state) {
  const { detail, selectedPhraseId } = state.phrases
  const selectedPhrase = detail[selectedPhraseId]

  // Need to check whether phrase itself is undefined since the detail may not
  // yet have been fetched from the server.
  const hasSelectedPhrase = !isUndefined(selectedPhraseId) &&
      !isUndefined(selectedPhrase)
  if (hasSelectedPhrase) {
    return {
      hasSelectedPhrase,
      selectedPhrase
    }
  } else {
    return {
      hasSelectedPhrase
    }
  }
}

function mapDispatchToProps (dispatch) {
  return {

  }
}

export default connect(mapStateToProps, mapDispatchToProps)(SidebarContent)
