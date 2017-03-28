import React from 'react'
import { storiesOf, action } from '@kadira/storybook'
import { Button } from 'react-bootstrap'

storiesOf('Button', module)
    .add('default', () => (
        <Button bsStyle='default'>Default</Button>
    ))
    .add('primary', () => (
        <Button bsStyle='primary'>Primary</Button>
    ))
