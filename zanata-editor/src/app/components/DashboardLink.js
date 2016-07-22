import React, { PropTypes } from 'react'

/**
 * Gravatar icon that links to the dashboard page
 */
const DashboardLink = React.createClass({

  propTypes: {
    dashboardUrl: PropTypes.string.isRequired,
    name: PropTypes.string,
    gravatarUrl: PropTypes.string
  },

  getDefaultProps: () => {
    return {
      // default "mystery man" icon
      gravatarUrl: 'http://www.gravatar.com/avatar/?d=mm'
    }
  },

  render: function () {
    return (
      <a href={this.props.dashboardUrl}
        className="u-sizeHeight-2 u-sizeWidth-1_1-2 u-inlineBlock"
        title={this.props.name}>
        <img className="u-round Header-avatar"
          src={this.props.gravatarUrl} />
      </a>
    )
  }
})

export default DashboardLink
