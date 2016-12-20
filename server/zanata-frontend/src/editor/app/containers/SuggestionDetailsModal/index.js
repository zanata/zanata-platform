/**
 * Modal to display the details for a group of suggestion matches.
 */

import React, { Component, PropTypes } from 'react'
import { Modal } from 'zanata-ui'
import { PanelGroup } from 'react-bootstrap'
import LocalProjectDetailPanel from './LocalProjectDetailPanel'
import ImportedTMDetailPanel from './ImportedTMDetailPanel'
import PlainSuggestionContents from './PlainSuggestionContents'
import { matchType } from '../../utils/suggestion-util'

class SuggestionDetailsModal extends Component {
  constructor (props) {
    super(props)
    this.state = {
      activeKey: 0
    }
  }

  /**
   * One of the detail panels is selected.
   */
  handleSelect (activeKey) {
    this.setState({activeKey})
  }

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

    const activeMatchType = matchType(matchDetails[this.state.activeKey])

    return (
      <Modal
        show
        onHide={this.props.onClose}>
        <Modal.Header>
          <Modal.Title><small><span className="pull-left">
          Translation Memory Details</span></small></Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <PlainSuggestionContents suggestion={this.props.suggestion}
            matchType={activeMatchType} />
          <PanelGroup accordion
            activeKey={this.state.activeKey}
            onSelect={::this.handleSelect}>
            {detailPanels}
          </PanelGroup>
        </Modal.Body>
      </Modal>
    )
  }
}

SuggestionDetailsModal.propTypes = {
  onClose: PropTypes.func.isRequired,
  suggestion: PropTypes.shape({
    matchDetails: PropTypes.array.isRequired
  }).isRequired
}

export default SuggestionDetailsModal
