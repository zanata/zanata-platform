// @ts-nocheck
/* eslint-disable spaced-comment */
/* @flow */ // TODO convert to TS
import cx from 'classnames'
import EditorSearchInput from '../components/EditorSearchInput'
import IconButtonToggle from '../components/IconButtonToggle'
import Pager from '../components/Pager'
import TranslatingIndicator from '../components/TranslatingIndicator'
import PhraseStatusFilter from '../components/PhraseStatusFilter'
import React from 'react'
import { connect } from 'react-redux'
import {
  getActivityVisible,
  getGlossaryVisible,
  getInfoPanelVisible,
  getKeyShortcutsVisible,
  getNavHeaderVisible,
  getShowSettings,
  getSuggestionsPanelVisible
} from '../reducers'
import { toggleShowSettings } from '../actions'
import {
  toggleActivity,
  toggleGlossary,
  toggleInfoPanel,
  toggleHeader,
  toggleKeyboardShortcutsModal
} from '../actions/header-actions'
import { toggleSuggestions } from '../actions/suggestions-actions'

/*::
type props = {
  +toggleKeyboardShortcutsModal: () => void,
  +toggleActivity: () => void,
  +toggleGlossary: () => void,
  +toggleInfoPanel: () => void,
  +toggleHeader: () => void,
  +toggleShowSettings: () => void,
  +toggleSuggestions: () => void,

  +glossaryVisible: bool,
  +infoPanelVisible: bool,
  +keyShortcutsVisible: bool,
  +navHeaderVisible: bool,
  +showSettings: bool,
  +suggestionsVisible: bool,

  +gettextCatalog: {
    getString: (string) => string
  }
}
*/

/**
 * Header row with editor controls (filtering, paging, etc.)
 */
export const ControlsHeader = ({
  /* eslint-disable react/prop-types */
  activityVisible,
  glossaryVisible,
  infoPanelVisible,
  keyShortcutsVisible,
  navHeaderVisible,
  showSettings,
  suggestionsVisible,
  toggleKeyboardShortcutsModal,
  toggleActivity,
  toggleGlossary,
  toggleInfoPanel,
  toggleHeader,
  toggleShowSettings,
  toggleSuggestions,
  gettextCatalog,
  permissions
  /* eslint-enable react/prop-types */
 }/*: props*/) => {
  return (
    /* eslint-disable max-len */
    <nav className="flex flex-wrapper u-bgHighest u-sPH-1-2 l--cf-of">
      <TranslatingIndicator permissions={permissions} />
      <div className="u-floatLeft controlHeader-left Input">
        <PhraseStatusFilter /></div>
      {/* FIXME move InputEditorSearch into component. Layout component should
                not have to know the internals of how the component is
                styled. */}
      <div className="u-floatLeft InputEditorSearch">
        <EditorSearchInput />
      </div>
      <div className="u-floatRight controlHeader-right">
        <ul className="u-listHorizontal u-textCenter">
          <li className="u-sMV-1-4">
            <Pager />
          </li>
          <li className="u-sM-1-8">
            <IconButtonToggle
              icon="suggestions"
              title={suggestionsVisible
                ? gettextCatalog.getString('Hide suggestions panel')
                : gettextCatalog.getString('Show suggestions panel')}
              onClick={toggleSuggestions}
              active={suggestionsVisible} />
          </li>
          <li className="u-sM-1-8">
            <IconButtonToggle
              icon="clock"
              title={activityVisible ? 'Hide activity' : 'Show activity'}
              onClick={toggleActivity}
              active={activityVisible}
            />
          </li>
          <li className="u-sM-1-8">
            <IconButtonToggle
              icon="glossary"
              title={glossaryVisible ? 'Hide glossary' : 'Show glossary'}
              onClick={toggleGlossary}
              active={glossaryVisible}
            />
          </li>
          <li className="u-sM-1-8">
            <IconButtonToggle
              icon="info"
              className="hide-sidebar-toggle"
              title={infoPanelVisible
                ? gettextCatalog.getString('Hide sidebar')
                : gettextCatalog.getString('Show sidebar')}
              onClick={toggleInfoPanel}
              active={infoPanelVisible} />
          </li>
          {/* extra items from the angular template that were not being
           displayed
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
              onClick={toggleKeyboardShortcutsModal}
              active={keyShortcutsVisible} />
          </li>
          <li className="u-sM-1-8">
            <IconButtonToggle
              icon="settings"
              title={gettextCatalog.getString('Settings')}
              onClick={toggleShowSettings}
              active={showSettings} />
          </li>
          <li className="u-sM-1-8">
            <IconButtonToggle
              icon="chevron-up-double"
              title={navHeaderVisible
                ? gettextCatalog.getString('Hide Menubar')
                : gettextCatalog.getString('Show Menubar')}
              onClick={toggleHeader}
              active={!navHeaderVisible}
              className={cx({'is-rotated': !navHeaderVisible})} />
          </li>
        </ul>
      </div>
    </nav>
    /* eslint-enable max-len */
  )
}

function mapStateToProps (state) {
  const { ui: { gettextCatalog } } = state
  return {
    activityVisible: getActivityVisible(state),
    glossaryVisible: getGlossaryVisible(state),
    infoPanelVisible: getInfoPanelVisible(state),
    keyShortcutsVisible: getKeyShortcutsVisible(state),
    navHeaderVisible: getNavHeaderVisible(state),
    suggestionsVisible: getSuggestionsPanelVisible(state),
    showSettings: getShowSettings(state),
    gettextCatalog,
    permissions: state.headerData.permissions
  }
}

const mapDispatchToProps = {
  toggleShowSettings,
  toggleActivity,
  toggleGlossary,
  toggleInfoPanel,
  toggleSuggestions,
  toggleKeyboardShortcutsModal,
  toggleHeader
}

export default connect(mapStateToProps, mapDispatchToProps)(ControlsHeader)
