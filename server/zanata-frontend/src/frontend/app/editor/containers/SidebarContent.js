import React from 'react'
import PropTypes from 'prop-types'
import { connect } from 'react-redux'
import { getShowSettings } from '../reducers'
import SidebarSettings from './SidebarSettings'
import TranslationInfoPanel from './TranslationInfoPanel'

/**
 * Chooses which content to display in the sidebar.
 */
export const SidebarContent = ({ showSettings }) => showSettings
  ? <SidebarSettings /> : <TranslationInfoPanel />

SidebarContent.propTypes = {
  showSettings: PropTypes.bool.isRequired
}

export default connect(
  state => ({ showSettings: getShowSettings(state) }))(SidebarContent)

// Changed here in other branch, copy to appropriate place:

// Use this when the activity tab is activated
// import ActivityTab from './ActivityTab'

// Note: space removed before parens
// <span>({this.props.glossaryCount})</span>

// Use this when activity tab is activated
// const activityTitle = (
//   <span>
//     <Icon name="clock" className="s1 gloss-tab-svg" />
//     <span className="hide-md">Activity</span>
//   </span>
// )

//  {/* Use this when activity tab is activated
//    <ActivityTab eventKey={2} title={activityTitle} /> */}

