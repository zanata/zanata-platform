import React from 'react'
import { Well } from 'react-bootstrap'
import Icon from '../../../components/Icon'

class RevisionRejected extends React.Component {

  render () {
    return (
      <div>
        <p><Icon name="refresh" className="s0" />
          <img className="u-round activity-avatar" src="" /><a>Username</a>
          <span className="u-textWarning"><strong> rejected </strong>
          </span>
        a translation.</p>
        <Well className="well-rejected">নাম</Well>
        <p className="small u-textMuted">
          <Icon name="clock" className="n1" /> May 21 2017 at 08:00</p>
      </div>
    )
  }
}

export default RevisionRejected
