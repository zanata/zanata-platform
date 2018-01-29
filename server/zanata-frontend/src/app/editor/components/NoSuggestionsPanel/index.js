import * as React from 'react'
import * as PropTypes from 'prop-types'
import { Icon } from '../../../components'
import LoaderText from '../../../components/LoaderText'

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
    const isLoader = this.props.icon === 'loader'
    const icon = isLoader
        ? <LoaderText loading loadingText={this.props.message} />
        : <Icon name={this.props.icon} className="s5" />
    const messagePara = isLoader
        ? undefined
        : <p>{this.props.message}</p>

    return (
      <div className="u-posCenterCenter u-textEmpty u-textCenter">
        <div className="u-sMB-1-4">
          {icon}
        </div>
        {messagePara}
      </div>
    )
  }
}

export default NoSuggestionsPanel
