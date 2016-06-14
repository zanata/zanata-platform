import React, { PropTypes } from 'react'
import Helmet from 'react-helmet'
import { isEmpty } from 'lodash'
import RecentContributions from './RecentContributions'
import UserMatrixStore from '../../stores/UserMatrixStore'
import {
  Base,
  Flex,
  Icon,
  LoaderText,
  Notification,
  Page,
  ScrollView,
  View
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
var UserProfile = React.createClass({
  getMatrixState: function () {
    const username = this.props.params.username
      ? this.props.params.username
      : window.config.user.username
    return UserMatrixStore.getMatrixState(username)
  },

  getInitialState: function () {
    return this.getMatrixState()
  },

  componentDidMount: function () {
    UserMatrixStore.addChangeListener(this._onChange)
  },

  componentWillUnmount: function () {
    UserMatrixStore.removeChangeListener(this._onChange)
  },

  componentWillReceiveProps: function (nextProps) {
    if (nextProps.params.username !== this.props.params.username) {
      this.setState(UserMatrixStore.getMatrixState(nextProps.params.username))
    }
  },

  _onChange: function () {
    this.setState(this.getMatrixState())
  },

  render: function () {
    const user = isEmpty(this.state.user) ? window.config.user : this.state.user
    const username = user && user.username ? user.username : ''
    const languageTeams = user && !isEmpty(user.languageTeams)
      ? user.languageTeams.join() : ''
    const notification = this.state.notification
    return (
      <Page>
        {notification && (<Notification
          severity={notification.severity}
          message={notification.message}
          details={notification.details}
          show={!!notification} />
        )}
        <Helmet title='User Profile' />
        <ScrollView>
          {user.loading || this.state.loading
            ? (
            <div
              className='D(f) Ai(fs) Ac(fs) Fld(c) Jc(fs) Flw(nw) My(r3) Mx(a) Maw(20em) W(100%)'>
              <LoaderText size='8' loading atomic={{w: 'W(100%)'}}/>
            </div>)
            : (
            <Flex dir='c' atomic={classes.wrapper}>
              <Flex dir='rr' id='profile-overview' atomic={classes.details}>
                <Base atomic={classes.detailsAvatar}>
                  <img src={user && user.imageUrl ? user.imageUrl : ''}
                    alt={username} />
                </Base>
                <Flex dir='c' atomic={classes.detailsText}>
                  <Base atomic={classes.usersName} id='profile-displayname'>
                    {user && user.name ? user.name : ''}
                  </Base>
                  <ul className='Fz(msn1)'>
                    <Flex tagName='li' align='c' id='profile-username'>
                      <Icon name='user'
                        atomic={{m: 'Mend(re)'}}
                        title='Username'/>
                      {username}
                      {user && user.email &&
                      (<span className='Mstart(rq) C(muted)'>{user.email}</span>)
                      }
                    </Flex>
                    {user && !isEmpty(user.languageTeams) &&
                    (<Flex tagName='li' align='c' id='profile-languages'>
                      <Icon name='language'
                        atomic={{m: 'Mend(re)'}}
                        title='Spoken languages'/>
                      {languageTeams}
                    </Flex>)}
                  </ul>
                </Flex>
              </Flex>
              {window.config.permission.isLoggedIn &&
              (<RecentContributions
                dateRangeOption={this.state.dateRangeOption}
                matrixForAllDays={this.state.matrixForAllDays}
                wordCountsForSelectedDayFilteredByContentState={
                    this.state.wordCountsForSelectedDayFilteredByContentState
                  }
                wordCountsForEachDayFilteredByContentState={
                    this.state.wordCountsForEachDayFilteredByContentState}
                contentStateOption={this.state.contentStateOption}
                selectedDay={this.state.selectedDay}
                dateRange={this.state.dateRange} />)
              }
            </Flex>)
          }
        </ScrollView>
      </Page>
    )
  }
})

UserProfile.propTypes = {
  params: PropTypes.shape({
    username: PropTypes.string
  })
}

export default UserProfile
