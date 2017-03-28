/**
 * Display for a single glossary term.
 */

import React, { PropTypes } from 'react'
import { Button, Tooltip, OverlayTrigger }
    from 'react-bootstrap'
import IconButton from '../IconButton'

// FIXME need a modal to open when this is clicked
const logDetailsClick = () => {
}

const GlossaryTerm = React.createClass({
  propTypes: {
    index: PropTypes.number.isRequired,
    copyGlossaryTerm: PropTypes.func.isRequired,
    term: PropTypes.shape({
      source: PropTypes.string.isRequired,
      target: PropTypes.string.isRequired
    }).isRequired
  },

  copy () {
    this.props.copyGlossaryTerm(this.props.term.target)
  },

  render () {
    const { index, term } = this.props
    const sourceTip = (
      <Tooltip id={'glossarytermsource-' + index}>
        term.source
      </Tooltip>
    )
    const targetTip = (
      <Tooltip id={'glossarytermtarget-' + index}>
        term.source
      </Tooltip>
    )

    return (
      <tr key={index}>
        <td data-filetype="text" className="gloss-text long-string">
          <OverlayTrigger placement="top" overlay={sourceTip}>
            <Button bStyle="link">
              <span>
                <span className="hide-mdplus u-textMeta">
                  Source
                </span>
                {term.source}
              </span>
            </Button>
          </OverlayTrigger>
        </td>
        <td data-filetype="text" className="gloss-text long-string">
          <OverlayTrigger placement="top" overlay={targetTip}>
            <Button bStyle="link">
              <span>
                <span className="hide-mdplus u-textMeta">
                  Target
                </span>
                {term.target}
              </span>
            </Button>
          </OverlayTrigger>
        </td>
        <td>
          <Button title="Copy" onClick={this.copy}
            className="Button Button--small u-rounded Button--primary">
            Copy
          </Button>
        </td>
        <td className="info-icon">
          <IconButton
            icon="info"
            title="Details"
            className="Button--link"
            onClick={logDetailsClick}
          />
        </td>
      </tr>
    )
  }
})

export default GlossaryTerm
