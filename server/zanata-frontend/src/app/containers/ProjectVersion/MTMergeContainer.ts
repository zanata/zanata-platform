import {
  MTMergeModal, MTMergeModalParams, MTMergeModalStateProps, MTMergeModalDispatchProps
} from '../../components/MTMerge/MTMergeModal';
import {
  toggleMTMergeModal,
  mergeVersionFromMT,
  // Note: trying the existing TM process functions for now
  // queryTMMergeProgress as queryMTMergeProgress,
  // cancelTMMergeRequest as cancelMTMergeRequest,
  // currentMTMergeProcessFinished,
  // MTMergeOptions
} from '../../actions/version-actions'
import { connect } from 'react-redux'
import { Locale } from '../../utils/prop-types-util';
import { RootState } from '../../reducers/state';

const mapReduxStateToProps = (state: RootState): MTMergeModalStateProps => {
  const {
    projectVersion: {
      locales,
      // notification,
      MTMerge: {
        showMTMerge,
        // triggered,
        // projectVersions,
        // processStatus,
        // queryStatus
      }
    }
  } = state
  return {
    showMTMerge,
    // triggered,
    availableLocales: locales as Locale[],
    // projectVersions,
    // notification,
    // processStatus,
    // queryStatus
  }
}

const mapDispatchToProps = (dispatch: any): MTMergeModalDispatchProps => {
  return {
    // toggleMTMergeModal: () => {
    //   dispatch(toggleMTMergeModal())
    // },
    // queryMTMergeProgress: (url: string) => {
    //   dispatch(queryMTMergeProgress(url))
    // },
    // onCancelMTMerge: (url: string) => {
    //   dispatch(cancelMTMergeRequest(url))
    // },
    // mergeProcessFinished: () => {
    //   dispatch(currentMTMergeProcessFinished())
    // },
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
