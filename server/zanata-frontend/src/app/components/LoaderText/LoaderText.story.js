import React from 'react'
import { storiesOf } from '@storybook/react'
import { LoaderText } from '../'

storiesOf('LoaderText', module)
    .add('default', () => (
      <LoaderText loading={true} loadingText='Updating'>
        Update
      </LoaderText>
    ))
