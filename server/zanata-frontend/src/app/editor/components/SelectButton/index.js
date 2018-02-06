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
import React from 'react'
import * as PropTypes from 'prop-types'
import { Button } from 'react-bootstrap'
import Icon from '../../../components/Icon'

class SelectButton extends React.Component {
  static propTypes = {
    id: PropTypes.string.isRequired,
    icon: PropTypes.oneOf(
        ['clock', 'comment', 'refresh', 'language']).isRequired,
    buttonName: PropTypes.string,
    className: PropTypes.string.isRequired,
    selected: PropTypes.bool.isRequired,
    selectItem: PropTypes.func.isRequired
  }

  selectItem = (event) => {
    this.props.selectItem(this.props.id, !this.props.selected)
  }

  render () {
    return (
      <Button onClick={this.selectItem}
        className={cx('Button Button--small u-rounded',
          this.props.className, {
            'is-active': this.props.selected
          })}>
        <Icon name={this.props.icon} className="n1" /> {this.props.buttonName}
      </Button>
    )
  }
}

export default SelectButton
