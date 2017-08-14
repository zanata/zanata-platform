import React from 'react'
import { storiesOf } from '@kadira/storybook'
import ActivityFeedItem from '.'

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

/*
 * See .storybook/README.md for info on the component storybook.
 */

const lastModifiedTime = new Date(2016, 11, 16, 2, 19)
const user = {
  username: 'damason',
  name: 'David Mason',
  imageUrl:
    'http://www.gravatar.com/avatar/a961139da8db88c4ae10d6dacf6bea1e?s=72'
}

storiesOf('ActivityFeedItem', module)
    .add('comment', () => (
      <ActivityFeedItem
        type="comment"
        content={'What in the world does this mean?'}
        lastModifiedTime={lastModifiedTime}
        user={user} />
      ))

    .add('translated', () => (
      <ActivityFeedItem
        type="revision"
        status="translated"
        content={'নাম'}
        lastModifiedTime={lastModifiedTime}
        user={user} />
    ))

    .add('fuzzy', () => (
      <ActivityFeedItem
        type="revision"
        status="fuzzy"
        content={'নাম'}
        lastModifiedTime={lastModifiedTime}
        user={user} />
    ))

    .add('approved', () => (
      <ActivityFeedItem
        type="revision"
        status="approved"
        content={'নাম'}
        lastModifiedTime={lastModifiedTime}
        user={user} />
    ))

    .add('rejected', () => (
      <ActivityFeedItem
        type="revision"
        status="rejected"
        content={'নাম'}
        lastModifiedTime={lastModifiedTime}
        user={user} />
    ))
