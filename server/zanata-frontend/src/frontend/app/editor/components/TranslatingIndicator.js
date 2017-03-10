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

import { Icon, Row } from 'zanata-ui'
import React, { PropTypes } from 'react'

/**
 * Indicator that shows 'Translating' when the user is
 * translating the document. Presumably it will show
 * 'viewing' when that mode is available.
 */
const TranslatingIndicator = React.createClass({

  propTypes: {
    // DO NOT RENAME, the translation string extractor looks specifically
    // for gettextCatalog.getString when generating the translation template.
    gettextCatalog: PropTypes.shape({
      getString: PropTypes.func.isRequired
    }).isRequired
  },

  render: function () {
    return (
      <button className="Link--neutral u-sPV-1-4 u-floatLeft
                         u-sizeHeight-1_1-2 u-sMR-1-4">
        <Row>
          <Icon name="translate" size="2" /> <span
            className="u-ltemd-hidden u-sMR-1-4">
            {this.props.gettextCatalog.getString('Translating')}
          </span>
        </Row>
      </button>
    )
  }
})

export default TranslatingIndicator
