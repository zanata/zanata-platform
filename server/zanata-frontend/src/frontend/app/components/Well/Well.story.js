import React from 'react'
import { storiesOf } from '@storybook/react'
import { Well } from 'react-bootstrap'

storiesOf('Well', module)
    .add('default', () => (
        <Well>Look I'm in a well! <a href='#'>I am a link
            in a well!</a></Well>
    ))
    .add('with padding', () => (
        <Well bsSize='small'>Look I'm in a small well!</Well>

    ))

    .add('large', () => (
        <Well bsSize='large'>Look I'm in a large well!</Well>
    ))
