import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import {connect} from 'react-redux'
import MTMerge from './MTMergeContainer'
import TMMergeModal from './TMMergeModal'
import TMXExportModal from '../../components/TMX/TMXExportModal'

import {
  toggleTMMergeModal
} from '../../actions/version-actions'

import {
  showExportTMXModal
} from '../../actions/tmx-actions'
// import { LocaleType } from '../../utils/prop-types-util'
import { fetchVersionLocales } from '../../actions/version-actions'

/**
 * Root component for Project Version Page
 */
class ProjectVersion extends Component {
  static propTypes = {
    // availableLocales: PropTypes.arrayOf(LocaleType),
    toggleTMMergeModal: PropTypes.func.isRequired,
    toggleTMXExportModal: PropTypes.func.isRequired,
    params: PropTypes.shape({
      project: PropTypes.string.isRequired,
      version: PropTypes.string.isRequired
    })
  }

  // componentDidMount () {
  //   this.props.fetchVersionLocales(
  //     this.props.projectSlug, this.props.versionSlug)
  // }

  render () {
    const { params } = this.props
    return (
      <div className='wideView' id='sidebarVersion'>
        <div className='u-centerBlock'>
          {
            // TODO not really used. It probably doesn't make sense to have
            // this (and the fetch above) if 'locales' is part of
            // ProjectVersionState
            // this.props.availableLocales &&
            <MTMerge
              allowMultiple={false}
              projectSlug={params.project}
              versionSlug={params.version}
            />}
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
// const mapStateToProps = (state) => ({
//   availableLocales: state.locales
// })
const mapStateToProps = undefined

export default connect(mapStateToProps, mapDispatchToProps)(ProjectVersion)
