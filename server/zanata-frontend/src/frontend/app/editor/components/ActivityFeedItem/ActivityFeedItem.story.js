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
const lastModifiedTime = new Date()

storiesOf('ActivityFeedItem', module)
    .add('comment', () => {
      return <ActivityFeedItem
          content={'What in the world does this mean?'}
          icon={'comment'}
          lastModifiedTime={lastModifiedTime}
          message={'commented on a translation'}
          username={'Kathryn '} />
    })

    .add('translated', () => {
      return <ActivityFeedItem
          content={'নাম'}
          icon={'refresh'}
          lastModifiedTime={lastModifiedTime}
          message={'created a translation revision'}
          status={'u-textSuccess'}
          username={'Kathryn'}
          wellStatus={'well-translated'} />
    })

    .add('fuzzy', () => {
      return <ActivityFeedItem
          content={'নাম'}
          icon={'refresh'}
          lastModifiedTime={lastModifiedTime}
          message={'created a fuzzy revision'}
          status={'u-textUnsure'}
          username={'Kathryn'}
          wellStatus={'well-fuzzy'} />
    })

    .add('approved', () => {
      return <ActivityFeedItem statusapproved
          content='নাম'
          icon='refresh'
          lastModifiedTime={lastModifiedTime}
          message='approved a translation'
          status='approved'
          username='Kathryn' />
    })

    .add('rejected', () => {
      return <ActivityFeedItem
          content={'নাম'}
          icon={'refresh'}
          lastModifiedTime={lastModifiedTime}
          message={'rejected a translation'}
          status={'u-textWarning'}
          username={'Kathryn'} />
    })
