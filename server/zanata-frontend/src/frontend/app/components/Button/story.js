import React from 'react'
import { storiesOf, action } from '@kadira/storybook'
import { Button } from 'react-bootstrap'

storiesOf('Button', module)
    .add('plain', () => (
        <Button onClick={action('onClick')}>
          Unstyled button. Pretty plain. Should be used with some styles.
        </Button>
    ))
