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
import { injectIntl, defineMessages } from 'react-intl'
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
}
*/

/* React-Intl I18n messages.
 * Consumed as Strings rather than FormattedMessage React Elements.
 * see: https://github.com/yahoo/react-intl/wiki/API#definemessages
 * and: https://github.com/yahoo/react-intl/wiki/API#injectintl */
export const messages = defineMessages({
  suggestHide: {
    id: 'Controlsheader.suggestion.hide',
    defaultMessage: 'Hide suggestions panel'
  },
  suggestShow: {
    id: 'Controlsheader.suggestion.show',
    defaultMessage: 'Show suggestions panel'
  },
  activityHide: {
    id: 'Controlsheader.activity.hide',
    defaultMessage: 'Hide activity tab'
  },
  activityShow: {
    id: 'Controlsheader.activity.show',
    defaultMessage: 'Show activity tab'
  },
  glossaryHide: {
    id: 'Controlsheader.glossary.hide',
    defaultMessage: 'Hide glossary tab'
  },
  glossaryShow: {
    id: 'Controlsheader.glossary.show',
    defaultMessage: 'Show glossary tab'
  },
  sidebarHide: {
    id: 'Controlsheader.sidebar.hide',
    defaultMessage: 'Hide sidebar'
  },
  sidebarShow: {
    id: 'Controlsheader.sidebar.Show',
    defaultMessage: 'Show sidebar'
  },
  keyShortcuts: {
    id: 'Controlsheader.keyboardshortcuts',
    defaultMessage: 'Keyboard Shortcuts'
  },
  settings: {
    id: 'Controlsheader.settings',
    defaultMessage: 'Settings'
  },
  menubarHide: {
    id: 'Controlsheader.menubar.hide',
    defaultMessage: 'Hide Menubar'
  },
  menubarShow: {
    id: 'Controlsheader.menubar.show',
    defaultMessage: 'Show Menubar'
  }
})

/**
 * Header row with editor controls (filtering, paging, etc.)
 */
export const ControlsHeader = ({
  /* eslint-disable react/prop-types */
  intl,
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
                ? intl.formatMessage({id: 'Controlsheader.suggestion.hide'})
                : intl.formatMessage({id: 'Controlsheader.suggestion.show'})}
              onClick={toggleSuggestions}
              active={suggestionsVisible} />
          </li>
          <li className="u-sM-1-8">
            <IconButtonToggle
              icon="clock"
              title={activityVisible
                ? intl.formatMessage({id: 'Controlsheader.activity.hide'})
                : intl.formatMessage({id: 'Controlsheader.activity.show'})}
              onClick={infoPanelVisible ? toggleActivity : toggleInfoPanel}
              active={activityVisible}
            />
          </li>
          <li className="u-sM-1-8">
            <IconButtonToggle
              icon="glossary"
              title={glossaryVisible
                ? intl.formatMessage({id: 'Controlsheader.glossary.hide'})
                : intl.formatMessage({id: 'Controlsheader.glossary.show'})}
              onClick={infoPanelVisible ? toggleGlossary : toggleInfoPanel}
              active={glossaryVisible}
            />
          </li>
          <li className="u-sM-1-8">
            <IconButtonToggle
              icon="info"
              className="hide-sidebar-toggle"
              title={infoPanelVisible
                ? intl.formatMessage({id: 'Controlsheader.sidebar.hide'})
                : intl.formatMessage({id: 'Controlsheader.sidebar.Show'})}
              onClick={toggleInfoPanel}
              active={infoPanelVisible} />
          </li>
          <li className="u-sm-hidden u-sM-1-8">
            <IconButtonToggle
              icon="keyboard"
              title={intl.formatMessage({id: 'Controlsheader.keyboardshortcuts'})}
              onClick={toggleKeyboardShortcutsModal}
              active={keyShortcutsVisible} />
          </li>
          <li className="u-sM-1-8">
            <IconButtonToggle
              icon="settings"
              title={intl.formatMessage({id: 'Controlsheader.settings'})}
              onClick={toggleShowSettings}
              active={showSettings} />
          </li>
          <li className="u-sM-1-8">
            <IconButtonToggle
              icon="chevron-up-double"
              title={navHeaderVisible
                ? intl.formatMessage({id: 'Controlsheader.menubar.hide'})
                : intl.formatMessage({id: 'Controlsheader.menubar.show'})}
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
  return {
    activityVisible: getActivityVisible(state),
    glossaryVisible: getGlossaryVisible(state),
    infoPanelVisible: getInfoPanelVisible(state),
    keyShortcutsVisible: getKeyShortcutsVisible(state),
    navHeaderVisible: getNavHeaderVisible(state),
    suggestionsVisible: getSuggestionsPanelVisible(state),
    showSettings: getShowSettings(state),
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

export default connect(mapStateToProps, mapDispatchToProps)(
  injectIntl(ControlsHeader))
