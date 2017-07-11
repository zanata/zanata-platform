import React from 'react'
import { storiesOf } from '@kadira/storybook'
import { action, decorateAction } from '@kadira/storybook-addon-actions'
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
    .add('default', () => {
      return <SelectButtonList items={items}
              selectItem={action('selectItem')}
              className="Button--secondary" />
    })

storiesOf('SelectButtonList', module)
    .add('first button active', () => {
      return <SelectButtonList items={items}
                               selectItem={action('selectItem')}
                               selected="all"
                               className="Button--secondary" />
    })
