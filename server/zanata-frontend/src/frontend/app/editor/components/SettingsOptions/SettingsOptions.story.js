import React from 'react'
import PropTypes from 'prop-types'
import { storiesOf, action } from '@kadira/storybook'
import SettingsOptions from '.'

/*
 * See .storybook/README.md for info on the component storybook.
 */
storiesOf('SettingOptions', module)
  .add('default', () => (
    <SettingsOptions
      settings={{

      }} />
  ))

