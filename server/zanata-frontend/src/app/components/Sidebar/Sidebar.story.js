import React from 'react'
import { storiesOf } from '@storybook/react'
import Sidebar from '.'
import PeoplePage from '../../containers/ProjectVersion/PeoplePage'
import AboutPage from '../../containers/ProjectVersion/AboutPage'
import ProjectSettings from '../../containers/ProjectVersion/ProjectSettings'

const peoplePage = <PeoplePage />
const aboutPage = <AboutPage />
const settingsPage = <ProjectSettings />

const pages = [peoplePage, aboutPage, settingsPage]

// TODO: implement as redux state
let active = '1'
// @ts-ignore
const onSelect = ({key}) => {
  active = key
}

storiesOf('Sidebar', module)
    .add('all', () => (
      <Sidebar active='1' content={pages[parseInt(active)]} onSelect={onSelect} />
    ))
    .add('people', () => (
      <Sidebar active='1' content={peoplePage} onSelect={onSelect} />
    ))
    .add('about', () => (
      <Sidebar active='2' content={aboutPage} onSelect={onSelect} />
    ))
    .add('settings', () => (
      <Sidebar active='3' content={settingsPage} onSelect={onSelect} />
    ))
    .add('languages', () => (
      <Sidebar active='4' content={settingsPage} onSelect={onSelect} />
    ))
    .add('documents', () => (
      <Sidebar active='5' content={settingsPage} onSelect={onSelect} />
    ))
    .add('groups', () => (
      <Sidebar active='6' content={settingsPage} onSelect={onSelect} />
    ))
    .add('version settings', () => (
      <Sidebar active='7' content={settingsPage} onSelect={onSelect} />
    ))

