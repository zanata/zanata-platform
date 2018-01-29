/**
 * Modal to display the details for a group of suggestion matches.
 */

import * as React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import { Modal } from '../../../components'
import { PanelGroup } from 'react-bootstrap'
import LocalProjectDetailPanel from './LocalProjectDetailPanel'
import ImportedTMDetailPanel from './ImportedTMDetailPanel'
import PlainSuggestionContents from './PlainSuggestionContents'
import { matchType } from '../../utils/suggestion-util'

class SuggestionDetailsModal extends Component {
  propTypes = {
    onClose: PropTypes.func.isRequired,
    suggestion: PropTypes.shape({
      matchDetails: PropTypes.array.isRequired
    }).isRequired,
    isRTLSource: PropTypes.bool.isRequired,
    isRTLTarget: PropTypes.bool.isRequired
  }

  constructor (props) {
    super(props)
    this.state = {
      activeKey: 0
    }
  }

  render () {
    const {suggestion, isRTLSource, isRTLTarget} = this.props
    const {matchDetails} = suggestion

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

    const directionClassSource = isRTLSource ? 'rtl' : 'ltr'
    const directionClassTarget = isRTLTarget ? 'rtl' : 'ltr'

    return (
      <Modal
        show
        onHide={this.props.onClose}
        id="SuggestionDetailsModal">
        <Modal.Header>
          <Modal.Title><small><span className="u-pullLeft">
          Suggestion Details</span></small></Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <PlainSuggestionContents suggestion={this.props.suggestion}
            matchType={activeMatchType}
            directionClassSource={directionClassSource}
            directionClassTarget={directionClassTarget} displayHeader />
          <PanelGroup>
            {detailPanels}
          </PanelGroup>
        </Modal.Body>
      </Modal>
    )
  }
}

export default SuggestionDetailsModal
