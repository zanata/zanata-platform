/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

import React from 'react'
import PropTypes from 'prop-types'
import { Button, ButtonToolbar } from 'react-bootstrap'
import Icon from '../../../components/Icon'

class ActTabActSelect extends React.Component {
  static propTypes = {
    icon: PropTypes.oneOf(['clock', 'comment', 'refresh', 'language']).isRequired,
    buttonName: PropTypes.oneOf(['All', 'Comments', 'Updates', 'Current', 'Source']).isRequired,
    buttonClass: PropTypes.oneOf(['Button--primary', 'Button--secondary'])
}

  render () {
    return (
          <Button className={'Button Button--small' +
          ' u-rounded ' + this.props.buttonClass}>
            <Icon name={this.props.icon} className="n1" /> {this.props.buttonName}
          </Button>
    )
  }
}

export default ActTabActSelect
