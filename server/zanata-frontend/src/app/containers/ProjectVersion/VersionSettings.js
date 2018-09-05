/* eslint-disable */
// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import {Icon} from '../../components'
import Tabs from 'antd/lib/tabs'
import 'antd/lib/tabs/style/css'
import General from './VersionSettingsTabs/General'
import Documents from './VersionSettingsTabs/Documents'
import Languages from './VersionSettingsTabs/Languages'
import Translation from './VersionSettingsTabs/Translation'

const TabPane = Tabs.TabPane
const generalTab = (
  <span className='f5'><Icon name='settings' className='s1 v-mid' /> General
  </span>)
const docsTab = (
  <span className='f5'><Icon name='document' className='s1 v-mid' /> Documents
  </span>)
const languagesTab = (
  <span className='f5'><Icon name='language' className='s1 v-mid' /> Languages
  </span>)
const translationTab = (
  <span className='f5'><Icon name='translate' className='s1 v-mid' /> Translation
  </span>)

const generalContent = <General />
const docsContent = <Documents />
const langContent = <Languages />
const transContent = <Translation />

class VersionSettings extends Component {
  render() {

    return (
      /* eslint-disable max-len */
      <div className='flexTab wideView'>
        <h1 className='txt-info'><Icon name='settings' className='s5 v-sub mr2' />
          <span className='fw4'>Version settings</span></h1>
        <Tabs
          tabPosition='left'>
          <TabPane tab={generalTab} key="1">{generalContent}</TabPane>
          <TabPane tab={docsTab} key="4">{docsContent}</TabPane>
          <TabPane tab={languagesTab} key="2">{langContent}</TabPane>
          <TabPane tab={translationTab} key="3">{transContent}</TabPane>
        </Tabs>
      </div>
      /* eslint-enable max-len */
    )
  }
}

export default VersionSettings

