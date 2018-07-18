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
        <React.Fragment>
          <Sidebar />
          <div className='flexTab'>
            <p>This sidebar example has the active tag applied to both the People
              and Languages pages to provide examples of how this design handles
              sidebar links.</p>
          </div>
      </React.Fragment>
    ))
    .add('AboutPage', () => (
        <React.Fragment>
          <Sidebar />
          <AboutPage aboutText={aboutText} aboutLink={url} linkName={linkname} />
        </React.Fragment>
    ))
    .add('PeoplePage', () => (
        <React.Fragment>
          <Sidebar />
          <PeoplePage />
        </React.Fragment>
    ))
    .add('GroupsPage', () => (
        <React.Fragment>
          <Sidebar />
          <GroupsPage />
        </React.Fragment>
    ))
    .add('LanguagesPage', () => (
        <React.Fragment>
          <Sidebar />
          <LanguagesPage />
        </React.Fragment>
    ))
    .add('DocumentsPage', () => (
        <React.Fragment>
          <Sidebar />
          <DocumentsPage />
        </React.Fragment>
    ))
