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

import cx from 'classnames'
import { Icon } from '../../../components'
import React from 'react'
import * as PropTypes from 'prop-types'

/**
 * Styled checkbox to toggle a filter option on and off.
 */
class FilterToggle extends React.Component {
  static propTypes = {
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
  }

  static defaultProps = {
    count: 0,
    withDot: true
  }

  render () {
    const className = cx('Toggle u-curved', this.props.className)
    const dot = this.props.withDot && <Icon name="dot" className="n1" />

    return (
      <div className={className}>
        <input className="Toggle-checkbox"
          type="checkbox"
          id={this.props.id}
          checked={this.props.isChecked}
          onChange={this.props.onChange} />
        <span className="Toggle-fakeCheckbox" />
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
}

export default FilterToggle
