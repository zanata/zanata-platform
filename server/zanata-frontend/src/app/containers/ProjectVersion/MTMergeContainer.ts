import {
  MTMergeModal, MTMergeModalParams, MTMergeModalStateProps, MTMergeModalDispatchProps
} from '../../components/MTMerge/MTMergeModal';
import {
  toggleMTMergeModal,
  mergeVersionFromMT,
  // Note: trying the existing TM process functions for now
  queryMTMergeProgress,
  cancelMTMergeRequest,
  currentMTMergeProcessFinished,
  // MTMergeOptions
} from '../../actions/version-actions'
import { connect } from 'react-redux'
import { Locale } from '../../utils/prop-types-util'
import { RootState } from '../../reducers/state'

const mapReduxStateToProps = (state: RootState): MTMergeModalStateProps => {
  const {
    projectVersion: {
      locales,
      MTMerge: {
        showMTMerge,
        processStatus,
        // queryStatus
      }
    }
  } = state
  return {
    showMTMerge,
    availableLocales: locales as Locale[],
    processStatus,
    // queryStatus
  }
}

const mapDispatchToProps = (dispatch: any): MTMergeModalDispatchProps => {
  return {
    // toggleMTMergeModal: () => {
    //   dispatch(toggleMTMergeModal())
    // },
    queryMTMergeProgress: (url: string) => {
      dispatch(queryMTMergeProgress(url))
    },
    onCancelMTMerge: (url: string) => {
      dispatch(cancelMTMergeRequest(url))
    },
    mergeProcessFinished: () => {
      dispatch(currentMTMergeProcessFinished())
    },
    onCancel: () => {
      dispatch(toggleMTMergeModal())
    },
    onSubmit: (projectSlug, versionSlug, mtMergeOptions) => {
      dispatch(mergeVersionFromMT(projectSlug, versionSlug, mtMergeOptions))
    }
  }
}

export default connect<MTMergeModalStateProps, MTMergeModalDispatchProps, MTMergeModalParams>(
  // try eliminating the cast with Redux 4
  mapReduxStateToProps as (state: any) => MTMergeModalStateProps,
  mapDispatchToProps)(MTMergeModal)
