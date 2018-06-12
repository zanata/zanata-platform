import cx from 'classnames'
import React from 'react'
import * as PropTypes from 'prop-types'
import { Button, ButtonGroup } from 'react-bootstrap'

/**
 * Dropdown with both an action button and a toggle button.
 */
class SplitDropdown extends React.Component {
  static propTypes = {
    onToggle: PropTypes.func.isRequired,
    isOpen: PropTypes.bool.isRequired,
    enabled: PropTypes.bool,
    className: PropTypes.string,
    // passing as props is much less hassle than trying
    // to identify and work with child elements.
    actionButton: PropTypes.element.isRequired,
    toggleButton: PropTypes.element,
    content: PropTypes.element.isRequired
  }

  static defaultProps = {
    enabled: true
  }

  render () {
    const className = cx('EditorDropdown', this.props.className, {
      'is-active': this.props.isOpen
    })

    const buttonClick = this.props.enabled
        ? { onClick: this.props.onToggle } : {}

    var toggleButtonItem

    if (this.props.toggleButton) {
      toggleButtonItem = (
        <div className="ButtonGroup-item">
          <ButtonGroup>
            <Button className="EditorDropdown-toggle"
              aria-haspopup
              aria-expanded={this.props.isOpen}
              {...buttonClick}>
            {this.props.toggleButton}</Button>
          </ButtonGroup>
        </div>
      )
    }

    return (
      /* eslint-disable max-len */
      <div className={className}>
        <ButtonGroup className="ButtonGroup--hz ButtonGroup--borderCollapse u-rounded">
          <div className="ButtonGroup-item">
            {this.props.actionButton}
          </div>
          {toggleButtonItem}
        </ButtonGroup>
        {this.props.content}
      </div>
      /* eslint-enable max-len */
    )
  }
}

export default SplitDropdown
