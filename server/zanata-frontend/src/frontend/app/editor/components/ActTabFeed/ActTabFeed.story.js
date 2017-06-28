import React from 'react'
import { storiesOf } from '@kadira/storybook'
import { action, decorateAction } from '@kadira/storybook-addon-actions'
import ActTabFeed from '.'
import RevisionTranslated from '../ActTabFeed/RevisionTranslated.js'
import RevisionApproved from '../ActTabFeed/RevisionApproved.js'
import RevisionRejected from '../ActTabFeed/RevisionRejected.js'
import RevisionComment from '../ActTabFeed/RevisionComment.js'
import RevisionFuzzy from '../ActTabFeed/RevisionFuzzy.js'

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

storiesOf('ActTabFeed', module)
    .add('comment', () => (
        <div className="revision-box">
          <RevisionComment />
        </div>
    ))
    .add('translated revision', () => (
        <div className="revision-box">
          <RevisionTranslated />
        </div>
    ))
    .add('fuzzy revision', () => (
        <div className="revision-box">
          <RevisionFuzzy />
        </div>
    ))
    .add('approved revision', () => (
        <div className="revision-box">
          <RevisionApproved />
        </div>
    ))
    .add('rejected revision', () => (
        <div className="revision-box">
          <RevisionRejected />
        </div>
    ))
