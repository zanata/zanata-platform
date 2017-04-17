import React from 'react'
import { storiesOf, action } from '@kadira/storybook'
import { Button } from 'react-bootstrap'

storiesOf('Button', module)
    .add('default', () => (
        <Button onClick={action('onClick')}>
          Unstyled button
        </Button>
    ))

storiesOf('Button', module)
    .add('primary', () => (
        <Button bsStyle='primary' onClick={action('onClick')}>
          Primary button
        </Button>
    ))

storiesOf('Button', module)
    .add('info', () => (
        <Button bsStyle='info' onClick={action('onClick')}>
          Info button
        </Button>
    ))

storiesOf('Button', module)
    .add('warning', () => (
        <Button bsStyle='warning' onClick={action('onClick')}>
          Warning button
        </Button>
    ))

storiesOf('Button', module)
    .add('danger', () => (
        <Button bsStyle='danger' onClick={action('onClick')}>
          Danger button
        </Button>
    ))

storiesOf('Button', module)
    .add('success', () => (
        <Button bsStyle='success' onClick={action('onClick')}>
          Success button
        </Button>
    ))
