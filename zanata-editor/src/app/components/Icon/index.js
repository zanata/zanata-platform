import cx from 'classnames'
import React, { PropTypes } from 'react'

/**
 * Icon component, usually renders an svg icon
 */
const Icon = React.createClass({

  propTypes: {
    name: PropTypes.string.isRequired,
    title: PropTypes.node,
    className: PropTypes.string
  },

  /**
   * Render a standard icon using SVG
   */
  svgIcon: function () {
    const titleMarkup = this.props.title
      ? '<title>' + this.props.title + '</title>'
      : ''

    // jsx does not understand xlink:href, so it is generated manually.
    // includes <title>since this is used as the full content of the svg tag
    const innerHtml = '<use xlink:href="#Icon-' + this.props.name +
                    '"/>' + titleMarkup

    return <svg className="Icon-item"
             dangerouslySetInnerHTML={{__html: innerHtml}} />
  },

  /**
   * Render an animated loader
   */
  loaderIcon: function () {
    const dot = <span className="Icon--loader-dot"/>
    return (
      <span className="Icon-item"
            title={this.props.title}>
        {dot}{dot}{dot}
      </span>
    )
  },

  render: function () {
    const isLoader = this.props.name === 'loader'

    const className = cx(this.props.className, 'Icon', {
      'Icon--loader': isLoader
    })

    return (
      <div className={className}>
        {isLoader
           ? this.loaderIcon()
           : this.svgIcon()}
      </div>
    )
  }
})

export default Icon
