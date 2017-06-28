import React from 'react'
import PropTypes from 'prop-types'
import { storiesOf, action } from '@kadira/storybook'
import SettingOption from '.'

storiesOf('SettingOption', module)
  .add('default', () => (
    <SettingOption
        settings={{
        id="cat"
        label="cat"
        active={true}
      }} />
  ))
