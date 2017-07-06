import React from 'react'
import { storiesOf } from '@kadira/storybook'
import { action, decorateAction } from '@kadira/storybook-addon-actions'
import SelectButtonList from '.'

storiesOf('SelectButtonList', module)
    .add('default', () => {

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

      return <SelectButtonList items={items}
              selectItem={action('selectItem')}
              className="Button--secondary" />
    })

storiesOf('SelectButtonList', module)
    .add('first button active', () => {

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

      return <SelectButtonList items={items}
                               selectItem={action('selectItem')}
                               selected="all"
                               className="Button--secondary" />
    })
