import {
  MTMergeModal, MTMergeModalParams, MTMergeModalStateProps, MTMergeModalDispatchProps, MTTranslationStatus
} from '../../components/MTMerge/MTMergeModal';
// import {
//   toggleMTMergeModal,
//   mergeVersionFromMT,
//   // Note: trying the existing TM process functions for now
//   queryTMMergeProgress as queryMTMergeProgress,
//   cancelTMMergeRequest as cancelMTMergeRequest,
//   currentMTMergeProcessFinished,
//   MTMergeOptions
// } from '../../actions/version-actions'
import { connect } from 'react-redux'
import { LocaleId, Locale } from '../../utils/prop-types-util';
import { TopLevelState } from '../../reducers/state';

const mapReduxStateToProps = (state: TopLevelState): MTMergeModalStateProps => {
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

const mapDispatchToProps = (_dispatch: any): MTMergeModalDispatchProps => {
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
      // TODO
    },
    onSubmit: (_selectedLocales: LocaleId[], _saveAs: MTTranslationStatus, _overwriteFuzzy: boolean) => {
      // TODO
      // dispatch(mergeVersionFromMT(projectSlug, versionSlug, mtMergeOptions))
    }
  }
}

export default connect<MTMergeModalStateProps, MTMergeModalDispatchProps, MTMergeModalParams>(
  // try eliminating the cast with Redux 4
  mapReduxStateToProps as (state: any) => MTMergeModalStateProps,
  mapDispatchToProps)(MTMergeModal)
