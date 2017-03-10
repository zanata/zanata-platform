import cx from 'classnames'
import ControlsHeader from '../ControlsHeader'
import NavHeader from '../NavHeader'
import ProgressBar from '../../components/ProgressBar'
import { connect } from 'react-redux'
import React, { PropTypes } from 'react'
import ZanataLogoLoader from '../ZanataLogoLoader'

/**
 * Header for navigation and control of the editor
 */
const EditorHeader = React.createClass({

  propTypes: {
    navHeaderVisible: PropTypes.bool.isRequired,
    counts: PropTypes.shape({
      total: PropTypes.number,
      approved: PropTypes.number,
      translated: PropTypes.number,
      needswork: PropTypes.number,
      rejected: PropTypes.number,
      untranslated: PropTypes.number
    })
  },

  render: function () {
    const className = cx('Header', 'Editor-header',
        { 'is-minimised': !this.props.navHeaderVisible })
    return (
      <div id="editor-header">
        <ZanataLogoLoader />
        <header role="banner"
          className={className}
          focus-on="editor-header">
          <NavHeader />
          <ControlsHeader />
          <ProgressBar
            size="small"
            counts={this.props.counts} />
        </header>
      </div>
    )
  }
})

function mapStateToProps (state) {
  return {
    navHeaderVisible: state.ui.panels.navHeader.visible,
    counts: state.headerData.context.selectedDoc.counts,
    ui: state.ui
  }
}

export default connect(mapStateToProps)(EditorHeader)
