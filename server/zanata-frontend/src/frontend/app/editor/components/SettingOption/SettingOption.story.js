import React from 'react'
import PropTypes from 'prop-types'
import { storiesOf, action } from '@kadira/storybook'
import SettingOption from '.'

storiesOf('SettingOption', module)
  .add('default', () => (
      <SettingOption
        setting={{
          id: 'Ambulance',
          label: 'Krankenwagen',
          active: {true}
     }}
      updateSetting={action(updateSetting)} />
  ))
