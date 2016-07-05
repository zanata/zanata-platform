import React, { Component, PropTypes } from 'react'
import { connect } from 'react-redux'
import Helmet from 'react-helmet'
import { isEmpty } from 'lodash'
import {
  profileInitialLoad,
  dateRangeChanged,
  filterUpdate,
  selectDayChanged
} from '../../actions/profile'
import RecentContributions from './RecentContributions'
import {
  Base,
  Flex,
  Icon,
  LoaderText,
  Notification,
  Page,
  ScrollView
} from '../../components'

const classes = {
  wrapper: {
    p: 'Py(r1)',
    fld: 'Fld(c) Fld(r)--md'
  },
  details: {
    w: 'W(100%) Miw(21rem) Maw(r16)',
    m: 'Mb(r1)'
  },
  detailsAvatar: {
    bdrs: 'Bdrs(rnd)',
    m: 'Mstart(a)',
    ov: 'Ov(h)',
    w: 'Maw(1/4)'
  },
  detailsText: {
    m: 'Mend(rh)',
    flx: 'Flx(flx1)'
  },
  usersName: {
    fz: 'Fz(ms3)',
    fw: 'Fw(600)'
  }
}

/**
 * Root component for user profile page
 */
class UserProfile extends Component {

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
      dateRangeOption,
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
    const languageTeams = !isEmpty(user.languageTeams)
      ? user.languageTeams.join() : undefined
    const isLoggedIn = window.config.permission.isLoggedIn

    const divClass = 'D(f) Ai(fs) Ac(fs) Fld(c) Jc(fs) Flw(nw) ' +
      'My(r3) Mx(a) Maw(20em) W(100%)'
    return (
      <Page>
        {notification &&
          (<Notification
            severity={notification.severity}
            message={notification.message}
            details={notification.details}
            show={!!notification} />
        )}
        <Helmet title='User Profile' />
        <ScrollView>
          {user.loading || loading
            ? (<div className={divClass}>
              <LoaderText size='8' loading atomic={{w: 'W(100%)'}} />
            </div>)
            : (<Flex dir='c' atomic={classes.wrapper}>
              <Flex dir='rr' id='profile-overview' atomic={classes.details}>
                <Base atomic={classes.detailsAvatar}>
                  <img src={user.imageUrl ? user.imageUrl : ''}
                    alt={username} />
                </Base>
                <Flex dir='c' atomic={classes.detailsText}>
                  {name &&
                    <Base atomic={classes.usersName} id='profile-displayname'>
                      {name}
                    </Base>
                  }
                  <ul className='Fz(msn1)'>
                    <Flex tagName='li' align='c' id='profile-username'>
                      <Icon name='user'
                        atomic={{m: 'Mend(re)'}}
                        title='Username' />
                      {username}
                      {email &&
                        (<span className='Mstart(rq) C(muted)'>
                          {email}
                        </span>)
                      }
                    </Flex>
                    {languageTeams &&
                    (<Flex tagName='li' align='c' id='profile-languages'>
                      <Icon name='language'
                        atomic={{m: 'Mend(re)'}}
                        title='Spoken languages' />
                      {languageTeams}
                    </Flex>)}
                  </ul>
                </Flex>
              </Flex>
              {isLoggedIn &&
              (<RecentContributions
                dateRangeOption={dateRangeOption}
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
            </Flex>)
          }
        </ScrollView>
      </Page>
    )
  }
}

UserProfile.propTypes = {
  params: PropTypes.object,
  user: PropTypes.object,
  notification: PropTypes.object,
  loading: PropTypes.bool,
  dateRangeOption: PropTypes.object,
  matrixForAllDays: PropTypes.array,
  wordCountsForSelectedDayFilteredByContentState: PropTypes.array,
  wordCountsForEachDayFilteredByContentState: PropTypes.array,
  contentStateOption: PropTypes.string,
  selectedDay: PropTypes.string,
  dateRange: PropTypes.object,
  handleInitLoad: PropTypes.func,
  handleDateRangeChanged: PropTypes.func,
  handleFilterChanged: PropTypes.func,
  handleSelectedDayChanged: PropTypes.func
}

const mapStateToProps = (state) => {
  return {
    location: state.routing.location,
    user: state.profile.user,
    notification: state.profile.notification,
    loading: state.profile.loading,
    dateRangeOption: state.profile.dateRangeOption,
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
    handleDateRangeChanged: (dateRangeOption) => {
      dispatch(dateRangeChanged(dateRangeOption))
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
