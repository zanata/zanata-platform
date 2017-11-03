import React from 'react'
import { storiesOf } from '@storybook/react'
import Notification from './component'

storiesOf('Notification', module)
  .add('Info', () => (
    <Notification severity="info"
      show
      message="This is an information message"
      details="These are the details of the message" />
  ))
  .add('Warning', () => (
    <Notification severity="warn"
      show
      message="This is a warning message"
      details="These are the details of the message" />
  ))
  .add('Error', () => (
    <Notification severity="error"
      show
      message="This is an error message"
      details="These are the details of the message" />
  ))
