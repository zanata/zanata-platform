import React from 'react'
import { storiesOf, action } from '@kadira/storybook'
import { LoaderText } from '../'

storiesOf('LoaderText', module)
    .add('default', () => (
      <LoaderText loading={true} loadingText='Updating'>
        Update
      </LoaderText>
    ))
