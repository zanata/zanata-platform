import React from 'react'
import { storiesOf } from '@kadira/storybook'
import { action } from '@kadira/storybook-addon-actions'
import CommentBox from '.'

/*
 * See .storybook/README.md for info on the component storybook.
 */
storiesOf('CommentBox', module)
    .add('default', () => (
      <CommentBox postComment={action('postComment')} />
  ))
