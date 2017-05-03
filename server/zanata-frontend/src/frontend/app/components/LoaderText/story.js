import React from 'react'
import { storiesOf } from '@kadira/storybook'
import { LoaderText } from '../'

storiesOf('LoaderText', module)
    .add('default', () => (
      <LoaderText loading={true} loadingText='Updating'>
        Update
      </LoaderText>
    ))
