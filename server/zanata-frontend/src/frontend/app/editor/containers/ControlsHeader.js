import cx from 'classnames'
import IconButtonToggle from '../components/IconButtonToggle'
import Pager from '../components/Pager'
import TranslatingIndicator from '../components/TranslatingIndicator'
import TransUnitFilter from '../components/TransUnitFilter'
import React, { PropTypes } from 'react'
import { connect } from 'react-redux'
import { setSidebarVisibility } from '../actions'
import {
  toggleHeader,
  toggleKeyboardShortcutsModal
} from '../actions/headerActions'
import {
  resetStatusFilter,
  updateStatusFilter,
  firstPage,
  nextPage,
  previousPage,
  lastPage
} from '../actions/controlsHeaderActions'
import { toggleSuggestions } from '../actions/suggestions'
import { calculateMaxPageIndexFromState } from '../utils/filter-paging-util'

const { bool, func, number, shape } = PropTypes

/**
 * Header row with editor controls (filtering, paging, etc.)
 */
const ControlsHeader = React.createClass({

  propTypes: {
    actions: shape({
      resetFilter: func.isRequired,
      onFilterChange: func.isRequired,
      firstPage: func.isRequired,
      previousPage: func.isRequired,
      nextPage: func.isRequired,
      lastPage: func.isRequired,
      setSidebarVisibility: func.isRequired,
      toggleSuggestionPanel: func.isRequired,
      toggleKeyboardShortcutsModal: func.isRequired,
      toggleMainNav: func.isRequired
    }).isRequired,
    paging: shape({
      pageNumber: number.isRequired,
      pageCount: number
    }).isRequired,

    ui: shape({
      panels: shape({
        sidebar: shape({
          visible: bool.isRequired
        }).isRequired,
        suggestions: shape({
          visible: bool.isRequired
        }).isRequired
      }).isRequired,
      textFlowDisplay: shape({
        filter: shape({
          // FIXME should be able to derive this from the other 4
          all: bool.isRequired,
          approved: bool.isRequired,
          translated: bool.isRequired,
          needswork: bool.isRequired,
          untranslated: bool.isRequired
        }).isRequired
      }).isRequired,

      // DO NOT RENAME, the translation string extractor looks specifically
      // for gettextCatalog.getString when generating the translation template.
      gettextCatalog: shape({
        getString: func.isRequired
      }).isRequired
    }).isRequired,

    counts: shape({
      // TODO better to derive total from the others rather than duplicate
      total: number,
      approved: number,
      translated: number,
      needswork: number,
      untranslated: number
    })
  },

  toggleSidebarVisibility: function () {
    const { actions, ui } = this.props
    actions.setSidebarVisibility(!ui.panels.sidebar.visible)
  },

  render: function () {
    const { actions, counts, paging, ui } = this.props
    const { textFlowDisplay, gettextCatalog } = ui
    const transFilterProps = {
      actions,
      counts,
      filter: textFlowDisplay.filter,
      gettextCatalog
    }
    const pagerProps = {
      ...paging,
      actions,
      gettextCatalog
    }
    const navHeaderHidden = !ui.panels.navHeader.visible
    return (
      <nav className="u-bgHighest u-sPH-1-2 l--cf-of u-sizeHeight-1_1-2">
        <TranslatingIndicator gettextCatalog={gettextCatalog} />
        <div className="u-floatLeft">
          <TransUnitFilter {...transFilterProps} />
        </div>
        <div className="u-floatRight">
          <ul className="u-listHorizontal u-textCenter">
            <li className="u-sMV-1-4">
              <Pager {...pagerProps} />
            </li>
            <li className="u-sM-1-8">
              <IconButtonToggle
                icon="suggestions"
                title={this.props.ui.panels.suggestions.visible
                  ? gettextCatalog.getString('Hide suggestions panel')
                  : gettextCatalog.getString('Show suggestions panel')}
                onClick={this.props.actions.toggleSuggestionPanel}
                active={this.props.ui.panels.suggestions.visible} />

            </li>
            <li className="u-sM-1-8">
              <IconButtonToggle
                icon="info"
                title={this.props.ui.panels.sidebar.visible
                  ? gettextCatalog.getString('Hide sidebar')
                  : gettextCatalog.getString('Show sidebar')}
                onClick={this.toggleSidebarVisibility}
                active={this.props.ui.panels.sidebar.visible} />
            </li>
      {/* extra items from the angular template that were not being displayed
            <li ng-show="appCtrl.PRODUCTION">
              <button class="Link--neutral u-sizeHeight-1_1-2"
                title="{{'Details'|translate}}">
                <icon name="info" title="{{'Details'|translate}}"
                      class="u-sizeWidth-1_1-2"></icon>
              </button>
            </li>
            <li ng-show="appCtrl.PRODUCTION">
              <button class="Link--neutral u-sizeHeight-1_1-2"
              title="{{'Editor Settings'|translate}}">
                <icon name="settings" title="{{'Editor Settings'|translate}}"
                      class="u-sizeWidth-1_1-2"></icon>
              </button>
            </li>
      */}
            <li className="u-sm-hidden u-sM-1-8">
              <IconButtonToggle
                icon="keyboard"
                title={gettextCatalog.getString('Keyboard Shortcuts')}
                onClick={this.props.actions.toggleKeyboardShortcutsModal} />
            </li>
            <li className="u-sM-1-8">
              <IconButtonToggle
                icon="chevron-up-double"
                title={navHeaderHidden
                  ? gettextCatalog.getString('Show Menubar')
                  : gettextCatalog.getString('Hide Menubar')}
                onClick={this.props.actions.toggleMainNav}
                active={navHeaderHidden}
                className={cx({'is-rotated': navHeaderHidden})} />
            </li>
          </ul>
        </div>
      </nav>
    )
  }
})

function mapStateToProps (state) {
  const { actions, phrases, ui } = state
  const pageCount = calculateMaxPageIndexFromState(state) + 1
  const pageNumber = Math.min(pageCount, phrases.paging.pageIndex + 1)

  return {
    actions,
    counts: state.headerData.context.selectedDoc.counts,
    paging: {
      ...phrases.paging,
      pageCount: pageCount,
      pageNumber: pageNumber
    },
    ui
  }
}

function mapDispatchToProps (dispatch) {
  return {
    actions: {
      resetFilter: () => {
        dispatch(resetStatusFilter())
      },
      onFilterChange: (status) => {
        dispatch(updateStatusFilter(status))
      },
      firstPage: () => {
        dispatch(firstPage())
      },
      previousPage: () => {
        dispatch(previousPage())
      },
      nextPage: () => {
        dispatch(nextPage())
      },
      lastPage: () => {
        dispatch(lastPage())
      },
      setSidebarVisibility: (visible) => {
        dispatch(setSidebarVisibility(visible))
      },
      toggleSuggestionPanel: () => dispatch(toggleSuggestions()),
      toggleKeyboardShortcutsModal: () => {
        // TODO pahuang implement toggle keyboard shutcut modal
        // console.log('======== toggleKeyboardShortcutsModal')
        dispatch(toggleKeyboardShortcutsModal())
      },
      toggleMainNav: () => dispatch(toggleHeader())
    }
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(ControlsHeader)
