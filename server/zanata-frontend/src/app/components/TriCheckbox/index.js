import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import cx from 'classnames'
/**
 * Checkbox with an intermediate state.
 */
class TriCheckbox extends Component {
  static propTypes = {
    className: PropTypes.string,
    useDefaultStyle: PropTypes.bool.isRequired,
    indeterminate: PropTypes.bool.isRequired
  }

  static defaultProps = {
    indeterminate: false,
    useDefaultStyle: true
  }

  // @ts-ignore any
  setNativeComponent = (nativeComponent) => {
    this.nativeComponent = nativeComponent
    if (nativeComponent) {
      nativeComponent.indeterminate = this.props.indeterminate
    }
  }

  // @ts-ignore any
  componentWillReceiveProps (nextProps) {
    if (this.nativeComponent &&
      (nextProps.indeterminate !== this.props.indeterminate)) {
      this.nativeComponent.indeterminate = nextProps.indeterminate
    }
  }

  render () {
    // pulling indeterminate out of props to avoid invalid props errors
    // eslint-disable-next-line no-unused-vars
    const { indeterminate, useDefaultStyle, className, ...otherProps } =
      this.props
    const classes = cx({'triCheckbox bstrapReact': useDefaultStyle}, className)
    return (
      <input
        className={classes}
        type="checkbox"
        ref={this.setNativeComponent}
        {...otherProps}
      />
    )
  }
}

export default TriCheckbox
