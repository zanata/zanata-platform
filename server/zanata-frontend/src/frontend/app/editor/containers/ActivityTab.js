import React from 'react'
import PropTypes from 'prop-types'
import { Tab } from 'react-bootstrap'
import ActTabActSelect from '../components/ActTabActSelect'
import ActTabLangSelect from '../components/ActTabLangSelect'
import ActTabCommentBox from '../components/ActTabCommentBox'
import ActTabFeed from '../components/ActTabFeed'

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
          <ActTabLangSelect />
        </div>
        <div className="sidebar-activity">
          <ActTabActSelect />
          <ActTabCommentBox />
          <ActTabFeed />
        </div>
      </Tab>
    )
  }
}

export default ActivityTab
