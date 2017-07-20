import React, { Component } from 'react'
import PropTypes from 'prop-types'
/**
 * Action button with an icon and title, unstyled.
 */
class TriCheckbox extends Component {
  static propTypes = {
    checked: PropTypes.bool.isRequired,
    indeterminate: PropTypes.bool.isRequired,
    /* arguments: clickCount, sound */
    onClick: PropTypes.func.isRequired
  }

  static defaultProps = {
    checked: false,
    indeterminate: false
  }

  constructor (props) {
    super(props)
    this.state = {
      checked: this.props.checked,
      indeterminate: this.props.indeterminate
    }
  }

  onClick = () => {
    // const { checked, indeterminate } = this.state
    this.setState({
      checked: false,
      indeterminate: true
    })
    this.props.onClick()
  }

  render () {
    const { checked, indeterminate, ...otherProps } = this.state
    return (
      <div>
        <input
          className={'mdc-checkbox__native-control'}
          type="checkbox"
          checked={checked}
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
