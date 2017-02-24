import React, { PropTypes } from 'react'
import { Tabs, Tab, FormGroup, InputGroup,
  FormControl } from 'react-bootstrap'
import Icon from '../../../frontend/app/components/Icon'
import { connect } from 'react-redux'
import { isEmpty, isUndefined } from 'lodash'

const activityTitle = 'Activity'
const glossaryTitle = 'Glossary'

// FIXME what is the following comment for?
// https://dmfrancisco.github.io/react-icons/

const SidebarContent = React.createClass({

  propTypes: {
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
    // FIXME kgough make this look good.
    const valueDisplay = isEmpty(value)
        ? <span style={{color: 'red'}}>No content</span>
        : <span style={{color: 'magenta'}}>{value}</span>
    return (
      <li>
        <span>{label}</span> {valueDisplay}
      </li>
    )
  },

  // FIXME kgough include icons, format date appropriately
  lastModifiedDisplay (lastModifiedBy, lastModifiedTime) {
    if (isUndefined(lastModifiedBy) && isUndefined(lastModifiedTime)) {
      return undefined
    }

    const modifiedByDisplay = isUndefined(lastModifiedBy) ? undefined
        : '(personIcon) ' + lastModifiedBy
    const modifiedTimeDisplay = isUndefined(lastModifiedTime) ? undefined
        : '(clockIcon)' + lastModifiedTime
    return <span>{modifiedByDisplay} {modifiedTimeDisplay}</span>
  },

  render () {
    return (
      <div>
        <h1 className="sidebar-heading">
          <Icon name="info" className="s1" /> Details
        </h1>
        <div className="sidebar-wrapper">
          {this.sidebarDetails()}
          <FormGroup className="trans-link">
            <InputGroup>
              <InputGroup.Addon><Icon name="copy"
                className="s1" />
              </InputGroup.Addon>
              <FormControl type="text" />
            </InputGroup>
          </FormGroup>
        </div>
        <Tabs id="sidebartabs" defaultActiveKey={1}>
          <Tab eventKey={1} title={activityTitle}>
            <div className="sidebar-wrapper" id="tab1">
              Tab 1 content
            </div>
          </Tab>
          <Tab eventKey={2} title={glossaryTitle}>
            <div className="sidebar-wrapper" id="tab2">
              Tab 2 content
            </div>
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
