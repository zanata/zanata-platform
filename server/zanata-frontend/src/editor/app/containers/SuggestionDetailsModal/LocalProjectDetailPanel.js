/**
 * A react-bootstrap Panel that displays details for a LOCAL_PROJECT suggestion
 * match.
 */

import React, { Component, PropTypes } from 'react'
import { Icon, Row } from 'zanata-ui'
import { Panel, ListGroup, ListGroupItem, Badge, Label } from 'react-bootstrap'
import SuggestionUpdateMessage from '../../components/SuggestionUpdateMessage'
import { matchType } from '../../utils/suggestion-util'

class LocalProjectDetailPanel extends Component {

  matchHeader (matchDetail) {
    const {
      documentName,
      documentPath,
      projectId,
      projectName,
      version } = matchDetail

    // FIXME example has document name shortened with an ellipsis in the middle
    //       when it is wider than the available space.
    const adjustedDocumentPath = documentPath || ''
    const fullDocName = adjustedDocumentPath + documentName

    // FIXME use standard styles when they are available
    // these values are based on variables in TransUnit/index.css but are
    // somehow transformed when the CSS is compiled, so values are taken from
    // the bundle file.
    const styleByMatchType = {
      // --TransUnit-color-success
      translated: {'backgroundColor': '#5cca7b'},
      // --TransUnit-color-highlight
      approved: {'backgroundColor': '#03a6d7'}
    }

    const labelTextByMatchType = {
      translated: 'Translated',
      approved: 'Approved'
    }

    const currentMatchType = matchType(matchDetail)

    return (
      <div className="TransUnit-details">
        <ul className="u-listInline u-sMB-1-4">
          <li title={projectId}>
            <Row>
              <Icon name="project" size="n1" /> {projectName}
            </Row>
          </li>
          <li>
            <Row>
              <Icon name="version" size="n1" /> {version}
            </Row>
          </li>
          <li title={fullDocName}>
            <Row>
              <Icon name="document"
                size="n1" /> {fullDocName}
            </Row>
          </li>
          <Label style={styleByMatchType[currentMatchType]}>
            {labelTextByMatchType[currentMatchType]}</Label>
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
    const {
      lastModifiedBy,
      lastModifiedDate,
      sourceComment,
      targetComment } = matchDetail

    const header = this.matchHeader(matchDetail)
    const lastChanged = new Date(lastModifiedDate)
    const currentMatchType = matchType(matchDetail)

    return (
      <Panel
        header={header}
        {...props}
        bsStyle="info"
      >
        {/* this stuff is really similar to the header, but includes a
         non-truncated document id display. Just using the same header for
         now as a shortcut. */}
        {header}
        <ListGroup>
          <ListGroupItem className="small" header="Source">
            <h3>{source}</h3>
            <ListGroupItem className="comment-box"><h4>Comments
              &nbsp;<Badge>{sourceComment ? 1 : 0}</Badge></h4>
              {sourceComment}
            </ListGroupItem>
          </ListGroupItem>
          <ListGroupItem className="small" header="Target">
            <h3>{target}{/* <Label bsStyle="success">Translated
            </Label>*/}</h3>
            <SuggestionUpdateMessage
              user={lastModifiedBy || 'Anonymous'}
              lastChanged={lastChanged}
              matchType={currentMatchType}
            />
            <ListGroupItem className="comment-box">
              <h4>Comments&nbsp;<Badge>{targetComment ? 1 : 0}</Badge></h4>
              {targetComment}
            </ListGroupItem>
          </ListGroupItem>
        </ListGroup>
      </Panel>
    )
  }
}

LocalProjectDetailPanel.propTypes = {
  /* Key of this panel within the group, used to expand/collapse */
  eventKey: PropTypes.number.isRequired,
  /* Source text for the match */
  source: PropTypes.string.isRequired,
  /* Translated text for the match */
  target: PropTypes.string.isRequired,
  /* Detailed descriptive and context information for the match. */
  matchDetail: PropTypes.shape({
    // This only renders things from local projects
    type: PropTypes.oneOf(['LOCAL_PROJECT']).isRequired,
    sourceComment: PropTypes.string,
    targetComment: PropTypes.string,
    projectId: PropTypes.string.isRequired,
    projectName: PropTypes.string.isRequired,
    version: PropTypes.string.isRequired,
    lastModifiedBy: PropTypes.string,
    lastModifiedDate: PropTypes.string.isRequired
  }).isRequired
}

export default LocalProjectDetailPanel
