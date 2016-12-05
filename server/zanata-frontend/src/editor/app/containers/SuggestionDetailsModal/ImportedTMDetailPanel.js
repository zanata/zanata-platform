/**
 * A react-bootstrap Panel that displays details for an IMPORTED_TM suggestion
 * match.
 */

import React, { Component, PropTypes } from 'react'
import { Icon, Row } from 'zanata-ui'
import { Panel, ListGroup, ListGroupItem, Label } from 'react-bootstrap'
import SuggestionUpdateMessage from '../../components/SuggestionUpdateMessage'

class ImportedTMDetailPanel extends Component {

  matchHeader (matchDetail) {
    const { transMemorySlug } = matchDetail

    // FIXME use standard styles when they are available, instead of hard-coded

    return (
      <div className="TransUnit-details">
        <ul className="u-listInline u-sMB-1-4">
          <li>
            <Row>
              <Icon name="import" size="n1" /> {transMemorySlug}
            </Row>
          </li>
          <Label style={{'backgroundColor': '#20718a'}}>Imported</Label>
        </ul>
      </div>
    )
  }

  render () {
    const {
      source,
      target,
      matchDetail,
      /* The collected ...props are used to make sure the returned Panel will
       * work properly with the enclosing PanelGroup/Accordion. If the props
       * are not passed through, panels do not work properly in a group. */
      ...props } = this.props
    const { lastChanged, transUnitId } = matchDetail
    const header = this.matchHeader(matchDetail)

    const lastChangedDate = new Date(lastChanged)

    return (
      <Panel
        header={header}
        {...props}
        bsStyle="info">
        {/* this stuff is really similar to the header, but includes a
         non-truncated document id display. Just using the same header for
         now as a shortcut. */}
        {header}
        <ListGroup>
          <ListGroupItem className="small">
            <h4 className="list-group-item-heading">Source</h4>
            <h3>{source}</h3>
          </ListGroupItem>
          <ListGroupItem className="small">
            <h4 className="list-group-item-heading">Target</h4>
            <h3>{target}</h3>
            <SuggestionUpdateMessage
              lastChanged={lastChangedDate}
              matchType={'imported'}
            />
          </ListGroupItem>
          <ListGroupItem className="small">
            <h4 className="list-group-item-heading">Properties</h4>
            <ul>
              <li>Id: {transUnitId}</li>
            </ul>
          </ListGroupItem>
        </ListGroup>
      </Panel>
    )
  }
}

ImportedTMDetailPanel.propTypes = {
  /* Key of this panel within the group, used to expand/collapse */
  eventKey: PropTypes.number.isRequired,
  /* Source text for the match */
  source: PropTypes.string.isRequired,
  /* Translated text for the match */
  target: PropTypes.string.isRequired,
  /* Detailed descriptive and context information for the match. */
  matchDetail: PropTypes.shape({
    // This only renders things from local projects
    type: PropTypes.oneOf(['IMPORTED_TM']).isRequired,
    transMemorySlug: PropTypes.string.isRequired,
    lastChanged: PropTypes.string.isRequired,
    transUnitId: PropTypes.string.isRequired
  }).isRequired
}

export default ImportedTMDetailPanel
