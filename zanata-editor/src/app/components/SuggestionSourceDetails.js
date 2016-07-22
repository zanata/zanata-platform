import React, { PropTypes } from 'react'
import Icon from './Icon'

/**
 * Display metadata for suggestion source.
 */
const SuggestionSourceDetails = React.createClass({
  propTypes: {
    suggestion: PropTypes.shape({
      matchDetails: PropTypes.arrayOf(PropTypes.shape({
        type: PropTypes.oneOf(
          ['IMPORTED_TM', 'LOCAL_PROJECT']).isRequired,
        transMemorySlug: PropTypes.string,
        projectId: PropTypes.string,
        projectName: PropTypes.string,
        version: PropTypes.string,
        documentPath: PropTypes.string,
        documentName: PropTypes.string
      }))
    })
  },

  render: function () {
    const { matchDetails } = this.props.suggestion
    const topMatch = matchDetails[0]
    const isTextFlow = topMatch.type === 'LOCAL_PROJECT'

    const projectIcon = isTextFlow ? (
      <li title={topMatch.projectId}>
        <Icon name="project" className="Icon--xsm" /> {topMatch.projectName}
      </li>
    ) : undefined

    const versionIcon = isTextFlow ? (
      <li>
        <Icon name="version" className="Icon--xsm" /> {topMatch.version}
      </li>
    ) : undefined

    const documentPath = topMatch.documentPath
      ? topMatch.documentPath + '/'
      : ''
    const documentIcon = isTextFlow ? (
      <li title={documentPath + topMatch.documentName}>
        <Icon name="document" className="Icon--xsm" /> {topMatch.documentName}
      </li>
    ) : undefined

    const importIcon = isTextFlow ? undefined : (
      <li>
        <Icon name="import" className="Icon--xsm" /> {topMatch.transMemorySlug}
      </li>
    )

    const remainingIcon = matchDetails.length > 1 ? (
      <li>
        <Icon name="translate" class="Icon--xsm"
        /> {matchDetails.length - 1} more occurrences
      </li>
    ) : undefined

    return (
      <div className="TransUnit-details">
        <ul className="u-textMeta u-listInline u-sizeLineHeight-1">
          {projectIcon}
          {versionIcon}
          {documentIcon}
          {importIcon}
          {remainingIcon}
        </ul>
      </div>
    )
  }
})

export default SuggestionSourceDetails
