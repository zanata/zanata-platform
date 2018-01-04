import React from 'react'
import { storiesOf, action } from '@storybook/react'
import { TextInput } from '../'
import { Well } from 'react-bootstrap'

storiesOf('TextInput', module)
    .add('default', () => (
        <span>
            <h2><img
                src="https://upload.wikimedia.org/wikipedia/commons/4/49/Zanata-Logo.svg"
                width="42px"/> TextInput</h2>
          <Well>Basic text input field</Well>
      <TextInput
          maxLength={100}
          id='demo'
          className='textInput'
          placeholder='TextInput…'
          accessibilityLabel='TextInput'
          defaultValue='Default text'
          onKeyDown={action('keyDown')}
      />
        </span>
    ))
