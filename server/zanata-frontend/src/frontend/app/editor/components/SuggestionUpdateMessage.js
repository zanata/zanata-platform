import React from 'react'
import PropTypes from 'prop-types'
import { FormattedDate } from 'react-intl'
import { Icon } from '../../components'
import { Row } from 'react-bootstrap'

/**
 * Show an appropriate message about the source and time of the most
 * recent update to the translation of a suggestion.
 */
class SuggestionUpdateMessage extends React.Component {
  static propTypes = {
    matchType: PropTypes.oneOf(['imported', 'translated', 'approved'])
      .isRequired,
    user: PropTypes.string,
    lastChanged: PropTypes.instanceOf(Date)
  }

  message = () => {
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
  }

  render () {
    return (
      <span className="u-textMeta">
        <Row>
          <Icon name="history" className="s0" />
          <span className="u-sML-1-4">{this.message()}</span>
        </Row>
      </span>
    )
  }
}

export default SuggestionUpdateMessage
