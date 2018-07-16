/* eslint-disable max-len */
// @ts-nocheck
import React from 'react'
import { action } from '@storybook/addon-actions'
import { storiesOf } from '@storybook/react'
import { TextInput } from '../'

storiesOf('TextInput', module)
    .add('default', () => (
      <span>
        <h2>
          <img
            src="https://upload.wikimedia.org/wikipedia/commons/4/49/Zanata-Logo.svg"
            width="42px" /> TextInput</h2>
        <p>Basic text input field</p>
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
