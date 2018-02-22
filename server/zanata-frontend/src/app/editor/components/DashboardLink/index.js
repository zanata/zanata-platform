import React from 'react'
import * as PropTypes from 'prop-types'

/**
 * Gravatar icon that links to the dashboard page
 */
class DashboardLink extends React.Component {
  static propTypes = {
    dashboardUrl: PropTypes.string.isRequired,
    name: PropTypes.string,
    gravatarUrl: PropTypes.string
  }

  static defaultProps = {
    // default "mystery man" icon
    gravatarUrl:
        'https://www.gravatar.com/avatar/00000000000000000000000000000000?d=' +
        'mm&f=y'
  }

  render () {
    return (
      <a href={this.props.dashboardUrl}
        className="u-sizeHeight-2 u-sizeWidth-1_1-2 u-inlineBlock"
        title={this.props.name}>
        <img className="u-round Header-avatar"
          src={this.props.gravatarUrl} />
      </a>
    )
  }
}

export default DashboardLink
