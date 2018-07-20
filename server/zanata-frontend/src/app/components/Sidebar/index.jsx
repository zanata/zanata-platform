import React from 'react'
import { Component } from 'react'
import Layout from 'antd/lib/layout/'
import 'antd/lib/layout/style/css'
import Icon from '../../components/Icon'
import Menu from 'antd/lib/menu'
import 'antd/lib/menu/style/css'
import Select from 'antd/lib/select'
import 'antd/lib/select/style/css'
import ProcessingSidebar from './ProcessingSidebar'
import VersionPanel from './VersionPanel'

const { Content, Sider } = Layout
const Option = Select.Option

class Sidebar extends Component {
  /* eslint-disable react/jsx-no-bind, no-return-assign */
  render () {
    const projectTitle = 'Zanata Server'
    return (
      <Layout>
        <Sider breakpoint='sm'
          defaultCollapsed={false}
          width='300'
          className='pvSidebar'
          collapsedWidth='0'>
          <h1 className='di txt-info'>
            <Icon name='project' className='s3 mr1 mt1' />
            {projectTitle}
          </h1>
          <Menu defaultSelectedKeys={['1']}>
            <Menu.Item key='1'>
              <Icon name='users' className='s1 v-mid mr1' />
              <span className='v-mid'>People</span>
            </Menu.Item>
            <Menu.Item key='2'>
              <Icon name='glossary' className='s1 v-mid mr1' />
              <span className='v-mid'>Glossary</span>
            </Menu.Item>
            <Menu.Item key='3'>
              <Icon name='info' className='s1 v-mid mr1' />
              <span className='v-mid'>About</span>
            </Menu.Item>
            <Menu.Item key='4'>
              <Icon name='settings' className='s1 v-mid mr1' />
              <span className='v-mid'>Settings</span>
            </Menu.Item>
          </Menu>
          <div className='mt3 mb3'>
            <Select placeholder='Options' style={{ width: 120 }}>
              <Option value='1'>Option 1</Option>
              <Option value='2'>Option 2</Option>
              <Option value='3'>Option 3</Option>
            </Select>
          </div>
          <ProcessingSidebar />
          <VersionPanel />
        </Sider>
        <Content>Content</Content>
      </Layout>
    )
    /* eslint-enable react/jsx-no-bind, no-return-assign */
  }
}
export default Sidebar
