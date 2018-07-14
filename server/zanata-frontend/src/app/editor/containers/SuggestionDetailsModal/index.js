// @ts-nocheck
/**
 * Modal to display the details for a group of suggestion matches.
 */

import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import LocalProjectDetailPanel from './LocalProjectDetailPanel'
import ImportedTMDetailPanel from './ImportedTMDetailPanel'
import PlainSuggestionContents from './PlainSuggestionContents'
import { matchType } from '../../utils/suggestion-util'
import Modal from 'antd/lib/modal'
import 'antd/lib/modal/style/css'
import Card from 'antd/lib/card'
import 'antd/lib/card/style/css'

class SuggestionDetailsModal extends Component {
  static propTypes = {
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
        case 'LOCAL_PROJECT': case 'MT':
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
        visible
        width={'90%'}
        bodyStyle={{padding: 0, wordWrap: 'normal'}}
        title='Suggestion Details'
        onCancel={this.props.onClose}
        id='SuggestionDetailsModal'
        footer={null}>
        <PlainSuggestionContents suggestion={this.props.suggestion}
          matchType={activeMatchType}
          directionClassSource={directionClassSource}
          directionClassTarget={directionClassTarget} displayHeader />
        <Card>
          {detailPanels}
        </Card>
      </Modal>
    )
  }
}

export default SuggestionDetailsModal
