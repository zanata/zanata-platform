import React from 'react'
import { action } from '@storybook/addon-actions'
import { storiesOf } from '@storybook/react'
import COMPONENT_NAME_HERE from '.'

/*
 * TODO add stories showing the range of states
 *      for COMPONENT_NAME_HERE
 */
storiesOf('COMPONENT_NAME_HERE', module)
  .add('default', () => (
    <COMPONENT_NAME_HERE fancy={false} onClick={action('onClick')} />
  ))
  .add('fancy', () => (
    <COMPONENT_NAME_HERE fancy noise="mwah" onClick={action('onClick')} />
  ))
