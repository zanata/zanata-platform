import React, { PropTypes } from 'react'
import { Icon } from '../../../components'
import LoaderText from '../../../components/LoaderText'

const wrapperClasses = 'u-posCenterCenter u-textEmpty u-textCenter'
/**
 * Generic panel showing an icon and message, to
 * use when there are no suggestions to display.
 */
class NoSuggestionsPanel extends React.Component {
  static propTypes = {
    message: PropTypes.string.isRequired,
    icon: PropTypes.oneOf(['loader', 'search', 'suggestions']).isRequired
  }

  render () {
    if (this.props.icon === 'loader') {
      return (
        <div className={wrapperClasses}>
          <div className="u-sMB-1-4">
            <LoaderText loading loadingText={this.props.message} />
          </div>
        </div>
      )
    } else {
      return (
        <div className={wrapperClasses}>
          <div className="u-sMB-1-4">
            <Icon name={this.props.icon} className="s5" />
          </div>
          <p>{this.props.message}</p>
        </div>
      )
    }
  }
}

export default NoSuggestionsPanel
