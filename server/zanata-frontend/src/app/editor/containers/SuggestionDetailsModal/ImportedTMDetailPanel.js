// @ts-nocheck

import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import { Icon } from '../../../components'
import Card from 'antd/lib/card'
import 'antd/lib/card/style/css'
import Tag from 'antd/lib/tag'
import 'antd/lib/tag/style/css'
import Row from 'antd/lib/row'
import 'antd/lib/row/style/css'
import SuggestionUpdateMessage from '../../components/SuggestionUpdateMessage'
import { MATCH_TYPE } from '../../utils/suggestion-util'

class ImportedTMDetailPanel extends Component {

  matchHeader (transMemorySlug) {
    // FIXME use standard styles when they are available, instead of hard-coded
    return (
      <div title={transMemorySlug} className="TransUnit-details ellipsis">
        <Icon name="import" className="s1" /> {transMemorySlug}
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
      <Card
        title={header}
        {...props}>
        <div className="TransUnit-details">
          <ul className="u-listInline u-sMB-1-4">
            <li>
              <Row>
                <Icon name="import" className="s1" />
                <span className="TransUnit-details-inner">{transMemorySlug}
                </span>
              </Row>
            </li>
            <Tag color="blue">Imported</Tag>
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
      </Card>
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
