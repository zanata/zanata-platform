import React, { Component } from 'react'
import PropTypes from 'prop-types'
import cx from 'classnames'
/**
 * Checkbox with an intermediate state.
 * TODO: Write 'tri-checkbox' style
 */
class TriCheckbox extends Component {
  static propTypes = {
    className: PropTypes.string,
    indeterminate: PropTypes.bool.isRequired
  }

  static defaultProps = {
    indeterminate: false
  }

  render () {
    const className = cx('tri-checkbox', this.props.className)
    const { indeterminate, ...otherProps } = this.props
    return (
      <div>
        <input
          className={className}
          type="checkbox"
          ref={(nativeComponent) => {
            if (nativeComponent) {
              /* eslint-disable no-param-reassign */
              nativeComponent.indeterminate = indeterminate
              /* eslint-enable no-param-reassign */
            }
          }}
          {...otherProps}
        />
      </div>
    )
  }
}

export default TriCheckbox
