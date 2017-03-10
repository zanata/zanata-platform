/**
 * A react-bootstrap Panel that displays details for a LOCAL_PROJECT suggestion
 * match.
 */

import React, { Component, PropTypes } from 'react'
import { Icon, Row } from 'zanata-ui'
import { Panel, Label } from 'react-bootstrap'
import SuggestionUpdateMessage from '../../components/SuggestionUpdateMessage'
import { matchType, MATCH_TYPE } from '../../utils/suggestion-util'

class LocalProjectDetailPanel extends Component {

  fullDocName (path, name) {
    return (path || '') + name
  }

  matchHeader (matchDetail) {
    const { documentName, documentPath } = matchDetail
    const fullDocName = this.fullDocName(documentPath, documentName)
    return (
      <div title={fullDocName} className="TransUnit-details ellipsis">
        <Icon name="document"
          size="1"
          theme={{
            base: {
              px: 'Px(rh)'
            }
          }} /> {fullDocName}
      </div>
    )
  }

  stateLabel (matchDetail) {
    switch (matchType(matchDetail)) {
      case MATCH_TYPE.TRANSLATED:
        return <Label bsStyle="success">Translated</Label>
      case MATCH_TYPE.APPROVED:
        return <Label bsStyle="info">Approved</Label>
      default:
        console.error('unrecognised match type')
    }
  }

  matchProperties (matchDetail) {
    const {
      documentName,
      documentPath,
      projectId,
      projectName,
      version } = matchDetail

    // FIXME example has document name shortened with an ellipsis in the middle
    //       when it is wider than the available space.
    const fullDocName = this.fullDocName(documentPath, documentName)

    return (
      <div className="TransUnit-details">
        <ul className="u-sMB-1-4">
          <li className="TransUnit-label-suggestions" title={projectId}>
            <Row>
              <Icon name="project" size="1" />PROJECT:
              <span className="TransUnit-details-inner">{projectName}</span>
            </Row>
          </li>
          <li className="TransUnit-label-suggestions">
            <Row>
              <Icon name="version" size="1" />VERSION:
              <span className="TransUnit-details-inner">{version}</span>
            </Row>
          </li>
        </ul>
        <ul className="u-listInline u-sMB-1-4">
          <li title={fullDocName} className="TransUnit-label-suggestions">
            <Row>
              <Icon name="document" size="1" />DOCUMENT:
              <span className="TransUnit-details-inner">{fullDocName}</span>
            </Row>
          </li>
          <li>
            {this.stateLabel(matchDetail)}
          </li>
        </ul>
      </div>
    )
  }

  buildCommentBox ({ sourceComment, targetComment }) {
    // no comment box when there are no comments
    if (!sourceComment && !targetComment) {
      return false
    }
    return [
      <hr key="0" />,
      <span key="1" className="comment-box">
        <h4 className="list-group-item-heading">Comments</h4>
        <ul className="list-inline">
          <li><Icon name="comment" title="comment" /></li>
          <li>Source</li>
        </ul>
        {sourceComment}
        <ul className="list-inline">
          <li><Icon name="comment" title="comment" /></li>
          <li>Target</li>
        </ul>
        {targetComment}
      </span>
    ]
  }

  render () {
    const {
      matchDetail,
      /* The collected ...props are used to make sure the returned Panel will
       * work properly with the enclosing PanelGroup/Accordion. If the props
       * are not passed through, panels do not work properly in a group. */
      ...props } = this.props
    const {
      lastModifiedBy,
      lastModifiedDate } = matchDetail

    const lastChanged = new Date(lastModifiedDate)
    const currentMatchType = matchType(matchDetail)

    return (
      <Panel
        header={this.matchHeader(matchDetail)}
        {...props}
        bsStyle="info"
      >
        {this.matchProperties(matchDetail)}
        <SuggestionUpdateMessage
          user={lastModifiedBy || 'Anonymous'}
          lastChanged={lastChanged}
          matchType={currentMatchType}
            />
        {this.buildCommentBox(matchDetail)}
      </Panel>
    )
  }
}

LocalProjectDetailPanel.propTypes = {
  /* Key of this panel within the group, used to expand/collapse */
  eventKey: PropTypes.number.isRequired,
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
