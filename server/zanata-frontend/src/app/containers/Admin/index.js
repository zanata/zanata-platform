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
import { Grid, Col, ListGroup, ListGroupItem } from 'react-bootstrap'
import { Icon } from '../../components'
import Helmet from 'react-helmet'

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
            <Grid>
              <h1>Administration</h1>
              <Col xs={12} sm={5} lg={3}>
                <ListGroup>
                  <ListGroupItem id='Admin_Server_configuration_home'
                    href='/admin/server_configuration'>
                    <Icon name='settings' className='s2' />
                    Server configuration
                  </ListGroupItem>
                </ListGroup>
              </Col>
              <Col xs={12} sm={5} lg={3}>
                <ListGroup>
                  <ListGroupItem id='Admin_Manage_users_home'
                    href='/admin/usermanager'>
                    <Icon name='user' className='s2' />
                    Manage users
                  </ListGroupItem>
                </ListGroup>
              </Col>
              <Col xs={12} sm={5} lg={3}>
                <ListGroup>
                  <ListGroupItem id='Admin_Manage_roles_home'
                    href='/admin/rolemanager'>
                    <Icon name='users' className='s2' />
                    Manage roles
                  </ListGroupItem>
                </ListGroup>
              </Col>
              <Col xs={12} sm={5} lg={3}>
                <ListGroup>
                  <ListGroupItem id='Admin_Manage_search_home'
                    href='/admin/search'>
                    <Icon name='search' className='s2' />
                    Manage search
                  </ListGroupItem>
                </ListGroup>
              </Col>
              <Col xs={12} sm={5} lg={3}>
                <ListGroup>
                  <ListGroupItem id='Admin_Role_Assignment_Rules_home'
                    href='/admin/rolerules'>
                    <Icon name='assign' className='s2' />
                    Role assignment rules
                  </ListGroupItem>
                </ListGroup>
              </Col>
              <Col xs={12} sm={5} lg={3}>
                <ListGroup>
                  <ListGroupItem id='Translation_Memory_home'
                    href='/tm/home.xhtml'>
                    <Icon name='tm' className='s2' />
                    Translation memory
                  </ListGroupItem>
                </ListGroup>
              </Col>
              <Col xs={12} sm={5} lg={3}>
                <ListGroup>
                  <ListGroupItem id='Process_Management_home'
                    href='/admin/processmanager'>
                    <Icon name='clock' className='s2' />
                    Process manager
                  </ListGroupItem>
                </ListGroup>
              </Col>
              <Col xs={12} sm={5} lg={3}>
                <ListGroup>
                  <ListGroupItem id='Admin_Manage_stats_home'
                    href='/admin/stats'>
                    <Icon name='piestats' className='s2' />
                    Overall statistics
                  </ListGroupItem>
                </ListGroup>
              </Col>
              <Col xs={12} sm={5} lg={3}>
                <ListGroup>
                  <ListGroupItem id='Admin_Monitoring_home'
                    href='/admin/monitoring'>
                    <Icon name='servmon' className='s2' />
                    Server monitoring
                  </ListGroupItem>
                </ListGroup>
              </Col>
              <Col xs={12} sm={5} lg={3}>
                <ListGroup>
                  <ListGroupItem id='Cache_Stats_Home'
                    href='/admin/cachestats'>
                    <Icon name='document' className='s2' />
                    Cache statistics
                  </ListGroupItem>
                </ListGroup>
              </Col>
              <Col xs={12} sm={5} lg={3}>
                <ListGroup>
                  <ListGroupItem id='exportTMXAllLink'
                    href='#'
                    onClick={this.props.toggleTMXExportModal}>
                    <Icon name='export' className='s2' />
                    Export all to TMX
                  </ListGroupItem>
                </ListGroup>
              </Col>
              <Col xs={12} sm={5} lg={3}>
                <ListGroup>
                  <ListGroupItem id='reviewCriteria'
                    href='/admin/review'>
                    <Icon name='tick' className='s2' />
                    Review criteria
                  </ListGroupItem>
                </ListGroup>
              </Col>
            </Grid>
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
