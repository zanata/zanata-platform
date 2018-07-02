// @ts-nocheck
import cx from 'classnames'
import React from 'react'
import * as PropTypes from 'prop-types'
import { Icon } from '../../components'
import TransUnit from '../components/TransUnit'
import ConcurrentModal from '../components/ConcurrentModal/index.tsx'
import { connect } from 'react-redux'
import { getCurrentPagePhraseDetail } from '../selectors'
import { getActivityVisible } from '../reducers'
import {
  fetchAllCriteria, toggleReviewModal
} from '../actions/review-trans-actions'
import {
  toggleConcurrentModal,
  saveResolveConflictLatest,
  saveResolveConflictOriginal
} from '../actions/phrases-actions'
import { getCriteria } from '../reducers/review-trans-reducer'
import { MINOR, MAJOR, CRITICAL } from '../utils/reject-trans-util'
import RejectTranslation from '../containers/RejectTranslation'
import { isUndefined } from 'lodash'

/**
 * The main content section showing the current page of TransUnit source,
 * status and translations.
 */
class MainContent extends React.Component {
  static propTypes = {
    activityVisible: PropTypes.bool.isRequired,
    maximised: PropTypes.bool.isRequired,
    saveResolveConflictLatest: PropTypes.func.isRequired,
    saveResolveConflictOriginal: PropTypes.func.isRequired,
    showConflictModal: PropTypes.bool.isRequired,
    showReviewModal: PropTypes.bool.isRequired,
    phrases: PropTypes.arrayOf(PropTypes.object).isRequired,
    toggleConcurrentModal: PropTypes.func.isRequired,
    toggleReviewModal: PropTypes.func.isRequired,
    fetchAllCriteria: PropTypes.func.isRequired,
    criteriaList: PropTypes.arrayOf(PropTypes.shape({
      commentRequired: PropTypes.bool.isRequired,
      description: PropTypes.string.isRequired,
      priority: PropTypes.oneOf([MINOR, MAJOR, CRITICAL]).isRequired
    })).isRequired,
    translationLocale: PropTypes.shape({
      id: PropTypes.string.isRequired
    }).isRequired,
    selectedPhraseId: PropTypes.number,
    projectSlug: PropTypes.string.isRequired,
    versionSlug: PropTypes.string.isRequired
  }
  componentDidMount () {
    this.props.fetchAllCriteria()
  }
  render () {
    const { maximised, phrases } = this.props
    if (phrases.length === 0) {
      // TODO translate "No content"
      return (
        <div className="u-posCenterCenter u-textEmpty u-textCenter">
          <span className="u-sMB-1-4">
            <Icon name="translate" className="s6" />
          </span>
          <p>No content</p>
        </div>
      )
    }
    const transUnits = phrases.map((phrase, _index) => {
      // FIXME maybe use phrase id, next page will have
      //       same index for different id. Not sure if
      //       that will matter though.

      // phrase is passed as a prop to avoid complexity of trying to get at
      // the phrase from state in mapDispatchToProps
      // TODO can just use a selector to get the phrase object, easy.
      return (
        <li key={phrase.id}>
          <TransUnit
            activityVisible={this.props.activityVisible}
            index={phrase.id}
            phrase={phrase}
            criteria={this.props.criteriaList}
            toggleConcurrentModal={this.props.toggleConcurrentModal}
            toggleRejectModal={this.props.toggleReviewModal}
            translationLocale={this.props.translationLocale}
            projectSlug={this.props.projectSlug}
            versionSlug={this.props.versionSlug} />
        </li>
      )
    })

    const className = cx('Editor-content TransUnit-container',
      { 'is-maximised': maximised })
    const selectedPhrase = this.props.phrases.find(
      x => x.id === this.props.selectedPhraseId)
    // Need to check whether phrase itself is undefined since the detail may not
    // yet have been fetched from the server.
    const selectedPhraseRevision = !isUndefined(selectedPhrase)
      ? selectedPhrase.revision
      : undefined
    // TODO scrollbar width container+child were not brought over
    //      from the angular code yet.
    return (
      <main role="main"
        id="editor-content"
        className={className}>
        <div className="Editor-translationsWrapper">
          <ul className="Editor-translations">
            {transUnits}
          </ul>
        </div>
        <ConcurrentModal
          closeConcurrentModal={this.props.toggleConcurrentModal}
          saveResolveConflictLatest={this.props.saveResolveConflictLatest}
          saveResolveConflictOriginal={this.props.saveResolveConflictOriginal}
          conflictData={selectedPhrase.conflict}
          show={this.props.showConflictModal}
        />
        <RejectTranslation
          show={this.props.showReviewModal}
          onHide={this.props.toggleReviewModal}
          transUnitID={this.props.selectedPhraseId}
          revision={selectedPhraseRevision}
          localeId={this.props.translationLocale.id}
          criteriaList={this.props.criteriaList}
          selectedPhrase={selectedPhrase} />
      </main>
    )
  }
}

function mapStateToProps (state, _ownProps) {
  // TODO replace with selector
  const maximised = !state.ui.panels.navHeader.visible
  const showReviewModal = state.review.showReviewModal
  const showConflictModal = state.phrases.showConflictModal
  return {
    activityVisible: getActivityVisible(state),
    maximised,
    showConflictModal: showConflictModal,
    showReviewModal: showReviewModal,
    criteriaList: getCriteria(state),
    phrases: getCurrentPagePhraseDetail(state),
    translationLocale: {
      id: state.context.lang
    },
    selectedPhraseId: state.phrases.selectedPhraseId,
    projectSlug: state.context.projectSlug,
    versionSlug: state.context.versionSlug
  }
}

function mapDispatchToProps (dispatch) {
  return {
    saveResolveConflictLatest: (latest, original) => dispatch(
      saveResolveConflictLatest(latest, original)),
    saveResolveConflictOriginal: (latest, original) => dispatch(
      saveResolveConflictOriginal(latest, original)),
    toggleConcurrentModal: () => dispatch(toggleConcurrentModal()),
    toggleReviewModal: () => dispatch(toggleReviewModal()),
    fetchAllCriteria: () => dispatch(fetchAllCriteria())
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(MainContent)
