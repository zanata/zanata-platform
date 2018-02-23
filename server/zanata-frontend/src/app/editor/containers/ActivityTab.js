// @ts-nocheck
import React from 'react'
import * as PropTypes from 'prop-types'
import { connect } from 'react-redux'
import { Tab } from 'react-bootstrap'
import ActivitySelectList from '../components/ActivitySelectList'
import LanguageSelectList from '../components/LanguageSelectList'
import CommentBox from '../components/CommentBox'
import ActivityFeedItem from '../components/ActivityFeedItem'

class ActivityTab extends React.Component {

  static propTypes = {
    activityItems: PropTypes.arrayOf(PropTypes.shape({
      type: PropTypes.oneOf(['comment', 'revision']).isRequired,

      content: PropTypes.string.isRequired,
      lastModifiedTime: PropTypes.instanceOf(Date).isRequired,
      // TODO damason define type for status
      status: PropTypes.oneOf(['translated', 'fuzzy', 'approved', 'rejected',
        'untranslated']),
      user: PropTypes.shape({
        name: PropTypes.string.isRequired,
        imageUrl: PropTypes.string.isRequired
      }).isRequired
    })).isRequired,
    // eventKey prop to use for the bootstrap Tab
    eventKey: PropTypes.number.isRequired,
    postComment: PropTypes.func.isRequired,
    selectActivityTypeFilter: PropTypes.func.isRequired,
    selectLanguageFilter: PropTypes.func.isRequired,
    selectedActivites: ActivitySelectList.idType,
    selectedLanguages: LanguageSelectList.idType
  }

  render () {
    const {
      activityItems,
      eventKey,
      postComment,
      selectActivityTypeFilter,
      selectLanguageFilter,
      selectedActivites,
      selectedLanguages
    } = this.props

    return (
      <Tab eventKey={eventKey} title=''>
        <div className='SidebarEditor-wrapper' id='SidebarEditorTabs-pane2'>
          <ActivitySelectList selectItem={selectActivityTypeFilter}
            selected={selectedActivites} />
        </div>
        <div className='SidebarActivity'>
          <LanguageSelectList selectItem={selectLanguageFilter}
            selected={selectedLanguages} />
          <CommentBox postComment={postComment} />
          {activityItems.map((item, index) => (
            <ActivityFeedItem key={index} {...item} />))}
        </div>
      </Tab>
    )
  }
}

function mapStateToProps (_state) {
  // Dummy data. Kept for structure reference.
  // FIXME delete when the component is wired into the app.
  return {
    selectedActivites: 'all',
    selectedLanguages: 'current'
  }
}

function mapDispatchToProps (_dispatch) {
  // FIXME dummy actions
  /* eslint-disable no-console */
  return {
    postComment: (e) => console.log('postComment: ' + e),
    selectActivityTypeFilter:
      (e) => console.log('selectActivityTypeFilter: ' + e),
    selectLanguageFilter:
      (e) => console.log('selectLanguageFilter: ' + e)
  }
  /* eslint-enable no-console */
}

export default connect(mapStateToProps, mapDispatchToProps)(ActivityTab)
