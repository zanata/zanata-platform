import React from 'react'
import { storiesOf } from '@kadira/storybook'
import ActivityFeed from '.'

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

storiesOf('ActivityFeed', module)
    .add('comment', () => {
      return <ActivityFeed
          icon={'comment'}
          message={'commented on a translation.'}
          status={'u-textMuted'} username={'Kathryn '} />
    })

    .add('translated', () => {
      const icon = 'refresh'
      const username = 'Kathryn '
      const message = 'marked this as translated'
      const status = 'u-textSuccess'
      const wellStatus = 'well-translated'

      return <ActivityFeed
          icon={icon} message={message} status={status} wellStatus={wellStatus}
          username={username} />
    })

    .add('fuzzy', () => {
      const icon = 'refresh'
      const username = 'Kathryn '
      const message = 'marked this as fuzzy'
      const status = 'u-textUnsure'
      const wellStatus = 'well-fuzzy'

      return <ActivityFeed
          icon={icon} message={message} status={status} wellStatus={wellStatus}
          username={username} />
    })

    .add('approved', () => {
      const icon = 'refresh'
      const username = 'Kathryn '
      const message = 'approved this translation'
      const status = 'u-textHighlight'
      const wellStatus = 'well-approved'

      return <ActivityFeed
          icon={icon} message={message} status={status} wellStatus={wellStatus}
          username={username} />
    })


    .add('rejected', () => {
      const icon = 'refresh'
      const username = 'Kathryn '
      const message = 'rejected this translation'
      const status = 'u-textWarning'
      const wellStatus = 'well-rejected'

      return <ActivityFeed
          icon={icon} message={message} status={status} wellStatus={wellStatus}
          username={username} />
    })
