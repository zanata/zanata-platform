import cx from 'classnames'
import React, { PropTypes } from 'react'
import { Icon } from 'zanata-ui'
import TransUnit from '../components/TransUnit'
import { connect } from 'react-redux'
import { getCurrentPagePhrasesFromState } from '../utils/filter-paging-util'

/**
 * The main content section showing the current page of TransUnit source,
 * status and translations.
 */
const MainContent = React.createClass({

  propTypes: {
    maximised: PropTypes.bool.isRequired,
    phrases: PropTypes.arrayOf(PropTypes.object).isRequired
  },

  render: function () {
    const { maximised, phrases } = this.props

    if (phrases.length === 0) {
      // TODO translate "No content"
      return (
        <div className="u-posCenterCenter u-textEmpty u-textCenter">
          <span className="u-sMB-1-4">
            <Icon name="translate" size="6" />
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
      return (
        <li key={phrase.id}>
          <TransUnit index={phrase.id} phrase={phrase} />
        </li>
      )
    })

    const className = cx('Editor-content TransUnit-container',
      { 'is-maximised': maximised })

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
      </main>
    )
  }
})

function mapStateToProps (state, ownProps) {
  const minimalPhrases = getCurrentPagePhrasesFromState(state)
  const detailPhrases = minimalPhrases.map(phrase => {
    const detail = state.phrases.detail[phrase.id]
    return detail || phrase
  })
  const maximised = !state.ui.panels.navHeader.visible
  return {
    context: state.context,
    maximised,
    phrases: detailPhrases
  }
}

export default connect(mapStateToProps)(MainContent)
