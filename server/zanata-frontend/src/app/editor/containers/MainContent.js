import cx from 'classnames'
import * as React from 'react'
import * as PropTypes from 'prop-types'
import { Icon } from '../../components'
import TransUnit from '../components/TransUnit'
import { connect } from 'react-redux'
import { getCurrentPagePhraseDetail } from '../selectors'

/**
 * The main content section showing the current page of TransUnit source,
 * status and translations.
 */
class MainContent extends React.Component {
  static propTypes = {
    maximised: PropTypes.bool.isRequired,
    phrases: PropTypes.arrayOf(PropTypes.object).isRequired
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
}

function mapStateToProps (state, ownProps) {
  // TODO replace with selector
  const maximised = !state.ui.panels.navHeader.visible

  return {
    maximised,
    phrases: getCurrentPagePhraseDetail(state)
  }
}

export default connect(mapStateToProps)(MainContent)
