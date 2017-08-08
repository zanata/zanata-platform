import React from 'react'
import { storiesOf } from '@storybook/react'
import { action, decorateAction } from '@storybook/addon-actions'
import Notification from './component'

storiesOf('Notification', module)
    .add('Info', () => (
        <Notification severity="info"
          show="true"
          message="This is an information message"
          details="These are the details of the message">
        </Notification>
    ))
    .add('Warning', () => (
        <Notification severity="warn"
          show="true"
          message="This is a warning message"
          details="These are the details of the message">
        </Notification>
    ))
    .add('Error', () => (
        <Notification severity="error"
          show="true"
          message="This is an error message"
          details="These are the details of the message">
        </Notification>
    ))
