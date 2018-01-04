import React from 'react'
import { storiesOf } from '@storybook/react'
import Notification from './component'
import { Well, Table } from 'react-bootstrap'

storiesOf('Notification', module)
  .add('Info', () => (
    <Notification severity="info"
      show
      message="This is an information message"
      details="Use when information needs to be given to the user before proceeding" />
  ))
  .add('Warning', () => (
      <Notification severity="warn"
      show
      message="This is a warning message"
      details="Use when a warning needs to be given to the user before proceeding" />
  ))
  .add('Error', () => (
    <Notification severity="error"
      show
      message="This is an error message"
      details="Use when there is a danger in the user taking a specific action" />
        ))
