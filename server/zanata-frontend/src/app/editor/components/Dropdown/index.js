import cx from 'classnames'
import * as React from 'react'
import * as PropTypes from 'prop-types'

/**
 * Dropdown component that wraps a toggle button and some content to toggle.
 */
class Dropdown extends React.Component {
  static propTypes = {
    onToggle: PropTypes.func.isRequired,
    isOpen: PropTypes.bool.isRequired,
    enabled: PropTypes.bool,
    className: PropTypes.string,

    children: PropTypes.arrayOf(PropTypes.element).isRequired
  }

  static defaultProps = {
    enabled: true
  }

  setButtonDiv = (buttonDiv) => {
    this.buttonDiv = buttonDiv
  }

  toggleDropdown = () => {
    const node = this.buttonDiv
    this.props.onToggle(node)
  }

  render () {
    const className = cx({
      'EditorDropdown': true,
      'is-active': this.props.isOpen
    }, this.props.className)

    var buttonCount = 0
    var contentCount = 0

    const children = React.Children.map(this.props.children, (child) => {
      if (child.type === Dropdown.Button) {
        buttonCount++
        // TODO should be ok just to assign onClick undefined
        const onClick = this.props.enabled
          ? { onClick: this.toggleDropdown } : {}
        return (
          <div ref={this.setButtonDiv}
            className="EditorDropdown-toggle"
            aria-haspopup
            aria-expanded={this.props.isOpen}
            {...onClick}>
            {child}
          </div>
        )
      }
      if (child.type === Dropdown.Content) {
        contentCount++
        return child
      }
      throw Error('<Dropdown> can only contain <Dropdown.Button> and ' +
        '<Dropdown.Content> elements, but found <' + child.type + '>. ' +
        'Put the always-visible part in <Dropdown.Button> and the revealed ' +
        'part in <Dropdown.Content>')
    })

    if (buttonCount !== 1) {
      throw Error('<Dropdown> must contain exactly one <Dropdown.Button>, ' +
        ' but found ' + buttonCount)
    }
    if (contentCount !== 1) {
      throw Error('<Dropdown> must contain exactly one <Dropdown.Content>, ' +
        'but found ' + contentCount)
    }

    return (
      <div className={className}>
        {children}
      </div>
    )
  }
}

Dropdown.Button = class extends React.Component {
  static propTypes = {
    children: PropTypes.element.isRequired
  }

  render () {
    // just unwrap the child and return it
    return React.Children.only(this.props.children)
  }
}

Dropdown.Content = class extends React.Component {
  static propTypes = {
    children: PropTypes.node.isRequired
  }

  render () {
    return (
      <div className="EditorDropdown-content EditorDropdown-content--bordered">
        {this.props.children}
      </div>
    )
  }
}

export default Dropdown
