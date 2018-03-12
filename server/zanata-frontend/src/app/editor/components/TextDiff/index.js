import React from 'react'
import * as PropTypes from 'prop-types'
import { diffWords } from 'diff'

function compare (text1, text2) {
  return diffWords(text1, text2, { ignoreCase: true })
}

export default class extends React.Component {
  static displayName = 'TextDiff';

  static propTypes = {
    text1: PropTypes.string.isRequired,
    text2: PropTypes.string.isRequired,
    className: PropTypes.string,
    // Matches are highlighted and non-matches display with line-through
    simpleMatch: PropTypes.bool
  }

  static defaultProps = {
    className: 'Difference'
  }

  /**
   * Semantic diff is an expensive operation, so it is worth
   * a check whether the text has changed before recalculating
   * the diff.
   */
  shouldComponentUpdate (nextProps, _nextState) {
    return nextProps.text1 !== this.props.text1 ||
      nextProps.text2 !== this.props.text2 ||
      // @ts-ignore
      nextProps.className !== this.className
  }

  render () {
    var differences = compare(this.props.text1, this.props.text2)
    // modifies in-place
    // diff.cleanupSemantic(differences)
    const simpleMatch = this.props.simpleMatch
    // const result = differences.map(([type, text], index) => {
    //   switch (type) {
    //     case -1:
    //       return (simpleMatch
    //         ? <span className="line-through" key={index}>{text}</span>
    //         : <del key={index}>{text}</del>)
    //     case 0:
    //       return (simpleMatch
    //         ? <span className="highlight" key={index}>{text}</span>
    //         : <span key={index}>{text}</span>)
    //     case 1:
    //       return (simpleMatch
    //         ? ''
    //         : <ins key={index}>{text}</ins>)
    //     default:
    //       console.error('invalid diff match type "' + type +
    //                     '". Expecting one of: -1, 0, 1')
    //   }
    // })
    const result = differences.map((part, index) => {
      if (part.added) {
        return (simpleMatch
                ? ''
                : <ins key={index}>{part.value}</ins>)
      }
      if (part.removed) {
        return (simpleMatch
                ? <span className="line-through" key={index}>{part.value}</span>
                : <del key={index}>{part.value}</del>)
      }
      return (simpleMatch
              ? <span className="highlight" key={index}>{part.value}</span>
              : <span key={index}>{part.value}</span>)
    })

    return (
      <div className={this.props.className}>
        {result}
      </div>
    )
  }
}
