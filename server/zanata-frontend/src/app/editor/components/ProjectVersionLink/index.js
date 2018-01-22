import * as React from 'react'
import * as PropTypes from 'prop-types'

/**
 * Link to open the version page.
 * Label is project + version name
 */
class ProjectVersionLink extends React.Component {
  static propTypes = {
    project: PropTypes.shape({
      name: PropTypes.string
    }).isRequired,
    version: PropTypes.string,
    url: PropTypes.string
  }

  static defaultProps = {
    project: {
      name: 'Loading... '
    },
    versionSlug: 'Loading... '
  }

  render () {
    // TODO use project slug if name is not defined
    return (
      <a href={this.props.url}
        className="Link--invert Header-item u-inlineBlock">
        <span className="u-sPH-1-4 u-sizeWidth1 u-gtemd-hidden">
          <i className="i i--arrow-left"></i>
        </span>
        <span className="Editor-currentProject u-sm-hidden u-sML-1-2">
          <span>{this.props.project.name}</span> <span
            className="u-textMuted">{this.props.version}</span>
        </span>
      </a>
    )
  }
}

export default ProjectVersionLink
