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
        <div className='wideView' id='admin'>
          <div className='u-centerBlock'>
            <p>You are not authorised to access to this page</p>
          </div>
        </div>
      )
    }
    return (
      <div className='page wideView' id='admin'>
        <div className='u-centerBlock'>
          <Grid>
            <h1>Administration</h1>
            <Col xs={12} sm={6} md={4} lg={3}>
              <ListGroup>
                <ListGroupItem href='#'>
                  <Icon name='settings' className='s3' />
                  Server configuration
                </ListGroupItem>
              </ListGroup>
            </Col>
            <Col xs={12} sm={6} md={4} lg={3}>
              <ListGroup>
                <ListGroupItem href='#'>
                  <Icon name='user' className='s3' />
                  Manage users
                </ListGroupItem>
              </ListGroup>
            </Col>
            <Col xs={12} sm={6} md={4} lg={3}>
              <ListGroup>
                <ListGroupItem href='#'>
                  <Icon name='users' className='s3' />
                  Manage roles
                </ListGroupItem>
              </ListGroup>
            </Col>
            <Col xs={12} sm={6} md={4} lg={3}>
              <ListGroup>
                <ListGroupItem href='#'>
                  <Icon name='search' className='s3' />
                  Manage search
                </ListGroupItem>
              </ListGroup>
            </Col>
            <Col xs={12} sm={6} md={4} lg={3}>
              <ListGroup>
                <ListGroupItem href='#'>
                  <Icon name='assign' className='s3' />
                  Role assignment rules
                </ListGroupItem>
              </ListGroup>
            </Col>
            <Col xs={12} sm={6} md={4} lg={3}>
              <ListGroup>
                <ListGroupItem href='#'>
                  <Icon name='tm' className='s3' />
                  Translation memory
                </ListGroupItem>
              </ListGroup>
            </Col>
            <Col xs={12} sm={6} md={4} lg={3}>
              <ListGroup>
                <ListGroupItem href='#'>
                  <Icon name='clock' className='s3' />
                  Process manager
                </ListGroupItem>
              </ListGroup>
            </Col>
            <Col xs={12} sm={6} md={4} lg={3}>
              <ListGroup>
                <ListGroupItem href='#'>
                  <Icon name='piestats' className='s3' />
                  Overall statistics
                </ListGroupItem>
              </ListGroup>
            </Col>
            <Col xs={12} sm={6} md={4} lg={3}>
              <ListGroup>
                <ListGroupItem href='#'>
                  <Icon name='servmon' className='s3' />
                  Server monitoring
                </ListGroupItem>
              </ListGroup>
            </Col>
            <Col xs={12} sm={6} md={4} lg={3}>
              <ListGroup>
                <ListGroupItem href='#'>
                  <Icon name='document' className='s3' />
                  Cache statistics
                </ListGroupItem>
              </ListGroup>
            </Col>
            <Col xs={12} sm={6} md={4} lg={3}>
              <ListGroup>
                <ListGroupItem href='#'>
                  <Icon name='export' className='s3' />
                  Export all to TMX
                </ListGroupItem>
              </ListGroup>
            </Col>
            <Col xs={12} sm={6} md={4} lg={3}>
              <ListGroup>
                <ListGroupItem href='#'>
                  <Icon name='tick' className='s3' />
                  Review criteria
                </ListGroupItem>
              </ListGroup>
            </Col>
          </Grid>
          <TMXExportModal />
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
