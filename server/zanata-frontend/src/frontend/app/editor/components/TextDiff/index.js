import React, { PropTypes } from 'react'
import Diff from 'text-diff'

const diff = new Diff()

function compare (text1, text2) {
  return diff.main(text1, text2)
}

module.exports = React.createClass({
  displayName: 'TextDiff',

  propTypes: {
    text1: PropTypes.string.isRequired,
    text2: PropTypes.string.isRequired,
    className: PropTypes.string
  },

  getDefaultProps: () => {
    return {
      className: 'Difference'
    }
  },

  /**
   * Semantic diff is an expensive operation, so it is worth
   * a check whether the text has changed before recalculating
   * the diff.
   */
  shouldComponentUpdate: function (nextProps, nextState) {
    return nextProps.text1 !== this.props.text1 ||
      nextProps.text2 !== this.props.text2 ||
      nextProps.className !== this.className
  },

  render: function () {
    var differences = compare(this.props.text1, this.props.text2)
    // modifies in-place
    diff.cleanupSemantic(differences)

    const result = differences.map(([type, text], index) => {
      switch (type) {
        case -1:
          return (<del key={index}>{text}</del>)
        case 0:
          return <span key={index}>{text}</span>
        case 1:
          return <ins key={index}>{text}</ins>
        default:
          console.error('invalid diff match type "' + type +
                        '". Expecting one of: -1, 0, 1')
      }
    })

    return (
      <div className={this.props.className}>
        {result}
      </div>
    )
  }
})
