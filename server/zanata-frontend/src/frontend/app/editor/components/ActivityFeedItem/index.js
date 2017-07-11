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
import { FormattedDate, FormattedTime } from 'react-intl'
import Icon from '../../../components/Icon'
import { Well } from 'react-bootstrap'

//TODO add other statuses
const statusToColor = {
  approved: 'u-textHighlight',
  rejected: 'u-textWarning',
  translated: 'u-textSuccess',
  fuzzy: 'u-textUnsure',
  untranslated: 'u-textPrimary'

}

const statusToWellClass = {
  approved: 'well-approved'
}

class ActivityFeedItem extends React.Component {
  static propTypes = {
    content: PropTypes.string.isRequired,
    icon: PropTypes.oneOf(['comment', 'refresh']).isRequired,
    lastModifiedTime: PropTypes.string.isRequired,
    message: PropTypes.string.isRequired,
    //TODO damason define type for status
    status: PropTypes.oneOf(['translated', 'fuzzy', 'approved', 'rejected',
      'untranslated']),
    username: PropTypes.string.isRequired
  }

  render () {
    return (
      <div className="revision-box">
        <p><Icon name={this.props.icon} className="s0" />
          <img className="u-round activity-avatar" src="" />
          <a>{this.props.username}</a>&nbsp;
          has <span className={statusToColor[this.props.status]}>
            {this.props.message}</span>
        </p>
        <Well className={statusToWellClass[this.props.status]}>
          {this.props.content}</Well>
        <p className="small u-textMuted">
          <Icon name="clock" className="n1" />&nbsp;
          <FormattedDate value={this.props.lastModifiedTime}
            format="medium" />&nbsp;
          <FormattedTime value={this.props.lastModifiedTime} />
        </p>
      </div>
    )
  }
}

export default ActivityFeedItem
