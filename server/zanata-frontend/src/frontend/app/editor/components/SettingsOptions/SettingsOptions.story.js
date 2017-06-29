import React from 'react'
import PropTypes from 'prop-types'
import { storiesOf, action } from '@kadira/storybook'
import SettingsOptions from '.'

storiesOf('SettingsOptions', module)
    .add('default', () => (
        <SettingsOptions
            settings={{
              id: 'Ambulance',
              label: 'Krankenwagen',
              active: {true}
            }}
            updateSetting={action(updateSetting)} />
    ))
