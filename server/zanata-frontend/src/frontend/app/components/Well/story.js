import React from 'react'
import { storiesOf, action } from '@kadira/storybook'
import { Well } from 'react-bootstrap'

storiesOf('Well', module)
    .add('default', () => (
        <span>
          <Well>Look I'm in a well! <a href='#'>I am a link
            in a well!</a></Well>
          <Well bsSize='large'>Look I'm in a large well!</Well>
          <Well bsSize='small'>Look I'm in a small well!</Well>
        </span>
      ))
