import React from 'react'
import { storiesOf, action } from '@kadira/storybook'
import { Alert } from 'react-bootstrap'

storiesOf('Alert', module)
    .add('success', () => (
        <Alert bsStyle='success'>
          <strong>Holy guacamole!</strong> Best check yo self
        </Alert>
    ))
    .add('warning', () => (
        <Alert bsStyle='warning'>
          <strong>Holy guacamole!</strong> Best check yo self
        </Alert>
    ))
    .add('danger', () => (
        <Alert bsStyle='danger'>
          <strong>Holy guacamole!</strong> Best check yo self
        </Alert>
    ))
    .add('info', () => (
        <Alert bsStyle='info'>
          <strong>Holy guacamole!</strong> Best check yo self
        </Alert>
    ))
