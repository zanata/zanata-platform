import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { connect } from 'react-redux'
import Helmet from 'react-helmet'
import { isEmpty, map } from 'lodash'
import {
  profileInitialLoad,
  dateRangeChanged,
  filterUpdate,
  selectDayChanged
} from '../../actions/profile-actions'
import RecentContributions from './RecentContributions'
import { Notification, Icon, LoaderText } from '../../components'
import { getLanguageUrl } from '../../utils/UrlHelper'
import { isLoggedIn } from '../../config'

/**
 * Root component for user profile page
 */
class UserProfile extends Component {
  static propTypes = {
    params: PropTypes.object,
    user: PropTypes.object,
    notification: PropTypes.object,
    loading: PropTypes.bool,
    matrixForAllDays: PropTypes.array,
    wordCountsForSelectedDayFilteredByContentState: PropTypes.array,
    wordCountsForEachDayFilteredByContentState: PropTypes.array,
    contentStateOption: PropTypes.string,
    selectedDay: PropTypes.string,
    dateRange: PropTypes.object,
    handleInitLoad: PropTypes.func,
    handleDateRangeChanged: PropTypes.func,
    handleFilterChanged: PropTypes.func,
    handleSelectedDayChanged: PropTypes.func.isRequired
  }

  componentDidMount () {
    const paramUsername = this.props.params.username
    const username = (!paramUsername || paramUsername === 'undefined')
      ? undefined : paramUsername
    this.props.handleInitLoad(username)
  }

  render () {
    const {
      user,
      notification,
      loading,
      matrixForAllDays,
      wordCountsForSelectedDayFilteredByContentState,
      wordCountsForEachDayFilteredByContentState,
      contentStateOption,
      selectedDay,
      dateRange,
      handleDateRangeChanged,
      handleFilterChanged,
      handleSelectedDayChanged
    } = this.props

    const username = user.username
    const name = user.name ? user.name : undefined
    const email = user.email ? user.email : undefined
    const roles = !isEmpty(user.roles) ? user.roles.join(', ') : undefined

    // TODO: fire ajax to get locale details
    const languageTeams = !isEmpty(user.languageTeams)
      ? map(user.languageTeams, (language) => {
        return (
          <a href={getLanguageUrl(language.localeId)}>
            {language.displayName}
          </a>
        )
      })
      : undefined

    let content
    if (user.loading || loading) {
      content = (<div className='user-profile'>
        <LoaderText className='loader-text s8' loading />
      </div>)
    } else if (isEmpty(username)) {
      content = <div className='flex-c profile-wrapper'></div>
    } else {
      content = (<div className='flex-c profile-wrapper'>
        <div className='details' id='profile-overview'>
          <img className='details-avatar'
            src={user.imageUrl ? user.imageUrl : ''} alt={username} />
          <div className='flex-c details-text'>
            {name &&
              <div className='username h2 ellipsis' id='profile-displayname'>
              {name}
              </div>
            }
            <ul className='large-font-list'>
              <li className='flex-center' id='profile-username'>
                <Icon name='user' className='s0' title='Username' />
                {username}
              </li>
              {email &&
              (<span className='profile-email'>{email}</span>)}
              {languageTeams &&
              (<li id='profile-languages'>
                <Icon name='language' className='s0 langicon pull-left'
                  title='Spoken languages' />
                {languageTeams}
              </li>)}
              {roles && isLoggedIn &&
              (<li className='flex-center' id='profile-roles' title='Roles'>
                <Icon name='users' className='s0' />
                <span>{roles}</span>
              </li>)}
            </ul>
          </div>
        </div>
        {isLoggedIn &&
        (<RecentContributions
          matrixForAllDays={matrixForAllDays}
          wordCountsForSelectedDayFilteredByContentState={
            wordCountsForSelectedDayFilteredByContentState}
          wordCountsForEachDayFilteredByContentState={
            wordCountsForEachDayFilteredByContentState}
          contentStateOption={contentStateOption}
          selectedDay={selectedDay}
          dateRange={dateRange}
          handleDateRangeChanged={handleDateRangeChanged}
          handleFilterChanged={handleFilterChanged}
          handleSelectedDayChanged={handleSelectedDayChanged} />)
        }
      </div>)
    }
    return (
      <div className='page'>
        {notification &&
          (<Notification
            severity={notification.severity}
            message={notification.message}
            details={notification.details}
            show={!!notification} />
        )}
        <Helmet title='User Profile' />
        <div className='wide-view-theme profile-page' >
          {content}
        </div>
      </div>
    )
  }
}

const mapStateToProps = (state) => {
  return {
    location: state.routing.location,
    user: state.profile.user,
    notification: state.profile.notification,
    loading: state.profile.loading,
    matrixForAllDays: state.profile.matrixForAllDays,
    wordCountsForSelectedDayFilteredByContentState:
      state.profile.wordCountsForSelectedDayFilteredByContentState,
    wordCountsForEachDayFilteredByContentState:
      state.profile.wordCountsForEachDayFilteredByContentState,
    contentStateOption: state.profile.contentStateOption,
    selectedDay: state.profile.selectedDay,
    dateRange: state.profile.dateRange
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    handleInitLoad: (username) => {
      dispatch(profileInitialLoad(username))
    },
    handleDateRangeChanged: (dateRange) => {
      dispatch(dateRangeChanged(dateRange))
    },
    handleFilterChanged: (contentState) => {
      dispatch(filterUpdate(contentState))
    },
    handleSelectedDayChanged: (day) => {
      dispatch(selectDayChanged(day))
    }
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(UserProfile)
