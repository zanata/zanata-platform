import React, { PropTypes } from 'react'
import { Icon } from 'zanata-ui'

/**
 * Generic panel showing an icon and message, to
 * use when there are no suggestions to display.
 */
const NoSuggestionsPanel = React.createClass({

  propTypes: {
    message: PropTypes.string.isRequired,
    icon: PropTypes.oneOf(['loader', 'search', 'suggestions']).isRequired
  },

  render: function () {
    return (
      <div
        className="u-posCenterCenter u-textEmpty u-textCenter">
        <div className="u-sMB-1-4">
          <Icon name={this.props.icon} size="5" />
        </div>
        <p>{this.props.message}</p>
      </div>
    )
  }
})

export default NoSuggestionsPanel
