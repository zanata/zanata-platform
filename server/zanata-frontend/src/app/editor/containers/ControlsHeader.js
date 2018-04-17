// @ts-nocheck
/* eslint-disable spaced-comment */
// TODO convert to TS
import cx from 'classnames'
import EditorSearchInput from '../components/EditorSearchInput'
import IconButtonToggle from '../components/IconButtonToggle'
import Pager from '../components/Pager'
import TranslatingIndicator from '../components/TranslatingIndicator'
import PhraseStatusFilter from '../components/PhraseStatusFilter'
import React from 'react'
import * as PropTypes from 'prop-types'
import { connect } from 'react-redux'
import { injectIntl, intlShape, defineMessages } from 'react-intl'
import {
  getSidebarTab,
  getActivityVisible,
  getGlossaryVisible,
  getInfoPanelVisible,
  getKeyShortcutsVisible,
  getNavHeaderVisible,
  getShowSettings,
  getSuggestionsPanelVisible
} from '../reducers'
import { ACTIVITY_TAB, GLOSSARY_TAB } from '../reducers/ui-reducer'
import { toggleShowSettings } from '../actions'
import {
  toggleActivity,
  toggleGlossary,
  toggleInfoPanel,
  toggleHeader,
  toggleKeyboardShortcutsModal
} from '../actions/header-actions'
import { toggleSuggestions } from '../actions/suggestions-actions'

/* React-Intl I18n messages.
 * Consumed as Strings rather than FormattedMessage React Elements.
 * see: https://github.com/yahoo/react-intl/wiki/API#definemessages
 * and: https://github.com/yahoo/react-intl/wiki/API#injectintl */
export const messages = defineMessages({
  suggestHide: {
    id: 'ControlsHeader.suggestion.hide',
    defaultMessage: 'Hide suggestions panel'
  },
  suggestShow: {
    id: 'ControlsHeader.suggestion.show',
    defaultMessage: 'Show suggestions panel'
  },
  activityHide: {
    id: 'ControlsHeader.activity.hide',
    defaultMessage: 'Hide activity tab'
  },
  activityShow: {
    id: 'ControlsHeader.activity.show',
    defaultMessage: 'Show activity tab'
  },
  glossaryHide: {
    id: 'ControlsHeader.glossary.hide',
    defaultMessage: 'Hide glossary tab'
  },
  glossaryShow: {
    id: 'ControlsHeader.glossary.show',
    defaultMessage: 'Show glossary tab'
  },
  sidebarHide: {
    id: 'ControlsHeader.sidebar.hide',
    defaultMessage: 'Hide sidebar'
  },
  sidebarShow: {
    id: 'ControlsHeader.sidebar.Show',
    defaultMessage: 'Show sidebar'
  },
  keyShortcuts: {
    id: 'ControlsHeader.keyboardshortcuts',
    defaultMessage: 'Keyboard Shortcuts'
  },
  settings: {
    id: 'ControlsHeader.settings',
    defaultMessage: 'Settings'
  },
  menubarHide: {
    id: 'ControlsHeader.menubar.hide',
    defaultMessage: 'Hide Menubar'
  },
  menubarShow: {
    id: 'ControlsHeader.menubar.show',
    defaultMessage: 'Show Menubar'
  }
})

/**
 * Header row with editor controls (filtering, paging, etc.)
 */
export const ControlsHeader = ({
  intl,
  activitySelected,
  activityVisible,
  glossarySelected,
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
  }) => {
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
                ? intl.formatMessage(messages.suggestHide)
                : intl.formatMessage(messages.suggestShow)}
              onClick={toggleSuggestions}
              active={suggestionsVisible} />
          </li>
          <li className="u-sM-1-8">
            <IconButtonToggle
              icon="clock"
              title={activityVisible
                ? intl.formatMessage(messages.activityHide)
                : intl.formatMessage(messages.activityShow)}
              onClick={infoPanelVisible
                ? toggleActivity : activitySelected
                ? toggleInfoPanel : function () {
                  toggleInfoPanel()
                  toggleActivity()
                }}
              active={activityVisible}
            />
          </li>
          <li className="u-sM-1-8">
            <IconButtonToggle
              icon="glossary"
              title={glossaryVisible
                ? intl.formatMessage(messages.glossaryHide)
                : intl.formatMessage(messages.glossaryShow)}
              onClick={infoPanelVisible
                ? toggleGlossary : glossarySelected
                ? toggleInfoPanel : function () {
                  toggleInfoPanel()
                  toggleGlossary()
                }}
              active={glossaryVisible}
            />
          </li>
          <li className="u-sM-1-8">
            <IconButtonToggle
              icon="info"
              className="hide-sidebar-toggle"
              title={infoPanelVisible
                ? intl.formatMessage(messages.sidebarHide)
                : intl.formatMessage(messages.sidebarShow)}
              onClick={toggleInfoPanel}
              active={infoPanelVisible} />
          </li>
          <li className="u-sm-hidden u-sM-1-8">
            <IconButtonToggle
              icon="keyboard"
              title={intl.formatMessage(messages.keyShortcuts)}
              onClick={toggleKeyboardShortcutsModal}
              active={keyShortcutsVisible} />
          </li>
          <li className="u-sM-1-8">
            <IconButtonToggle
              icon="settings"
              title={intl.formatMessage(messages.settings)}
              onClick={toggleShowSettings}
              active={showSettings} />
          </li>
          <li className="u-sM-1-8">
            <IconButtonToggle
              icon="chevron-up-double"
              title={navHeaderVisible
                ? intl.formatMessage(messages.menubarHide)
                : intl.formatMessage(messages.menubarShow)}
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

ControlsHeader.propTypes = {
  intl: intlShape.isRequired,
  activitySelected: PropTypes.bool.isRequired,
  activityVisible: PropTypes.bool.isRequired,
  glossarySelected: PropTypes.bool.isRequired,
  glossaryVisible: PropTypes.bool.isRequired,
  infoPanelVisible: PropTypes.bool.isRequired,
  keyShortcutsVisible: PropTypes.bool.isRequired,
  navHeaderVisible: PropTypes.bool.isRequired,
  showSettings: PropTypes.bool.isRequired,
  suggestionsVisible: PropTypes.bool.isRequired,
  toggleKeyboardShortcutsModal: PropTypes.func.isRequired,
  toggleActivity: PropTypes.func.isRequired,
  toggleGlossary: PropTypes.func.isRequired,
  toggleInfoPanel: PropTypes.func.isRequired,
  toggleHeader: PropTypes.func.isRequired,
  toggleShowSettings: PropTypes.func.isRequired,
  toggleSuggestions: PropTypes.func.isRequired,
  permissions: PropTypes.shape({
    reviewer: PropTypes.bool.isRequired,
    translator: PropTypes.bool.isRequired
  }).isRequired
}

function mapStateToProps (state) {
  return {
    activitySelected: getSidebarTab(state) === ACTIVITY_TAB,
    glossarySelected: getSidebarTab(state) === GLOSSARY_TAB,
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
