import React from 'react'
import { storiesOf } from '@storybook/react'
import Sidebar from '.'
import AboutPage from '../../containers/ProjectVersion/AboutPage'
import PeoplePage from '../../containers/ProjectVersion/PeoplePage'
import GroupsPage from '../../containers/ProjectVersion/GroupsPage'
import LanguagesPage from '../../containers/ProjectVersion/LanguagesPage'
import DocumentsPage from '../../containers/ProjectVersion/DocumentsPage'

const aboutText = 'This is one rocking project version. This is the best' +
    ' project version ever.'
const url = 'https://www.google.com'
const linkname = 'Our awesome webpage'

storiesOf('Sidebar', module)
    .add('default', () => (
      <div>
        <Sidebar />
        <div className='flexTab'>
          <p>This sidebar example has the active tag applied to both the People
            and Languages pages to provide examples of how this design handles
            sidebar links.
          </p>
        </div>
      </div>
    ))
    .add('AboutPage', () => (
      <div>
        <Sidebar />
        <AboutPage aboutText={aboutText} aboutLink={url} linkName={linkname} />
      </div>
    ))
    .add('PeoplePage', () => (
      <div>
        <Sidebar />
        <PeoplePage />
      </div>
    ))
    .add('GroupsPage', () => (
      <div>
        <Sidebar />
        <GroupsPage />
      </div>
    ))
    .add('LanguagesPage', () => (
      <div>
        <Sidebar />
        <LanguagesPage />
      </div>
    ))
    .add('DocumentsPage', () => (
      <div>
        <Sidebar />
        <DocumentsPage />
      </div>
    ))
