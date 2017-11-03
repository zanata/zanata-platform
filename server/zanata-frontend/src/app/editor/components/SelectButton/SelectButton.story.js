import React from 'react'
import { storiesOf, action } from '@storybook/react'
import SelectButton from '.'

/*
 * See .storybook/README.md for info on the component storybook.
 */
storiesOf('SelectButton', module)
  .add('default', () => (
    <SelectButton icon="clock" id="all" selectItem={action('selectItem')}
      buttonName="All" className="Button--secondary" selected={false} />
  ))

  .add('active', () => (
    <SelectButton icon="clock" id="all" selectItem={action('selectItem')}
      buttonName="All" className="Button--secondary" selected />
  ))
