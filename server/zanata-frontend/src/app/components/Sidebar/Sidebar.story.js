import React from 'react'
import { storiesOf } from '@storybook/react'
import Sidebar from '.'
import AboutPage from '../../containers/ProjectVersion/AboutPage'
import PeoplePage from '../../containers/ProjectVersion/PeoplePage'
import GroupsPage from '../../containers/ProjectVersion/GroupsPage'
import LanguagesPage from "../../containers/ProjectVersion/LanguagesPage";
import DocumentsPage from "../../containers/ProjectVersion/DocumentsPage";

const aboutText = 'This is one rocking project version. This is the best' +
    ' project version ever.'
const url = 'https://www.google.com'
const linkname = 'Our awesome webpage'

storiesOf('Sidebar', module)
    .add('default (no test)', () => (
        <div>
          <Sidebar />
          <div className='flexTab'>
            <p>This sidebar example has the active tag applied to both the People
              and Languages pages to provide examples of how this design handles
              sidebar links.</p>
            <p>The sidebar nav has been implemented using &nbsp;
              <a href='https://react-bootstrap.github.io/components.html#navs'>
                react bootstrap components</a>.</p>
          </div>
      </div>
    ))
    .add('AboutPage (no test)', () => (
        <div>
          <Sidebar />
          <AboutPage aboutText={aboutText} aboutLink={url} linkName={linkname} />
        </div>
    ))
    .add('PeoplePage (no test)', () => (
        <div>
          <Sidebar />
          <PeoplePage />
        </div>
    ))
    .add('GroupsPage (no test)', () => (
        <div>
          <Sidebar />
          <GroupsPage />
        </div>
    ))
    .add('LanguagesPage (no test)', () => (
        <div>
          <Sidebar />
          <LanguagesPage />
        </div>
    ))
    .add('DocumentsPage (no test)', () => (
        <div>
          <Sidebar />
          <DocumentsPage />
        </div>
    ))
