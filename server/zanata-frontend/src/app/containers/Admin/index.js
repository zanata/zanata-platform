// @ts-nocheck
/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
 *  as indicated by the @author tags. See the copyright.txt file in the
 *  distribution for a full listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this software; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import {connect} from 'react-redux'
import TMXExportModal from '../../components/TMX/TMXExportModal'
import { isAdmin } from '../../config'
import { Icon } from '../../components'
import Helmet from 'react-helmet'
import { Col, Row, Card } from 'antd'

import {
  showExportTMXModal
} from '../../actions/tmx-actions'

/**
 * Root component for Admin Page
 */
class Admin extends Component {
  static propTypes = {
    toggleTMXExportModal: PropTypes.func.isRequired
  }

  render () {
    if (!isAdmin) {
      return (
        <div>
          <Helmet title='Administration' />
          <div className='wideView' id='admin'>
            <div className='u-centerBlock'>
              <p>You are not authorised to access to this page</p>
            </div>
          </div>
        </div>
      )
    }
    const style = {
      backgroundColor: 'rgb(250,250,250)',
      fontSize: 16,
      marginBottom: 5
    }
    return (
      <div>
        <Helmet title='Administration' />
        <div className='page wideView' id='admin'>
          <div className='u-centerBlock'>
            <h1>Administration</h1>
            <Row gutter={6}>
              <Col xs={24} sm={12} lg={6}>
                <a id='Admin_Server_configuration_home'
                  href='/admin/server_configuration'>
                  <Card style={style} hoverable>
                    <Icon name='settings' className='s2 mr2 mr2' />
                    Server configuration
                  </Card>
                </a>
              </Col>
              <Col xs={24} sm={12} lg={6}>
                <a id='Admin_Manage_users_home'
                  href='/admin/usermanager'>
                  <Card style={style} hoverable>
                    <Icon name='user' className='s2 mr2' />
                    Manage users
                  </Card>
                </a>
              </Col>
              <Col xs={24} sm={12} lg={6}>
                <a id='Admin_Manage_roles_home'
                  href='/admin/rolemanager'>
                  <Card style={style} hoverable>
                    <Icon name='users' className='s2 mr2' />
                    Manage roles
                  </Card>
                </a>
              </Col>
              <Col xs={24} sm={12} lg={6}>
                <a id='Admin_Manage_search_home'
                  href='/admin/search'>
                  <Card style={style} hoverable>
                    <Icon name='search' className='s2 mr2' />
                    Manage search
                  </Card>
                </a>
              </Col>
            </Row>
            <Row gutter={6}>
              <Col xs={24} sm={12} lg={6}>
                <a id='Admin_Role_Assignment_Rules_home'
                  href='/admin/rolerules'>
                  <Card style={style} hoverable>
                    <Icon name='assign' className='s2 mr2' />
                    Role assignment rules
                  </Card>
                </a>
              </Col>
              <Col xs={24} sm={12} lg={6}>
                <a id='Translation_Memory_home'
                  href='/tm/home.xhtml'>
                  <Card style={style} hoverable>
                    <Icon name='tm' className='s2 mr2' />
                    Translation memory
                  </Card>
                </a>
              </Col>
              <Col xs={24} sm={12} lg={6}>
                <a id='Process_Management_home'
                  href='/admin/processmanager'>
                  <Card style={style} hoverable>
                    <Icon name='clock' className='s2 mr2' />
                    Process manager
                  </Card>
                </a>
              </Col>
              <Col xs={24} sm={12} lg={6}>
                <a id='Admin_Manage_stats_home'
                  href='/admin/stats'>
                  <Card style={style} hoverable>
                    <Icon name='piestats' className='s2 mr2' />
                    Overall statistics
                  </Card>
                </a>
              </Col>
            </Row>
            <Row gutter={6}>
              <Col xs={24} sm={12} lg={6}>
                <a id='Admin_Monitoring_home'
                  href='/admin/monitoring'>
                  <Card style={style} hoverable>
                    <Icon name='servmon' className='s2 mr2' />
                    Server monitoring
                  </Card>
                </a>
              </Col>
              <Col xs={24} sm={12} lg={6}>
                <a id='Cache_Stats_Home'
                  href='/admin/cachestats'>
                  <Card style={style} hoverable>
                    <Icon name='document' className='s2 mr2' />
                    Cache statistics
                  </Card>
                </a>
              </Col>
              <Col xs={24} sm={12} lg={6}>
                <a id='exportTMXAllLink'
                  href='#'
                  onClick={this.props.toggleTMXExportModal}>
                  <Card style={style} hoverable>
                    <Icon name='export' className='s2 mr2' />
                    Export all to TMX
                  </Card>
                </a>
              </Col>
              <Col xs={24} sm={12} lg={6}>
                <a id='reviewCriteria'
                  href='/admin/review'>
                  <Card style={style} hoverable>
                    <Icon name='tick' className='s2 mr2' />
                    Review criteria
                  </Card>
                </a>
              </Col>
            </Row>
            <TMXExportModal />
          </div>
        </div>
      </div>
    )
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    toggleTMXExportModal: (show) => {
      dispatch(showExportTMXModal(show))
    }
  }
}

export default connect(undefined, mapDispatchToProps)(Admin)
