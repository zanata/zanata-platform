import { chain } from 'lodash'
import cx from 'classnames'
import React, { PropTypes } from 'react'
import {
  STATUS_UNTRANSLATED,
  STATUS_NEEDS_WORK,
  STATUS_TRANSLATED,
  STATUS_APPROVED,
  STATUS_REJECTED
} from '../../utils/status'

const ProgressItem = React.createClass({
  propTypes: {
    state: PropTypes.string.isRequired,
    /* left position of this part of the bar (percentage of full bar width) */
    start: PropTypes.number.isRequired,
    /* width of this part of the bar (percentage of full bar width) */
    width: PropTypes.number.isRequired
  },
  render: function () {
    const className = cx('Progressbar-item', 'Progressbar-' + this.props.state)
    const style = {
      marginLeft: this.props.start + '%',
      width: this.props.width + '%'
    }
    return (
      <span className={className} style={style} />
    )
  }
})

/**
 * Bar showing translation progress
 */
const ProgressBar = React.createClass({

  propTypes: {
    size: PropTypes.string,
    counts: PropTypes.shape({
      // TODO better to derive total from the others rather than duplicate
      total: PropTypes.number,
      approved: PropTypes.number,
      translated: PropTypes.number,
      needswork: PropTypes.number,
      rejected: PropTypes.number,
      untranslated: PropTypes.number
    }).isRequired
  },

  getDefaultProps: () => {
    return {
      counts: {
        total: 0,
        approved: 0,
        translated: 0,
        needswork: 0,
        rejected: 0,
        untranslated: 0
      }
    }
  },

  render: function () {
    const className = cx('Progressbar', {
      'Progressbar--sm': this.props.size === 'small',
      'Progressbar--lg': this.props.size === 'large'
    })

    const { counts } = this.props

    const total = parseFloat(counts.total)
    const widths = chain(counts)
      .pick([STATUS_APPROVED, STATUS_TRANSLATED, STATUS_NEEDS_WORK,
        STATUS_REJECTED, STATUS_UNTRANSLATED])
      .mapValues((count) => {
        return count ? 100 * parseFloat(count) / total : 0
      })
      .value()

    var starts = {
      approved: 0,
      translated: widths.approved
    }
    starts.needswork = starts.translated + widths.translated
    starts.rejected = starts.needswork + widths.needswork
    starts.untranslated = starts.rejected + widths.rejected

    return (
      <div className={className}>
        <ProgressItem
          state="approved"
          start={starts.approved}
          width={widths.approved} />
        <ProgressItem
          state="translated"
          start={starts.translated}
          width={widths.translated} />
        <ProgressItem
          state="needswork"
          start={starts.needswork}
          width={widths.needswork} />
        <ProgressItem
          state="rejected"
          start={starts.rejected}
          width={widths.rejected} />
        <ProgressItem
          state="untranslated"
          start={starts.untranslated}
          width={widths.untranslated} />
      </div>
    )
  }
})

export default ProgressBar
