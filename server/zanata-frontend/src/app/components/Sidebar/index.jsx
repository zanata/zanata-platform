import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Layout from 'antd/lib/layout/'
import 'antd/lib/layout/style/css'
import Icon from '../../components/Icon'
import Menu from 'antd/lib/menu'
import 'antd/lib/menu/style/css'
import Row from 'antd/lib/row'
import 'antd/lib/row/style/css'
import ProcessingSidebar from './ProcessingSidebar'
import VersionPanel from './VersionPanel'

const { Sider, Content } = Layout

class Sidebar extends Component {
  static propTypes = {
    content: PropTypes.string.isRequired,
    active: PropTypes.string.isRequired
  }
  /* eslint-disable react/jsx-no-bind, no-return-assign */
  render () {
    const projectTitle = 'Zanata Server'
    const content = this.props.content
    return (
      <Layout>
        <Sider breakpoint='sm'
          defaultCollapsed={false}
          width='300'
          className='pvSidebar'
          collapsedWidth='0'>
          <h1 className='di txt-info'>
            <Icon name='project' className='s5 mr1 v-sub' />
            {projectTitle}
          </h1>
          <Menu defaultSelectedKeys={[this.props.active]}>
            <Menu.Item key='1'>
              <Icon name='users' className='s1 v-mid mr1' />
              <span className='v-mid'>People</span>
            </Menu.Item>
            <Menu.Item key='2'>
              <Icon name='info' className='s1 v-mid mr1' />
              <span className='v-mid'>About</span>
            </Menu.Item>
            <Menu.Item key='3'>
              <Icon name='settings' className='s1 v-mid mr1' />
              <span className='v-mid'>Settings</span>
            </Menu.Item>
          </Menu>
          <Button type='dashed' className='ml3 mt2 mb3'>
            <Icon name='glossary' className='s0 v-sub' /> Glossary
          </Button>
          <Row>
            <Button type='primary' size='small' icon='export'
              className='ml3 mt2 mb3'>Export to TMX</Button>
            <Button type='primary' size='small' className='ml3 mb3' icon='plus'>
            Add new version</Button>
          </Row>
          <ProcessingSidebar />
          <VersionPanel />
        </Sider>
        <Content>
          {content}
        </Content>
      </Layout>
    )
    /* eslint-enable react/jsx-no-bind, no-return-assign */
  }
}
export default Sidebar
