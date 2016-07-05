import cx from 'classnames'
import Icon from '../Icon'
import React, { PropTypes } from 'react'

/**
 * Styled checkbox to toggle a filter option on and off.
 */
const FilterToggle = React.createClass({

  propTypes: {
    id: PropTypes.string,
    className: PropTypes.string,
    isChecked: PropTypes.bool.isRequired,
    onChange: PropTypes.func.isRequired,
    title: PropTypes.string.isRequired,
    count: PropTypes.oneOfType([
      PropTypes.number,
      // FIXME stats API gives a string, change that to a number
      //       and remove this option.
      PropTypes.string
    ]),
    withDot: PropTypes.bool
  },

  getDefaultProps: () => {
    return {
      count: 0,
      withDot: true
    }
  },

  render: function () {
    const className = cx('Toggle u-round', this.props.className)
    const dot = this.props.withDot
      ? <Icon name="dot" className="Icon--xsm"/> : undefined

    return (
      <div className={className}>
        <input className="Toggle-checkbox"
               type="checkbox"
               id={this.props.id}
               checked={this.props.isChecked}
               onChange={this.props.onChange}/>
        <span className="Toggle-fakeCheckbox"/>
        <label className="Toggle-label"
               htmlFor={this.props.id}
               title={this.props.title}>
          {dot}
          {this.props.count}
          <span className="u-hiddenVisually">{this.props.title}</span>
        </label>
      </div>
    )
  }
})

export default FilterToggle
