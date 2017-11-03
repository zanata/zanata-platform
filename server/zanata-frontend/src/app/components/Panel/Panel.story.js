import React from 'react'
import { storiesOf } from '@storybook/react'
import { Panel } from 'react-bootstrap'

storiesOf('Panel', module)
    .add('default', () => (
        <Panel>
          Basic panel example
        </Panel>
    ))

    .add('with heading', () => (
        <Panel header='Panel header'>
          Panel content
        </Panel>
    ))

    .add('primary', () => (
        <Panel header='Panel header' bsStyle='primary'>
          Panel content
        </Panel>
    ))

    .add('success', () => (
        <Panel header='Panel header' bsStyle='success'>
          Panel content
        </Panel>
    ))

    .add('info', () => (
        <Panel header='Panel header' bsStyle='info'>
          Panel content
        </Panel>
    ))

    .add('warning', () => (
        <Panel header='Panel header' bsStyle='warning'>
          Panel content
        </Panel>
    ))

    .add('danger', () => (
        <Panel header='Panel header' bsStyle='danger'>
          Panel content
        </Panel>
    ))
