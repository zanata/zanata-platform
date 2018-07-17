import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import {connect} from 'react-redux'
import Notification from 'antd/lib/notification'
import MTMerge from './MTMergeContainer'
import TMMergeModal from './TMMergeModal'
import TMXExportModal from '../../components/TMX/TMXExportModal'

import {
  toggleTMMergeModal
} from '../../actions/version-actions'

import {
  showExportTMXModal
} from '../../actions/tmx-actions'
import { fetchVersionLocales } from '../../actions/version-actions'

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
    }),
    notification: PropTypes.node
  }

  // @ts-ignore any
  componentDidUpdate (prevProps, prevState) {
    const { notification } = this.props
    if (notification && prevProps.notification !== notification) {
      // @ts-ignore any
      Notification[notification.severity]({
        message: notification.message,
        description: notification.description,
        duration: notification.duration
      })
    }
  }

  render () {
    const { params } = this.props
    return (
      <div className='wideView' id='sidebarVersion'>
        <div className='u-centerBlock'>
          <MTMerge
            allowMultiple={false}
            projectSlug={params.project}
            versionSlug={params.version}
          />
          <TMMergeModal
            // @ts-ignore
            projectSlug={params.project}
            versionSlug={params.version} />
          <TMXExportModal
            // @ts-ignore
            project={params.project}
            version={params.version} />
        </div>
      </div>
    )
  }
}

// @ts-ignore
const mapDispatchToProps = (dispatch) => {
  return {
    // @ts-ignore any
    fetchVersionLocales: (project, version) => {
      dispatch(fetchVersionLocales(project, version))
    },
    toggleTMMergeModal: () => {
      dispatch(toggleTMMergeModal())
    },
    // @ts-ignore any
    toggleTMXExportModal: (show) => {
      // @ts-ignore
      dispatch(showExportTMXModal(show))
    }
  }
}

// @ts-ignore any
const mapStateToProps = (state) => {
  return { notification: state.projectVersion.notification }
}

export default connect(mapStateToProps, mapDispatchToProps)(ProjectVersion)
