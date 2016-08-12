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

import cx from 'classnames'
import IconButton from './IconButton'
import React, { PropTypes } from 'react'

/**
 * An action button with an icon, title and background styling.
 *
 * Like IconButton but changes colour based on 'active' prop.
 *
 * props.className is applied to the icon
 */
const IconButtonToggle = React.createClass({

  propTypes: {
    icon: PropTypes.string.isRequired,
    title: PropTypes.string.isRequired,
    onClick: PropTypes.func.isRequired,
    active: PropTypes.bool.isRequired,
    disabled: PropTypes.bool,
    className: PropTypes.string
  },

  getDefaultProps: () => {
    return {
      active: false
    }
  },

  render: function () {
    const className = cx(this.props.className,
      'Button Button--snug u-roundish Button--invisible',
      { 'is-active': this.props.active })

    return (
      <IconButton
        {...this.props}
        className={className} />
    )
  }
})

export default IconButtonToggle
