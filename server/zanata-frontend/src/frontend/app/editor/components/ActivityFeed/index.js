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

import React from 'react'
import PropTypes from 'prop-types'
import Icon from '../../../components/Icon'
import { Well } from 'react-bootstrap'

class ActivityFeed extends React.Component {
  static propTypes = {
    icon: PropTypes.oneOf(['comment', 'refresh']).isRequired,
    username: PropTypes.string.isRequired,
    message: PropTypes.isRequired,
    status: PropTypes.string.isRequired,
    wellStatus: PropTypes.string
  }

  render () {
    // add date from suggestions panel - only hardcode in storybook
    const date = 'May 18 2017 at 15:00'
    return (
        <div className="revision-box">
          <p><Icon name={this.props.icon} className="s0" />
            <img className="u-round activity-avatar" src="" /><a>{this.props.username}</a>
            has <span className={this.props.status}>{this.props.message}</span>
          </p>
          <Well className={this.props.wellStatus}>নাম</Well>
          <p className="small u-textMuted">
            <Icon name="clock" className="n1" /> {date}</p>
        </div>
    )
  }
}

export default ActivityFeed
