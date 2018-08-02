import React from 'react'
import { storiesOf } from '@storybook/react'
import Sidebar from '.'
import PeoplePage from '../../containers/ProjectVersion/PeoplePage'
import AboutPage from '../../containers/ProjectVersion/AboutPage'

const peoplePage = <PeoplePage />
const aboutPage = <AboutPage />

storiesOf('Sidebar', module)
    .add('people', () => (
      <Sidebar active='1' content={peoplePage} />
    ))
    .add('about', () => (
      <Sidebar active='2' content={aboutPage} />
    ))
