// @ts-nocheck
import React from 'react'
import { storiesOf } from '@storybook/react'
import ProgressBar from '.'

const counts = {
  total: 10,
  approved: 2,
  translated: 3,
  needswork: 2,
  rejected: 1,
  untranslated: 2
}

/*
 * See .storybook/README.md for info on the component storybook.
 */
storiesOf('ProgressBar', module)
  .add('Small', () => (
    <ProgressBar size='small' counts={counts} />
  ))
  .add('Medium (default)', () => (
    <ProgressBar counts={counts} />
  ))
  .add('Large', () => (
    <ProgressBar size='large' counts={counts} />
  ))
