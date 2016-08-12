import React, { PropTypes } from 'react'
import { Icon, Row } from 'zanata-ui'

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

    const projectIcon = isTextFlow && (
      <li title={topMatch.projectId}>
        <Row>
          <Icon name="project" size="n1" /> {topMatch.projectName}
        </Row>
      </li>
    )

    const versionIcon = isTextFlow && (
      <li>
        <Row>
          <Icon name="version" size="n1" /> {topMatch.version}
        </Row>
      </li>
    )

    const documentPath = topMatch.documentPath
      ? topMatch.documentPath + '/'
      : ''
    const documentIcon = isTextFlow && (
      <li title={documentPath + topMatch.documentName}>
        <Row>
          <Icon name="document" size="n1" /> {topMatch.documentName}
        </Row>
      </li>
    )

    const importIcon = isTextFlow ? undefined : (
      <li>
        <Row>
          <Icon name="import" size="n1" /> {topMatch.transMemorySlug}
        </Row>
      </li>
    )

    const remainingIcon = matchDetails.length > 1 && (
      <li>
        <Row>
          <Icon name="translate" size="n1"
          /> {matchDetails.length - 1} more occurrences
        </Row>
      </li>
    )

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
