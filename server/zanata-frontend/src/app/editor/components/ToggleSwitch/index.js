const cx /* TS: import cx */ = require('classnames')
import * as React from 'react'
import * as PropTypes from 'prop-types'

/**
 * Checkbox that appears as a slider-style switch
 */
class ToggleSwitch extends React.Component {
  static propTypes = {
    id: PropTypes.string,
    className: PropTypes.string,
    isChecked: PropTypes.bool.isRequired,
    onChange: PropTypes.func.isRequired,
    label: PropTypes.string.isRequired
  }

  render () {
    return (
      <span className={cx('Switch', this.props.className)}>
        <input className="Switch-checkbox"
          type="checkbox"
          id={this.props.id}
          checked={this.props.isChecked}
          onChange={this.props.onChange} />
        <label className="Switch-label" htmlFor={this.props.id}>
          <span className="Switch-labelText">{this.props.label}</span>
        </label>
      </span>
    )
  }
}

export default ToggleSwitch
