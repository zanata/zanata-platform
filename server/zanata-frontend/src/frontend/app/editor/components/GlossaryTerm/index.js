/**
 * Display for a single glossary term.
 */

import React from 'react'
import PropTypes from 'prop-types'
import { Button, Tooltip, OverlayTrigger } from 'react-bootstrap'
import IconButton from '../IconButton'
import { isEmpty } from 'lodash'

class GlossaryTerm extends React.Component {
  static propTypes = {
    index: PropTypes.number.isRequired,
    copyGlossaryTerm: PropTypes.func.isRequired,
    showDetails: PropTypes.func.isRequired,
    term: PropTypes.shape({
      source: PropTypes.string.isRequired,
      target: PropTypes.string.isRequired
    }).isRequired,
    directionClassSource: PropTypes.string,
    directionClassTarget: PropTypes.string
  }

  copy = () => {
    this.props.copyGlossaryTerm(this.props.term.target)
  }

  showDetails = () => {
    this.props.showDetails(this.props.index)
  }

  render () {
    const {index, term, directionClassSource, directionClassTarget} = this.props
    const sourceTip = (
      <Tooltip id={'glossarytermsource-' + index}>
        {term.source}
      </Tooltip>
    )
    const targetTip = (
      <Tooltip id={'glossarytermtarget-' + index}>
        {term.target}
      </Tooltip>
    )

    return (
      <tr key={index}>
        <td data-filetype="text" className="GlossaryText long-string">
          <OverlayTrigger placement="top" overlay={sourceTip}>
            <Button bsStyle="link">
              <span>
                <span className="hide-mdplus u-textMeta">
                  Source
                </span>
                <span className={directionClassSource}>{term.source}</span>
              </span>
            </Button>
          </OverlayTrigger>
        </td>
        <td data-filetype="text" className="GlossaryText long-string">
          <OverlayTrigger placement="top" overlay={targetTip}>
            <Button bsStyle="link">
              <span>
                <span className="hide-mdplus u-textMeta">
                  Target
                </span>
                <span className={directionClassTarget}>{term.target}</span>
              </span>
            </Button>
          </OverlayTrigger>
        </td>
        <td
          title={isEmpty(term.target)
            ? 'No translation to copy.'
            : 'Insert translated term at the cursor position.'}>
          <Button
            onClick={this.copy}
            disabled={isEmpty(term.target)}
            className="EditorButton Button--small u-rounded Button--primary">
            Copy
          </Button>
        </td>
        <td className="info-icon">
          <IconButton
            icon="info"
            title="Details"
            className="Button--link s1"
            onClick={this.showDetails}
          />
        </td>
      </tr>
    )
  }
}

export default GlossaryTerm
