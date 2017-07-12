import React from 'react'
import PropTypes from 'prop-types'
import { Tab } from 'react-bootstrap'
import ActivitySelectList from '../components/ActivitySelectList'
import LanguageSelectList from '../components/LanguageSelectList'
import CommentBox from '../components/CommentBox'
import ActivityFeedItem from '../components/ActivityFeedItem'

class ActivityTab extends React.Component {

  static propTypes = {
    // eventKey prop to use for the bootstrap Tab
    eventKey: PropTypes.number.isRequired,
    selectActivityTypeFilter: PropTypes.func.isRequired,
    selectLanguageFilter: PropTypes.func.isRequired,
    selectedActivites: ActivitySelectList.idType,
    selectedLanguages: LanguageSelectList.idType
  }

  render () {
    const {
      eventKey,
      selectActivityTypeFilter,
      selectLanguageFilter,
      selectedActivites,
      selectedLanguages
    } = this.props
    // TODO damason add the selectItem function
    return (
      <Tab eventKey={eventKey} title="">
        <div className="sidebar-wrapper" id="tab2">
          <ActivitySelectList selectItem={selectActivityTypeFilter}
            selected={selectedActivites} />
        </div>
        <div className="sidebar-activity">
          <LanguageSelectList selectItem={selectLanguageFilter}
            selected={selectedLanguages} />
          <CommentBox />
          <ActivityFeedItem
            icon="refresh"
            username="Reviewdude"
            status="u-textHighlight"
            message="approved a translation"
            wellStatus="well-approved"
            content="নাম"
          />
          <ActivityFeedItem
            icon="refresh"
            username="Kathryn"
            status="u-textSuccess"
            message="created a translated revision"
            wellStatus="well-translated"
            content="নাম"
          />
          <ActivityFeedItem
            icon="comment"
            username="Kathryn"
            message="commented on a translation"
            content="I have no idea what I am doing"
          />
          <ActivityFeedItem
            icon="refresh"
            username="Kathryn"
            status="u-textUnsure"
            message="created a fuzzy translation"
            wellStatus="well-fuzzy"
            content="নাম"
          />
          <ActivityFeedItem
            icon="refresh"
            username="Reviewdude"
            status="u-textWarning"
            message="rejected a translation"
            wellStatus="well-rejected"
            content="নাম"
          />
        </div>
      </Tab>
    )
  }
}

export default ActivityTab
