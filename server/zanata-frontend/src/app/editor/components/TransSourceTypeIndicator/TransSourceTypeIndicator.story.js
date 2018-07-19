// @ts-nocheck
/* eslint-disable */
import React from 'react'
import TransSourceTypeIndicator from './index.tsx'
import { storiesOf } from '@storybook/react';

/*
 * See .storybook/README.md for info on the component storybook.
 */
// @ts-ignore any
storiesOf('TransSourceTypeIndicator', module)
  .add('Google translations', () =>(
    <TransSourceTypeIndicator type='MT' metadata='Google' />)
)
.add('Microsoft translations', () =>(
  <TransSourceTypeIndicator type='MT' metadata='Ms' />)
)
.add('Not MT', () =>(
  <TransSourceTypeIndicator type='VM' />)
)