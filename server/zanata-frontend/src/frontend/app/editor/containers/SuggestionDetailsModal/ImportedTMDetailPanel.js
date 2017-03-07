/**
 * A react-bootstrap Panel that displays details for an IMPORTED_TM suggestion
 * match.
 */

import React, { Component, PropTypes } from 'react'
import { Icon, Row } from 'zanata-ui'
import { Panel, Label } from 'react-bootstrap'
import SuggestionUpdateMessage from '../../components/SuggestionUpdateMessage'
import { MATCH_TYPE } from '../../utils/suggestion-util'

class ImportedTMDetailPanel extends Component {

  matchHeader (transMemorySlug) {
    // FIXME use standard styles when they are available, instead of hard-coded
    return (
      <div title={transMemorySlug} className="TransUnit-details ellipsis">
        <Icon name="import"
          size="1" /> {transMemorySlug}
      </div>
    )
  }

  render () {
    const {
      matchDetail,
      /* The collected ...props are used to make sure the returned Panel will
       * work properly with the enclosing PanelGroup/Accordion. If the props
       * are not passed through, panels do not work properly in a group. */
      ...props } = this.props
    const { lastChanged, transUnitId, transMemorySlug } = matchDetail
    const header = this.matchHeader(transMemorySlug)

    const lastChangedDate = new Date(lastChanged)

    return (
      <Panel
        header={header}
        {...props}
        bsStyle="info">
        <div className="TransUnit-details">
          <ul className="u-listInline u-sMB-1-4">
            <li>
              <Row>
                <Icon name="import" size="1" />
                <span className="TransUnit-details-inner">{transMemorySlug}
                </span>
              </Row>
            </li>
            <Label bsStyle="primary">Imported</Label>
          </ul>
        </div>
        <ul>
          <li>
            <SuggestionUpdateMessage
              lastChanged={lastChangedDate}
              matchType={MATCH_TYPE.IMPORTED}
            />
          </li>
          <br />
          <li className="small">
            <h4 className="list-group-item-heading">Properties</h4>
            <ul>
              <li>Id: {transUnitId}</li>
            </ul>
          </li>
        </ul>
      </Panel>
    )
  }
}

ImportedTMDetailPanel.propTypes = {
  /* Key of this panel within the group, used to expand/collapse */
  eventKey: PropTypes.number.isRequired,
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
