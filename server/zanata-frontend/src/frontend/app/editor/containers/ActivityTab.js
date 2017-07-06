import React from 'react'
import PropTypes from 'prop-types'
import { Tab } from 'react-bootstrap'
import ActivitySelectList from '../components/ActivitySelectList'
import LanguageSelectList from '../components/LanguageSelectList'
import CommentBox from '../components/CommentBox'
import ActivityFeed from '../components/ActivityFeed'

class ActivityTab extends React.Component {

  static propTypes = {
    // eventKey prop to use for the bootstrap Tab
    eventKey: PropTypes.number.isRequired
  }

  render () {
    const { eventKey } = this.props
    return (
      <Tab eventKey={eventKey} title="">
        <div className="sidebar-wrapper" id="tab2">
          <ActivitySelectList />
        </div>
        <div className="sidebar-activity">
          <LanguageSelectList />
          <CommentBox />
          <ActivityFeed />
        </div>
      </Tab>
    )
  }
}

export default ActivityTab
