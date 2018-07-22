import React from 'react'
import IconButton from '.'
import { storiesOf } from '@storybook/react'

/*
 * See .storybook/README.md for info on the component storybook.
 */
// @ts-ignore any
const clickFun = function (_e) {}

storiesOf('IconButton', module)
  .add('markup', () => (
    <IconButton
      icon="mail"
      title="Mozart"
      onClick={clickFun}
      className="push-me" />)
  )
  .add('markup (disabled)', () => (
    <IconButton
      icon="mail"
      title="Tea"
      onClick={clickFun}
      disabled
      className="drink-me" />)
  )
