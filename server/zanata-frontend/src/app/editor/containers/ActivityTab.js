// @ts-nocheck
import React from 'react'
import * as PropTypes from 'prop-types'
import { connect } from 'react-redux'
import ActivitySelectList from '../components/ActivitySelectList'
import LanguageSelectList from '../components/LanguageSelectList'
import CommentBox from '../components/CommentBox'
import ActivityFeedItem from '../components/ActivityFeedItem'
import { isEmpty } from 'lodash'

const DO_NOT_RENDER = undefined

class ActivityTab extends React.Component {

  static propTypes = {
    activityItems: PropTypes.arrayOf(PropTypes.shape({
      type: PropTypes.oneOf(['comment', 'revision']).isRequired,
      content: PropTypes.string.isRequired,
      lastModifiedTime: PropTypes.instanceOf(Date).isRequired,
      // TODO damason define type for status
      status: PropTypes.oneOf(['translated', 'needswork', 'approved',
      'rejected', 'untranslated']),
      user: PropTypes.shape({
        name: PropTypes.string.isRequired,
        imageUrl: PropTypes.string.isRequired
      }).isRequired
    })),
    postComment: PropTypes.func.isRequired,
    selectActivityTypeFilter: PropTypes.func.isRequired,
    selectLanguageFilter: PropTypes.func.isRequired,
    selectedActivites: ActivitySelectList.idType,
    selectedLanguages: LanguageSelectList.idType
  }

  render () {
    const {
      activityItems,
      postComment,
      selectActivityTypeFilter,
      selectLanguageFilter,
      selectedActivites,
      selectedLanguages
    } = this.props
    const ActivityItems = (isEmpty(activityItems))
      ? DO_NOT_RENDER
      : activityItems.map((item, index) => (
        <ActivityFeedItem key={index} {...item} />))
    return (
      <div>
        <div className='SidebarEditor-wrapper' id='SidebarEditorTabs-pane2'>
          <ActivitySelectList selectItem={selectActivityTypeFilter}
            selected={selectedActivites} />
        </div>
        <div className='SidebarActivity'>
          <LanguageSelectList selectItem={selectLanguageFilter}
            selected={selectedLanguages} />
          <CommentBox postComment={postComment} />
          {ActivityItems}
        </div>
      </div>
    )
  }
}

function mapStateToProps (_state) {
  // Dummy data. Kept for structure reference.
  // FIXME delete when the component is wired into the app.
  return {
    selectedLanguages: 'current'
  }
}

function mapDispatchToProps (_dispatch) {
  // FIXME dummy actions
  /* eslint-disable no-console */
  return {
    selectLanguageFilter:
      (e) => console.log('selectLanguageFilter: ' + e)
  }
  /* eslint-enable no-console */
}

export default connect(mapStateToProps, mapDispatchToProps)(ActivityTab)
