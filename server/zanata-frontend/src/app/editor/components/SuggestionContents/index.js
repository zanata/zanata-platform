const TextDiff = require('../TextDiff')
import * as React from 'react'
import * as PropTypes from 'prop-types'
const cx /* TS: import cx */ = require('classnames')

/**
 * Display all content strings (singular or plurals) for a suggestion.
 * May show a diff against a set of provided strings.
 */
class SuggestionContents extends React.Component {
  static propTypes = {
    plural: PropTypes.bool.isRequired,
    contents: PropTypes.arrayOf(
      PropTypes.string
    ).isRequired,
    showDiff: PropTypes.bool,
    compareTo: PropTypes.arrayOf(
      PropTypes.string
    )
  }

  pluralFormLabel = (index) => {
    if (this.props.plural) {
      // FIXME translate the text. Either:
      //    - get it from Angular Gettext
      //    - use react-intl for it
      const text = index ? 'Plural Form' : 'Singular Form'
      return (
        <span className="u-textMeta">
          {text}
        </span>
      )
    }
  }

  /* Simple or diff content, depending whether props.compareTo
   * is present.
   */
  contentDiv = (content, index) => {
    const compareTo = this.props.compareTo
    const showDiff = this.props.showDiff
    const className = cx(
      'TransUnit-text',
      {
        'TransUnit-text--tight': !this.props.plural,
        'Difference': compareTo
      }
    )
    const simpleView = compareTo
      ? <TextDiff
        className={className}
        text1={index >= compareTo.length ? '' : compareTo[index]}
        text2={content} simpleMatch />
      : <div className={className}>
          {content}
      </div>
    return showDiff
      ? <TextDiff
        className={className}
        text1={index >= compareTo.length ? '' : compareTo[index]}
        text2={content} />
      : simpleView
  }

  render () {
    const contents = this.props.contents.map((content, index) => {
      return (
        <div key={index} className="TransUnit-item">
          <div className="TransUnit-itemHeader">
            {this.pluralFormLabel(index)}
          </div>
          {this.contentDiv(content, index)}
        </div>
      )
    })

    return (
      <div>
        {contents}
      </div>
    )
  }
}

export default SuggestionContents
