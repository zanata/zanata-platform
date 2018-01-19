import * as React from 'react'
import * as PropTypes from 'prop-types'
import { Icon } from '../../../components'
import cx from 'classnames'
import { Row } from 'react-bootstrap'

/**
 * Display metadata for suggestion source.
 */
class SuggestionDetailsSummary extends React.Component {
  static propTypes = {
    onClick: PropTypes.func,
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
  }

  render () {
    const { matchDetails } = this.props.suggestion
    const topMatch = matchDetails[0]
    const isTextFlow = topMatch.type === 'LOCAL_PROJECT'

    const projectIcon = isTextFlow && (
      <li title={topMatch.projectId}>
        <Row>
          <Icon name="project" className="n1" /> {topMatch.projectName}
        </Row>
      </li>
    )

    const versionIcon = isTextFlow && (
      <li>
        <Row>
          <Icon name="version" className="n1" /> {topMatch.version}
        </Row>
      </li>
    )

    const documentPath = topMatch.documentPath
      ? topMatch.documentPath + '/'
      : ''
    const documentIcon = isTextFlow && (
      <li className="DocName" title={documentPath + topMatch.documentName}>
        <Row>
          <Icon name="document" className="n1" />
          <span className="ellipsis">{topMatch.documentName}</span>
        </Row>
      </li>
    )

    const importIcon = isTextFlow ? undefined : (
      <li>
        <Row>
          <Icon name="import" className="n1" /> {topMatch.transMemorySlug}
        </Row>
      </li>
    )

    const remainingIcon = matchDetails.length > 1 && (
      <li>
        <Row>
          <Icon name="translate" className="n1"
          /> {matchDetails.length - 1} more occurrences
        </Row>
      </li>
    )

    const className = cx('TransUnit-details', {
      'cursor-pointer': this.props.onClick
    })

    return (
      <div className={className} onClick={this.props.onClick}>
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
}

export default SuggestionDetailsSummary
