import cx from 'classnames'
import React from 'react'
import * as PropTypes from 'prop-types'
import { Icon } from '../../components'
import TransUnit from '../components/TransUnit'
import { connect } from 'react-redux'
import { getCurrentPagePhraseDetail } from '../selectors'
import { fetchAllCriteria } from '../actions/review-trans-actions'
import { getCriteria } from '../reducers/review-trans-reducer'
import { MINOR, MAJOR, CRITICAL } from '../utils/reject-trans-util'
import RejectTranslationModal from '../containers/RejectTranslationModal'
import { isUndefined } from 'lodash'
/**
 * The main content section showing the current page of TransUnit source,
 * status and translations.
 */
class MainContent extends React.Component {
  static propTypes = {
    maximised: PropTypes.bool.isRequired,
    phrases: PropTypes.arrayOf(PropTypes.object).isRequired,
    fetchAllCriteria: PropTypes.func.isRequired,
    criteria: PropTypes.arrayOf(PropTypes.shape({
      editable: PropTypes.bool.isRequired,
      description: PropTypes.string.isRequired,
      priority: PropTypes.oneOf([MINOR, MAJOR, CRITICAL]).isRequired
    })).isRequired,
    translationLocale: PropTypes.shape({
      id: PropTypes.string.isRequired
    }).isRequired,
    selectedPhraseId: PropTypes.number
  }
  constructor (props) {
    super(props)
    this.state = {
      showRejectModal: false
    }
  }
  componentDidMount () {
    this.props.fetchAllCriteria()
  }
  toggleRejectModal = () => {
    this.setState(prevState => ({
      showRejectModal: !prevState.showRejectModal
    }))
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

    const transUnits = phrases.map((phrase, index) => {
      // FIXME maybe use phrase id, next page will have
      //       same index for different id. Not sure if
      //       that will matter though.

      // phrase is passed as a prop to avoid complexity of trying to get at
      // the phrase from state in mapDispatchToProps
      // TODO can just use a selector to get the phrase object, easy.
      return (
        <li key={phrase.id}>
          <TransUnit
            index={phrase.id}
            phrase={phrase}
            criteria={this.props.criteria}
            toggleRejectModal={this.toggleRejectModal} />
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
        <RejectTranslationModal
          show={this.state.showRejectModal}
          onHide={this.toggleRejectModal}
          transUnitID={this.props.selectedPhraseId}
          revision={selectedPhraseRevision}
          localeId={this.props.translationLocale.id}
          criteria={this.props.criteria}
          selectedPhrase={selectedPhrase} />
      </main>
    )
  }
}

function mapStateToProps (state, ownProps) {
  // TODO replace with selector
  const maximised = !state.ui.panels.navHeader.visible
  return {
    maximised,
    criteria: getCriteria(state),
    phrases: getCurrentPagePhraseDetail(state),
    translationLocale: {
      id: state.context.lang
    },
    selectedPhraseId: state.phrases.selectedPhraseId
  }
}

function mapDispatchToProps (dispatch) {
  return {
    fetchAllCriteria: () => dispatch(fetchAllCriteria())
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(MainContent)
