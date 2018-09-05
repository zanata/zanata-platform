import { chain } from 'lodash'
import cx from 'classnames'
import React from 'react'
import * as PropTypes from 'prop-types'

class ProgressItem extends React.Component {
  static propTypes = {
    state: PropTypes.string.isRequired,
    /* left position of this part of the bar (percentage of full bar width) */
    start: PropTypes.number.isRequired,
    /* width of this part of the bar (percentage of full bar width) */
    width: PropTypes.number.isRequired
  }

  render () {
    const className = cx('progress-bar-custom', 'progress-bar-' +
      this.props.state)
    const style = {
      width: this.props.width + '%'
    }
    return (
      <span className={className} style={style} />
    )
  }
}

class VersionProgress extends React.Component {
  static propTypes = {
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
  }

  static defaultProps = {
    counts: {
      total: 0,
      approved: 0,
      translated: 0,
      needswork: 0,
      rejected: 0,
      untranslated: 0
    }
  }

  render () {
    const className = cx('progress-custom w-100 mb3')

    const { counts } = this.props

    const total = parseFloat(counts.total)
    const widths = chain(counts)
        .pick(['approved', 'translated', 'needswork',
          'rejected', 'untranslated'])
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
}

export default VersionProgress
