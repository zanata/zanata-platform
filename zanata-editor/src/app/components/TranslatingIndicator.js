import Icon from './Icon'
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
        <Icon name="translate" /> <span
          className="u-ltemd-hidden u-sMR-1-4">
          {this.props.gettextCatalog.getString('Translating')}
        </span>
      </button>
    )
  }
})

export default TranslatingIndicator
