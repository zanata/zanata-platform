import React from 'react'
import * as PropTypes from 'prop-types'

/**
 * Heading that displays locale name and ID
 */
class TransUnitLocaleHeading extends React.Component {
  static propTypes = {
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired
  }

  render () {
    const { id, name } = this.props
    return (
      <h3 className="TransUnit-heading">
        {name} <span className="u-textMuted u-textUpper">{id}</span>
      </h3>
    )
  }
}

export default TransUnitLocaleHeading
