import React from 'react'
import { storiesOf } from '@storybook/react'
import Sidebar from '.'
import PeoplePage from '../../containers/ProjectVersion/PeoplePage'

const peoplePage = <PeoplePage />

storiesOf('Sidebar', module)
    .add('default', () => (
      <Sidebar content={peoplePage} />
    ))
