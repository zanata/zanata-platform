// @ts-nocheck
import React from 'react'
import { storiesOf, action } from '@storybook/react'
import CommentBox from '.'

/*
 * See .storybook/README.md for info on the component storybook.
 */
storiesOf('CommentBox', module)
    .add('default', () => (
      <CommentBox postComment={action('postComment')} />
  ))
