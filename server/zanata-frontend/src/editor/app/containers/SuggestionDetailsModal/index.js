/**
 * Modal to display the details for a group of suggestion matches.
 */

import React, { Component, PropTypes } from 'react'
import { Modal } from 'zanata-ui'
import { PanelGroup } from 'react-bootstrap'
import LocalProjectDetailPanel from './LocalProjectDetailPanel'
import ImportedTMDetailPanel from './ImportedTMDetailPanel'
import PlainSuggestionContents from './PlainSuggestionContents'

class SuggestionDetailsModal extends Component {
  render () {
    const { matchDetails } = this.props.suggestion

    const detailPanels = matchDetails.map((matchDetail, index) => {
      const props = {
        matchDetail, key: index, eventKey: index
      }
      switch (matchDetail.type) {
        case 'LOCAL_PROJECT':
          return <LocalProjectDetailPanel {...props} />
        case 'IMPORTED_TM':
          return <ImportedTMDetailPanel {...props} />
        default:
          console.error('Unrecognised suggestion match type', matchDetail.type)
      }
    })

    return (
      <div>
        <Modal
          show
          onHide={this.props.onClose}>
          <Modal.Header>
            <Modal.Title><small><span className="pull-left">
            Translation Memory Details</span></small></Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <PlainSuggestionContents suggestion={this.props.suggestion} />
            <PanelGroup defaultActiveKey={0} accordion>
              {detailPanels}
            </PanelGroup>
          </Modal.Body>
        </Modal>
      </div>)
  }
}

SuggestionDetailsModal.propTypes = {
  onClose: PropTypes.func.isRequired,
  suggestion: PropTypes.shape({
    matchDetails: PropTypes.array.isRequired
  }).isRequired
}

export default SuggestionDetailsModal
