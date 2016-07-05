import Icon from './Icon'
import React, { PropTypes } from 'react'

/**
 * Paging buttons and current-page indicator.
 */

const PagerButton = React.createClass({
  render: function () {
    const icon =
      <Icon name={this.props.icon}
            title={this.props.title}
            className="u-sizeWidth-1"/>
    return (
        <li>
          {this.props.disabled
            ? <span className="u-textNeutral u-sizeHeight-1_1-2 u-textNoSelect"
                  title={this.props.title}>
                {icon}
              </span>
            : <a className="Link--neutral u-sizeHeight-1_1-2 u-textNoSelect"
               title={this.props.title}
               onClick={this.props.action}>
                {icon}
              </a>
          }
        </li>
    )
  }
})

const Pager = React.createClass({
  propTypes: {
    actions: PropTypes.shape({
      firstPage: PropTypes.func.isRequired,
      previousPage: PropTypes.func.isRequired,
      nextPage: PropTypes.func.isRequired,
      lastPage: PropTypes.func.isRequired
    }).isRequired,
    pageNumber: PropTypes.number.isRequired,
    pageCount: PropTypes.number,

    // DO NOT RENAME, the translation string extractor looks specifically
    // for gettextCatalog.getString when generating the translation template.
    gettextCatalog: PropTypes.shape({
      getString: PropTypes.func.isRequired
    }).isRequired
  },

  render: function () {
    const { actions, gettextCatalog, pageCount, pageNumber } = this.props
    const { firstPage, previousPage, nextPage, lastPage } = actions

    /* FIXME make this string translatable (was using angular gettext)
    const pageXofY = pageCount
      ? gettextCatalog.getString(
        '{{currentPage}} of {{totalPages}}', {
          currentPage: pageNumber,
          totalPages: pageCount
        })
      : pageNumber*/
    const pageXofY = `${pageNumber} of ${pageCount}`

    const buttons = {
      first: {
        icon: 'previous',
        title: gettextCatalog.getString('First page'),
        action: firstPage,
        disabled: pageNumber <= 1
      },
      prev: {
        icon: 'chevron-left',
        title: gettextCatalog.getString('Previous page'),
        action: previousPage,
        disabled: pageNumber <= 1
      },
      next: {
        icon: 'chevron-right',
        title: gettextCatalog.getString('Next page'),
        action: nextPage,
        disabled: pageNumber >= pageCount
      },
      last: {
        icon: 'next',
        title: gettextCatalog.getString('Last page'),
        action: lastPage,
        disabled: pageNumber >= pageCount
      }
    }

    return (
      <ul className="u-listHorizontal u-textCenter">
        <PagerButton {...buttons.first}/>
        <PagerButton {...buttons.prev}/>
        <li className="u-sizeHeight-1 u-sPH-1-4">
          <span className="u-textNeutral">
            {pageXofY}
          </span>
        </li>
        <PagerButton {...buttons.next}/>
        <PagerButton {...buttons.last}/>
      </ul>
    )
  }
})

export default Pager
