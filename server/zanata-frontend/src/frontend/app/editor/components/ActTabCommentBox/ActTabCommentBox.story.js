import React from 'react'
import { storiesOf } from '@kadira/storybook'
import { action, decorateAction } from '@kadira/storybook-addon-actions'
import ActTabCommentBox from '.'

/*
 * See .storybook/README.md for info on the component storybook.
 */
storiesOf('ActTabCommentBox', module)
    .add('default', () => (
      <ActTabCommentBox />
  ))
