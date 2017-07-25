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

  setNativeComponent = (nativeComponent) => {
    this.nativeComponent = nativeComponent
    if (nativeComponent) {
      nativeComponent.indeterminate = this.props.indeterminate
    }
  }

  componentWillReceiveProps (nextProps) {
    if (this.nativeComponent &&
      (nextProps.indeterminate !== this.props.indeterminate)) {
      this.nativeComponent.indeterminate = nextProps.indeterminate
    }
  }

  render () {
    const className = cx('tri-checkbox', this.props.className)
    /*eslint no-unused-vars: off*/
    // pulling indeterminate out of props to avoid invalid props errors
    const { indeterminate, ...otherProps } = this.props
    return (
      <div>
        <input
          className={className}
          type="checkbox"
          ref={this.setNativeComponent}
          {...otherProps}
        />
      </div>
    )
  }
}

export default TriCheckbox
