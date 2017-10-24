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

import React, {Component} from 'react'
import PropTypes from 'prop-types'
import {FormattedMessage} from 'react-intl'
import Icon from '../../../components/Icon'
import Link from '../../../components/Link'
import DateAndTimeDisplay from '../DateAndTimeDisplay'
import {Well} from 'react-bootstrap'
import {profileUrl} from '../../api'

export const MINOR = 'Minor'
export const MAJOR = 'Major'
export const CRITICAL = 'Critical'

const statusToWellClass = {
  approved: 'well-approved',
  rejected: 'well-rejected',
  translated: 'well-translated',
  fuzzy: 'well-fuzzy',
  // FIXME class does not exist, create it
  untranslated: 'well-untranslated'
}

class ActivityFeedItem extends Component {
  static propTypes = {
    criteria: PropTypes.string,
    commentText: PropTypes.string,
    content: PropTypes.string,
    lastModifiedTime: PropTypes.instanceOf(Date).isRequired,
    // TODO damason define type for status
    priority: PropTypes.oneOf([
      MINOR,
      MAJOR,
      CRITICAL
    ]),
    status: PropTypes.oneOf(['translated', 'fuzzy', 'approved', 'rejected',
      'untranslated']),
    textStatus: PropTypes.oneOf(['u-textWarning', 'u-textDanger', 'u-textHighlight']),
    type: PropTypes.oneOf(['comment', 'revision']).isRequired,
    user: PropTypes.shape({
      name: PropTypes.string.isRequired,
      username: PropTypes.string.isRequired,
      imageUrl: PropTypes.string.isRequired,
    }).isRequired
  }

  getMessage = () => {
    const {user, type, status} = this.props
    // Uses href because editor app is separate from frontend app
    const comment = (
      <span className="comment">
        <Well bsSize="small">
        <Icon name="comment" className="s0"/>
          {this.props.commentText}
        </Well>
      </span>
    )
    const criteria = (
      <span className="CriteriaText">
        {this.props.criteria}
      </span>
    )
    const name = (
      <Link useHref link={profileUrl(user.username)}>{user.name}</Link>
    )
    const priority = (
      <span className="CriteriaText">
        <Icon name="warning" className="s0"/>
        <span className={this.props.textStatus}>
          {this.props.priority}
        </span>
      </span>
    )
    if (type === 'comment') {
      return (
        <FormattedMessage id="ActivityFeedItem.comment"
          description="Title for a comment in the activity feed."
          defaultMessage="{name} has commented on a translation"
          values={{
            name
          }}
        />
      )
    }
    // else it is a revision
    switch (status) {
      case 'approved':
        // Nested formatted messages are needed to highlight part of the message
        const approvedTranslation = (
          <span className="u-textHighlight">
            <FormattedMessage id="ActivityFeedItem.approved.approvedTranslation"
              description={
                'Highlighted section inserted into ActivityFeedItem.approved'}
              defaultMessage="approved a translation"/>
          </span>
        )
        return (
          <FormattedMessage id="ActivityFeedItem.approved"
            description={
              'Title for an item in the activity feed showing a reviewer ' +
              'approved the translation. The inserted section is from ' +
              'ActivityFeedItem.approved.approvedTranslation'}
            defaultMessage="{name} has {approvedTranslation}"
            values={{
              name,
              approvedTranslation
          }}/>
        )

      case 'rejected':
        // Nested formatted messages are needed to highlight part of the message
        const rejectedTranslation = (
          <span className="u-textWarning">
            <FormattedMessage id="ActivityFeedItem.rejected.rejectedTranslation"
              description={
                'Highlighted section inserted into ActivityFeedItem.rejected'}
              defaultMessage="rejected a translation"/>
          </span>
        )
        return (
          <FormattedMessage id="ActivityFeedItem.rejected"
            description={
              'Title for an item in the activity feed showing a reviewer ' +
              'rejected the translation. The inserted section is from ' +
              'ActivityFeedItem.rejected.rejectedTranslation'}
            defaultMessage="{name} has {rejectedTranslation} for the reason: {criteria} - {priority} priority. {comment}"
            values={{
              comment,
              criteria,
              name,
              priority,
              rejectedTranslation
          }}/>
        )

      case 'translated':
        // Nested formatted messages are needed to highlight part of the message
        const translatedRevision = (
          <span className="u-textSuccess">
            <FormattedMessage
              id="ActivityFeedItem.translated.translatedRevision"
              description={
                'Highlighted section inserted into ActivityFeedItem.translated'}
              defaultMessage="created a translation revision"/>
          </span>
        )
        return (
          <FormattedMessage id="ActivityFeedItem.translated"
            description={
              'Title for an item in the activity feed showing a ' +
              'translator added a translation. The inserted section is from ' +
              'ActivityFeedItem.translated.translatedRevision'}
            defaultMessage="{name} has {translatedRevision}"
            values={{
              name,
              translatedRevision
          }}/>
        )

      case 'fuzzy':
        // Nested formatted messages are needed to highlight part of the message
        const fuzzyRevision = (
          <span className="u-textUnsure">
            <FormattedMessage id="ActivityFeedItem.fuzzy.fuzzyRevision"
              description={
                'Highlighted section inserted into ActivityFeedItem.fuzzy'}
              defaultMessage="created a fuzzy revision"/>
          </span>
        )
        return (
          <FormattedMessage id="ActivityFeedItem.fuzzy"
            description={
              'Title for an item in the activity feed showing a ' +
              'translator saved a fuzzy translation (a translation that ' +
              'still needs to be edited). The inserted section is from ' +
              'ActivityFeedItem.fuzzy.fuzzyRevision'}
            defaultMessage="{name} has {fuzzyRevision}"
            values={{
              name,
              fuzzyRevision
          }}/>
        )

      case 'untranslated':
        // Nested formatted messages are needed to highlight part of the message
        const deletedTranslation = (
          <span className="u-textPrimary">
            <FormattedMessage id="ActivityFeedItem.deleted.deletedTranslation"
              description={
                'Highlighted section inserted into ActivityFeedItem.deleted'}
              defaultMessage="deleted a translation"/>
          </span>
        )
        return (
          <FormattedMessage id="ActivityFeedItem.deleted"
            description={
              'Title for an item in the activity feed showing a ' +
              'translator has deleted the translation. The inserted ' +
              'section is from ActivityFeedItem.deleted.deletedTranslation'}
            defaultMessage="{name} has {deletedTranslation}"
            values={{
              name,
              deletedTranslation
          }}/>
        )

      default:
        console.error('Unknown status type', status)
    }
  }

  render() {
    const {content, lastModifiedTime, status, type, user} = this.props
    const isComment = type === 'comment'

    return (
      <div className="RevisionBox">
        <p>
          <Icon name={isComment ? 'comment' : 'refresh'} className="s0"/>
          <Link useHref link={profileUrl(user.username)}>
            {/* TODO use component for avatar image */}
            <img className="u-round ActivityAvatar" src={user.imageUrl}/>
          </Link>
          {this.getMessage()}
        </p>
        <Well className={isComment ? '' : statusToWellClass[status]}>
          {content}</Well>
        <DateAndTimeDisplay dateTime={lastModifiedTime}
          className="u-block small u-sMT-1-2 u-sPB-1-4
          u-textMuted u-textSecondary"/>
      </div>
    )
  }
}

export default ActivityFeedItem
