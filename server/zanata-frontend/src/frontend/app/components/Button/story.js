import React from 'react'
import { storiesOf, action } from '@kadira/storybook'
import { Button } from 'react-bootstrap'

storiesOf('Button', module)
    .add('default', () => (
        <Button onClick={action('onClick')}>
          Unstyled button
        </Button>
    ))
    .add('primary', () => (
        <Button bsStyle='primary' onClick={action('onClick')}>
          Primary button
        </Button>
    ))
    .add('info', () => (
        <Button bsStyle='info' onClick={action('onClick')}>
          Info button
        </Button>
    ))
    .add('warning', () => (
        <Button bsStyle='warning' onClick={action('onClick')}>
          Warning button
        </Button>
    ))
    .add('danger', () => (
        <Button bsStyle='danger' onClick={action('onClick')}>
          Danger button
        </Button>
    ))
    .add('success', () => (
        <Button bsStyle='success' onClick={action('onClick')}>
          Success button
        </Button>
    ))
