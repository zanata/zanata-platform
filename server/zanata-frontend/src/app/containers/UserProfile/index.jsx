// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
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
      content = (<div className='userProfile bstrapReact'>
        <LoaderText className='loaderText s8' loading />
      </div>)
    } else if (isEmpty(username)) {
      content = <div className='u-flexColumn userProfile-wrapper bstrapReact'>
      </div>
    } else {
      content = (<div className='u-flexColumn userProfile-wrapper bstrapReact'>
        <div className='userProfile-details' id='userProfile-overview'>
          <img className='userProfile-details-avatar'
            src={user.imageUrl ? user.imageUrl : ''} alt={username} />
          <div className='u-flexColumn details-text'>
            {name &&
              <div className='username h1 ellipsis'
                id='profile-displayname'>
              {name}
              </div>
            }
            <ul className='largeFontList'>
              <li className='u-flexCenter' id='profile-username'>
                <Icon name='user' className='s0' title='Username' />
                {username}
              </li>
              {email &&
              (<span className='userProfile-email'>{email}</span>)}
              {languageTeams &&
              (<li id='profileLanguages'>
                <Icon name='language' className='s0' title='Spoken languages'
                  parentClassName='iconLanguage u-pullLeft' />
                {languageTeams}
              </li>)}
              {roles && isLoggedIn &&
              (<li className='u-flexCenter' id='profileRoles' title='Roles'>
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
      <div>
        {notification &&
          (<Notification
            severity={notification.severity}
            message={notification.message}
            details={notification.details}
            show={!!notification} />
        )}
        <Helmet title='User Profile' />
        <div className='wideView profile' >
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
