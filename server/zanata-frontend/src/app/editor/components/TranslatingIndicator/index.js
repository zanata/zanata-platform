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
import * as PropTypes from 'prop-types'
import { Icon } from '../../../components'
import { FormattedMessage } from 'react-intl'

/**
 * Indicator that shows 'Translating' when the user is
 * translating the document. Presumably it will show
 * 'viewing' when that mode is available.
 */
class TranslatingIndicator extends React.Component {
  static propTypes = {
    permissions: PropTypes.shape({
      reviewer: PropTypes.bool.isRequired,
      translator: PropTypes.bool.isRequired
    }).isRequired
  }

  render () {
    const { permissions } = this.props
    // These need to be translated individually for context
    const reviewingMessage = (
      <FormattedMessage id='TranslatingIndicator.reviewing'
        description={'Indicator of editor reviewing mode'}
        defaultMessage='Reviewing' />
    )
    const translatingMessage = (
      <FormattedMessage id='TranslatingIndicator.translating'
        description={'Indicator of editor translating mode'}
        defaultMessage='Translating' />
    )
    const viewingMessage = (
      <FormattedMessage id='TranslatingIndicator.viewing'
        description={'Indicator of editor viewing mode'}
        defaultMessage='Viewing' />
    )
    const modeMessage = (
        permissions.reviewer
        ? reviewingMessage : permissions.translator
        ? translatingMessage
        : viewingMessage
    )
    const iconName = (
        permissions.reviewer
          ? 'review' : permissions.translator
          ? 'translate'
          : 'locked'
    )
    const iconStyle = (
      permissions.reviewer || permissions.translator
          ? 's2'
          : 's2 u-textDanger'
    )
    return (
      /* eslint-disable max-len */
      <button className='Link--neutral u-sPV-1-6 u-floatLeft u-sMR-1-4'>
        <Icon name={iconName} className={iconStyle} /> <span
          className='u-ltemd-hidden TransIndicator u-sMR-1-4'>
          {modeMessage}
        </span>
      </button>
      /* eslint-enable max-len */
    )
  }
}

export default TranslatingIndicator
