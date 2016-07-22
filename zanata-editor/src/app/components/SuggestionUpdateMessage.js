import React, { PropTypes } from 'react'
import { FormattedDate } from 'react-intl'
import { Icon, Row } from 'zanata-ui'

/**
 * Show an appropriate message about the source and time of the most
 * recent update to the translation of a suggestion.
 */
const SuggestionUpdateMessage = React.createClass({

  propTypes: {
    matchType: PropTypes.oneOf(['imported', 'translated', 'approved'])
      .isRequired,
    user: PropTypes.string,
    lastChanged: PropTypes.instanceOf(Date)
  },

  message: function () {
    const date =
      <FormattedDate value={this.props.lastChanged} format="medium" />

    switch (this.props.matchType) {
      case 'imported':
        return (
          <span>
            Last updated {date}
          </span>
        )
      case 'translated':
        return (
          <span>
            Translated by {this.props.user} on {date}
          </span>
        )
      case 'approved':
        return (
          <span>
            Approved by {this.props.user} on {date}
          </span>
        )
      default:
        console.error('invalid match type to generate message: ' +
                      this.props.matchType)
    }
  },

  render: function () {
    return (
      <span className="u-textMeta">
        <Row>
          <Icon name="history" size="0" />
          <span className="u-sML-1-4">{this.message()}</span>
        </Row>
      </span>
    )
  }
})

export default SuggestionUpdateMessage
