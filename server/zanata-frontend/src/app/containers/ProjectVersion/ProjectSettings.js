/* eslint-disable */
// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import {Icon} from '../../components'
import Tabs from 'antd/lib/tabs'
import 'antd/lib/tabs/style/css'
import General from './ProjectSettingsTabs/General'
import Languages from './ProjectSettingsTabs/Languages'

const TabPane = Tabs.TabPane
const generalTab = (
  <span className='f5'><Icon name='settings' className='s1 v-mid' /> General
  </span>)
const languagesTab = (
  <span className='f5'><Icon name='language' className='s1 v-mid' /> Languages
  </span>)
const translationTab = (
  <span className='f5'><Icon name='translate' className='s1 v-mid' /> Translation
  </span>)
const permissionsTab = (
  <span className='f5'><Icon name='users' className='s1 v-mid' /> Permissions
  </span>)
const webhooksTab = (
  <span className='f5'><Icon name='code' className='s1 v-mid' /> Webhooks
  </span>)
const aboutTab = (
  <span className='f5'><Icon name='info' className='s1 v-mid' /> About
  </span>)
const generalContent = <General/>
const langContent = <Languages />

class ProjectSettings extends Component {
  render() {

    return (
      /* eslint-disable max-len */
      <div className='flexTab wideView'>
        <h1 className='txt-info'><Icon name='settings' className='s5 v-sub mr2' />
          <span className='fw4'>Settings</span></h1>
        <Tabs
          tabPosition='left'>
          <TabPane tab={generalTab} key="1">{generalContent}</TabPane>
          <TabPane tab={languagesTab} key="2">{langContent}</TabPane>
          <TabPane tab={translationTab} key="3">Content of tab 3</TabPane>
          <TabPane tab={permissionsTab} key="4">Content of tab 4</TabPane>
          <TabPane tab={webhooksTab} key="5">Content of tab 5</TabPane>
          <TabPane tab={aboutTab} key="6">Content of tab 6</TabPane>
        </Tabs>
      </div>
      /* eslint-enable max-len */
    )
  }
}

export default ProjectSettings

