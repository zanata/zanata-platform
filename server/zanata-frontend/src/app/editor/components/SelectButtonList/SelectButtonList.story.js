import * as React from 'react'
import { storiesOf, action } from '@storybook/react'
import SelectButtonList from '.'

const items = [
  {
    id: 'all',
    icon: 'clock',
    label: 'All'
  },
  {
    id: 'comments',
    icon: 'comment',
    label: 'Comments'
  },
  {
    id: 'updates',
    icon: 'refresh',
    label: 'Updates'
  }
]

storiesOf('SelectButtonList', module)
  .add('default', () => (
    <SelectButtonList items={items}
      selectItem={action('selectItem')}
      className="Button--secondary" />
  ))

  .add('first button active', () => (
    <SelectButtonList items={items}
      selectItem={action('selectItem')}
      selected="all"
      className="Button--secondary" />
  ))
