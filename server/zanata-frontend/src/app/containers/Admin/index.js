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
import { Link, Icon } from '../../components'
import Helmet from 'react-helmet'
import Col from 'antd/lib/col'
import 'antd/lib/col/style/css'
import Row from 'antd/lib/row'
import 'antd/lib/row/style/css'

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
    return (
      <div>
        <Helmet title='Administration' />
        <div className='page wideView' id='admin'>
          <div className='u-centerBlock'>
            <h1>Administration</h1>
            <Row gutter={6}>
              <Col xs={24} sm={12} lg={6}>
                <Link id='Admin_Server_configuration_home'
                  link='/admin/server_configuration'
                  useHref className='list-group-item'>
                  <Icon name='settings' className='s2' />
                  Server configuration
                </Link>
              </Col>
              <Col xs={24} sm={12} lg={6}>
                <Link id='Admin_Manage_users_home'
                  className='list-group-item'
                  link='/admin/usermanager' useHref>
                  <Icon name='user' className='s2' />
                  Manage users
                </Link>
              </Col>
              <Col xs={24} sm={12} lg={6}>
                <Link id='Admin_Manage_roles_home'
                  className='list-group-item'
                  link='/admin/rolemanager' useHref>
                  <Icon name='users' className='s2' />
                  Manage roles
                </Link>
              </Col>
              <Col xs={24} sm={12} lg={6}>
                <Link id='Admin_Manage_search_home'
                  className='list-group-item'
                  link='/admin/search' useHref>
                  <Icon name='search' className='s2' />
                  Manage search
                </Link>
              </Col>
            </Row>
            <Row gutter={6}>
              <Col xs={24} sm={12} lg={6}>
                <Link id='Admin_Role_Assignment_Rules_home'
                  className='list-group-item'
                  link='/admin/rolerules' useHref>
                  <Icon name='assign' className='s2' />
                  Role assignment rules
                </Link>
              </Col>
              <Col xs={24} sm={12} lg={6}>
                <Link id='Translation_Memory_home'
                  className='list-group-item'
                  link='/tm' useHref>
                  <Icon name='tm' className='s2' />
                  Translation memory
                </Link>
              </Col>
              <Col xs={24} sm={12} lg={6}>
                <Link id='Process_Management_home'
                  className='list-group-item'
                  link='/admin/processmanager' useHref>
                  <Icon name='clock' className='s2 mr2' />
                  Process manager
                </Link>
              </Col>
              <Col xs={24} sm={12} lg={6}>
                <Link id='Admin_Manage_stats_home'
                  className='list-group-item'
                  link='/admin/stats' useHref>
                  <Icon name='piestats' className='s2 mr2' />
                  Overall statistics
                </Link>
              </Col>
            </Row>
            <Row gutter={6}>
              <Col xs={24} sm={12} lg={6}>
                <Link id='Admin_Monitoring_home'
                  className='list-group-item'
                  link='/admin/monitoring' useHref>
                  <Icon name='servmon' className='s2 mr2' />
                  Server monitoring
                </Link>
              </Col>
              <Col xs={24} sm={12} lg={6}>
                <Link id='Cache_Stats_Home'
                  className='list-group-item'
                  link='/admin/cachestats' useHref>
                  <Icon name='document' className='s2 mr2' />
                  Cache statistics
                </Link>
              </Col>
              <Col xs={24} sm={12} lg={6}>
                <a id='exportTMXAllLink'
                  className='list-group-item'
                  href='#'
                  onClick={this.props.toggleTMXExportModal}>
                  <Icon name='export' className='s2 mr2' />
                  Export all to TMX
                </a>
              </Col>
              <Col xs={24} sm={12} lg={6}>
                <Link id='reviewCriteria'
                  className='list-group-item' link='/admin/review'>
                  <Icon name='tick' className='s2 mr2' />
                  Review criteria
                </Link>
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
