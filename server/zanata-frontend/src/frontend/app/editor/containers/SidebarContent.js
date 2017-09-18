import React from 'react'
import PropTypes from 'prop-types'
import { connect } from 'react-redux'
import { getShowSettings } from '../reducers'
import SettingsPanel from './SettingsPanel'
import TranslationInfoPanel from './TranslationInfoPanel'

/**
 * Chooses which content to display in the sidebar.
 */
export const SidebarContent = ({ showSettings }) => showSettings
  ? <SettingsPanel /> : <TranslationInfoPanel />

SidebarContent.propTypes = {
  showSettings: PropTypes.bool.isRequired
}

export default connect(
  state => ({ showSettings: getShowSettings(state) }))(SidebarContent)
