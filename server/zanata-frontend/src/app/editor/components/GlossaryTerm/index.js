/**
 * Display for a single glossary term.
 */

import React from 'react'
import * as PropTypes from 'prop-types'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Tooltip from 'antd/lib/tooltip'
import 'antd/lib/tooltip/style/css'
import { isEmpty } from 'lodash'
import cx from 'classnames'

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
      <span id={'glossarytermsource-' + index}>
        {term.source}
      </span>
    )
    const targetTip = (
      <span id={'glossarytermtarget-' + index}>
        {term.target}
      </span>
    )

    return (
      <tr key={index}>
        <td data-filetype="text" className="GlossaryText StringLong">
          <Tooltip placement="top" title={sourceTip}>
            <Button className="btn-link">
              <span>
                <span className="hide-mdplus u-textMeta">
                  Source
                </span>
                <span className={directionClassSource}>{term.source}</span>
              </span>
            </Button>
          </Tooltip>
        </td>
        <td data-filetype="text" className="GlossaryText StringLong">
          <Tooltip placement="top" title={targetTip}>
            <Button className="btn-link">
              <span className={
                cx({'u-textMuted': isEmpty(term.target)})}>
                <span className="hide-mdplus u-textMeta">
                  Translation
                </span>
                <span className={directionClassTarget}>
                  {isEmpty(term.target) ? '-none-' : term.target}
                </span>
              </span>
            </Button>
          </Tooltip>
        </td>
        <td
          title={isEmpty(term.target)
            ? 'No translation to copy.'
            : 'Insert translated term at the cursor position.'}>
          <Button type="primary"
            onClick={this.copy}
            disabled={isEmpty(term.target)}
            className="EditorButton Button--small u-rounded Button--primary">
            Copy
          </Button>
        </td>
        <td className="info-icon">
          <Button
            icon="info-circle-o"
            title="Details"
            className="Button--link s1 v-btm"
            onClick={this.showDetails}
          />
        </td>
      </tr>
    )
  }
}

export default GlossaryTerm
