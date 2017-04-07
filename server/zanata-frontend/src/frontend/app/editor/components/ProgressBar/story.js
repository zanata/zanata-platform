import React from 'react'
import { storiesOf, action } from '@kadira/storybook'
import ProgressBar from '.'

const cts = {total: 10,approved: 2,translated: 3,needswork: 2,rejected: 1,untranslated: 2}

/*
 * See .storybook/README.md for info on the component storybook.
 */
storiesOf('ProgressBar', module)
  .add('Small', () => (
    <ProgressBar size='small' counts={cts}/>
  ))
  .add('Medium (default)', () => (
    <ProgressBar counts={cts}/>
  ))
  .add('Large', () => (
    <ProgressBar size='large' counts={cts}/>
  ))
