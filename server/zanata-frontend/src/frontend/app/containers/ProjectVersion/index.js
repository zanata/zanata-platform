import React, {Component} from 'react'
import PropTypes from 'prop-types'
import {connect} from 'react-redux'
import { Button }
  from 'react-bootstrap'
import Helmet from 'react-helmet'
import TMMergeModal from './TMMergeModal'
import ProgressModal from '../../components/Modal/ProgressModal'

import {
  toggleTMMergeModal,
  queryTMMergeProgress
} from '../../actions/version-actions'

import {processStatusPropType} from '../../utils/prop-types-util'

/**
 * Root component for Project Version Page
 */
class ProjectVersion extends Component {
  static propTypes = {
    openTMMergeModal: PropTypes.func.isRequired,
    onCancelTMMerge: PropTypes.func.isRequired,
    params: PropTypes.shape({
      project: PropTypes.string.isRequired,
      version: PropTypes.string.isRequired
    }),
    TMMergeProcessStatus: PropTypes.shape({
      url: PropTypes.string.isRequired,
      percentageComplete: PropTypes.number.isRequired,
      statusCode: processStatusPropType.isRequired
    }),
    queryTMMergeProgress: PropTypes.func.isRequired
  }

  queryTMMergeProgress = () => {
    this.props.queryTMMergeProgress(this.props.TMMergeProcessStatus.url)
  }

  render () {
    const {
      openTMMergeModal,
      params,
      TMMergeProcessStatus,
      onCancelTMMerge
    } = this.props
    // depending on whether we are in progress, we display one of the modals
    const modal = TMMergeProcessStatus
      ? (
      <ProgressModal show onCancelOperation={onCancelTMMerge}
        processStatus={TMMergeProcessStatus}
        queryProgress={this.queryTMMergeProgress} />
      )
      : <TMMergeModal projectSlug={params.project}
        versionSlug={params.version} />
    return (
      <div className='page wide-view-theme' id='version'>
        <Helmet title='ProjectVersion' />
        <div className='center-block'>
          <h1>Project Version</h1>
          <Button
            onClick={openTMMergeModal}>
            Version
          </Button>
          {modal}
        </div>
      </div>
    )
  }
}

const mapStateToProps = (state) => {
  const processStatus = state.projectVersion.TMMerge.processStatus
  return {
    TMMergeProcessStatus: processStatus
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    openTMMergeModal: () => {
      dispatch(toggleTMMergeModal())
    },
    queryTMMergeProgress: (url) => {
      dispatch(queryTMMergeProgress(url))
    },
    onCancelTMMerge: () => {
      // TODO pahuang implement cancel operation
      console.warn('boom!!!')
    }
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(ProjectVersion)
