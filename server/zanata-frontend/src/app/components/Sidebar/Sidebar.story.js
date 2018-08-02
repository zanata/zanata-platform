import React from 'react'
import { storiesOf } from '@storybook/react'
import Sidebar from '.'
import PeoplePage from '../../containers/ProjectVersion/PeoplePage'
import AboutPage from '../../containers/ProjectVersion/AboutPage'
import ProjectSettings from '../../containers/ProjectVersion/ProjectSettings'

const peoplePage = <PeoplePage />
const aboutPage = <AboutPage />
const settingsPage = <ProjectSettings />

storiesOf('Sidebar', module)
    .add('people', () => (
      <Sidebar active='1' content={peoplePage} />
    ))
    .add('about', () => (
      <Sidebar active='2' content={aboutPage} />
    ))
    .add('settings', () => (
      <Sidebar active='7' content={settingsPage} />
    ))
