import React, {Component} from 'react'
import PropTypes from 'prop-types'
import {connect} from 'react-redux'
import TMMergeModal from './TMMergeModal'
import TMXExportModal from '../../components/TMX/TMXExportModal'

import {
  toggleTMMergeModal
} from '../../actions/version-actions'

import {
  showExportTMXModal
} from '../../actions/tmx-actions'

/**
 * Root component for Project Version Page
 */
class ProjectVersion extends Component {
  static propTypes = {
    toggleTMMergeModal: PropTypes.func.isRequired,
    toggleTMXExportModal: PropTypes.func.isRequired,
    params: PropTypes.shape({
      project: PropTypes.string.isRequired,
      version: PropTypes.string.isRequired
    })
  }

  render () {
    const { params } = this.props
    return (
      <div className='page wideView' id='sidebarVersion'>
        <div className='u-centerBlock'>
          <TMMergeModal projectSlug={params.project}
            versionSlug={params.version} />
          <TMXExportModal project={params.project}
            version={params.version} />
        </div>
      </div>
    )
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    toggleTMMergeModal: () => {
      dispatch(toggleTMMergeModal())
    },
    toggleTMXExportModal: (show) => {
      dispatch(showExportTMXModal(show))
    }
  }
}

export default connect(undefined, mapDispatchToProps)(ProjectVersion)
