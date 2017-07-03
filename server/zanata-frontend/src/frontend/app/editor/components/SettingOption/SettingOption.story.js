import React from 'react'
import PropTypes from 'prop-types'
import { storiesOf, action } from '@kadira/storybook'
import SettingOption from '.'

storiesOf('SettingOption', module)
  .add('default', () => (
      <SettingOption
          id='list-item-1'
          label='List item 1'
          active
          updateSetting={action('updateSetting')} />
  ))
