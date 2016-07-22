import cx from 'classnames'
import React, { PropTypes } from 'react'

/**
 * Dropdown with both an action button and a toggle button.
 */
const SplitDropdown = React.createClass({

  propTypes: {
    onToggle: PropTypes.func.isRequired,
    isOpen: PropTypes.bool.isRequired,
    enabled: PropTypes.bool,
    className: PropTypes.string,
    // passing as props is much less hassle than trying
    // to identify and work with child elements.
    actionButton: PropTypes.element.isRequired,
    toggleButton: PropTypes.element,
    content: PropTypes.element.isRequired
  },

  getDefaultProps: () => {
    return {
      enabled: true
    }
  },

  render: function () {
    const className = cx('Dropdown', this.props.className, {
      'is-active': this.props.isOpen
    })

    const buttonClick = this.props.enabled
        ? { onClick: this.props.onToggle } : {}

    var toggleButtonItem

    if (this.props.toggleButton) {
      toggleButtonItem = (
        <div className="ButtonGroup-item">
          <div ref="button"
            className="Dropdown-toggle"
            aria-haspopup
            aria-expanded={this.props.isOpen}
            {...buttonClick}>
            {this.props.toggleButton}
          </div>
        </div>
      )
    }

    return (
      <div className={className}>
        <div className="ButtonGroup ButtonGroup--hz
                        ButtonGroup--borderCollapse  ButtonGroup--round">
          <div className="ButtonGroup-item">
            {this.props.actionButton}
          </div>
          {toggleButtonItem}
        </div>
        {this.props.content}
      </div>
    )
  }
})

export default SplitDropdown
