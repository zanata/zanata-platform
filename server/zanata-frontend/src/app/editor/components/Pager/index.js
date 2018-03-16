// @ts-nocheck
/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

import { Icon } from '../../../components'
import React from 'react'
import * as PropTypes from 'prop-types'
import { connect } from 'react-redux'
import {
  firstPage,
  nextPage,
  previousPage,
  lastPage
} from '../../actions/controls-header-actions'
import { getMaxPageIndex } from '../../selectors'

/**
* Paging buttons and current-page indicator.
*/
const PagerButton = ({ action, disabled, icon, title }) => {
  const iconElement =
    <Icon name={icon}
      title={title}
      className="s2" />
  return (
    <li>
      {disabled
        ? <span className="u-textNeutral u-sizeHeight-1_1-2 u-textNoSelect"
          title={title}>
          {iconElement}
        </span>
        : <a className="Link--neutral u-sizeHeight-1_1-2 u-textNoSelect"
          title={title}
          onClick={action}>
          {iconElement}
        </a>
      }
    </li>
  )
}

PagerButton.propTypes = {
  // name of the icon type to use
  icon: PropTypes.string.isRequired,
  // text to display on mouseover
  title: PropTypes.string.isRequired,
  // when true, appears faded and action is not bound
  disabled: PropTypes.bool.isRequired,
  // action to perform on click/tap
  action: PropTypes.func.isRequired
}

export const Pager = ({
  firstPage,
  previousPage,
  nextPage,
  lastPage,
  pageCount,
  pageNumber,
  gettextCatalog
}) => {
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
      <PagerButton {...buttons.first} />
      <PagerButton {...buttons.prev} />
      <li className="u-sizeHeight-1 u-sPH-1-4">
        <span className="u-textNeutral">
          {pageXofY}
        </span>
      </li>
      <PagerButton {...buttons.next} />
      <PagerButton {...buttons.last} />
    </ul>
  )
}

Pager.propTypes = {
  firstPage: PropTypes.func.isRequired,
  previousPage: PropTypes.func.isRequired,
  nextPage: PropTypes.func.isRequired,
  lastPage: PropTypes.func.isRequired,
  pageNumber: PropTypes.number.isRequired,
  pageCount: PropTypes.number,

  // DO NOT RENAME, the translation string extractor looks specifically
  // for gettextCatalog.getString when generating the translation template.
  gettextCatalog: PropTypes.shape({
    getString: PropTypes.func.isRequired
  }).isRequired
}

const mapStateToProps = (state) => {
  const { phrases, ui: { gettextCatalog } } = state
  const pageCount = getMaxPageIndex(state) + 1
  const pageNumber = Math.min(pageCount, phrases.paging.pageIndex + 1)

  return {
    pageCount: pageCount,
    pageNumber: pageNumber,
    gettextCatalog
  }
}

const mapDispatchToProps = {
  firstPage,
  previousPage,
  nextPage,
  lastPage
}

export default connect(mapStateToProps, mapDispatchToProps)(Pager)
