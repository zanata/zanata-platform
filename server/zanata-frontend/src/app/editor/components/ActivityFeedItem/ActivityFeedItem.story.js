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
import { storiesOf } from '@storybook/react'
import ActivityFeedItem from '.'
import { MINOR, MAJOR, CRITICAL } from '../../utils/reject-trans-util'

/*
 * See .storybook/README.md for info on the component storybook.
 */

const lastModifiedTime = new Date(2016, 11, 16, 2, 19)
const user = {
  name: 'damason',
  username: 'David Mason'
}

storiesOf('ActivityFeedItem', module)
    .add('comment', () => (
      <ActivityFeedItem
        type='comment'
        content={'What in the world does this mean?'}
        lastModifiedTime={lastModifiedTime}
        user={user} />
    ))

    .add('translated', () => (
      <ActivityFeedItem
        type='revision'
        status='translated'
        content={'নাম'}
        lastModifiedTime={lastModifiedTime}
        user={user} />
    ))

    .add('needswork', () => (
      <ActivityFeedItem
        type='revision'
        status='needswork'
        content={'নাম'}
        lastModifiedTime={lastModifiedTime}
        user={user} />
    ))

    .add('approved', () => (
      <ActivityFeedItem
        type='revision'
        status='approved'
        content={'নাম'}
        lastModifiedTime={lastModifiedTime}
        user={user} />
    ))

    .add('rejected - minor priority', () => (
      <ActivityFeedItem
        criteria='Spelling and Grammar'
        type='revision'
        priority={MINOR}
        textStatus='u-textHighlight'
        status='rejected'
        commentText='You spelt this wrong.'
        content={'নাম'}
        lastModifiedTime={lastModifiedTime}
        user={user} />
    ))
    .add('rejected - major priority', () => (
      <ActivityFeedItem
        criteria='Spelling and Grammar'
        type='revision'
        priority={MAJOR}
        textStatus='u-textWarning'
        status='rejected'
        commentText='You spelt this wrong.'
        content={'নাম'}
        lastModifiedTime={lastModifiedTime}
        user={user} />
    ))
    .add('rejected - critical priority', () => (
      <ActivityFeedItem
        criteria='Spelling and Grammar'
        type='revision'
        priority={CRITICAL}
        textStatus='u-textDanger'
        status='rejected'
        commentText='You spelt this wrong.'
        content={'নাম'}
        lastModifiedTime={lastModifiedTime}
        user={user} />
    ))
