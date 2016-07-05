import React from 'react'
import { storiesOf } from '@kadira/storybook'
import LogoLoader from '.'

/*
 * See .storybook/README.md for info on the component storybook.
 */
storiesOf('LogoLoader', module)
  .add('default', () => (
    <LogoLoader inverted={false} loading={false}/>
  ))
  .add('inverted colour', () => (
    <LogoLoader inverted={true} loading={false}/>
  ))
  .add('loading', () => (
    <ul>
      <li><LogoLoader inverted={false} loading={true}/>
        inverted: false, loading: true</li>
      <li><LogoLoader inverted={true} loading={true}/>
        inverted: true, loading: true</li>
    </ul>
  ))
