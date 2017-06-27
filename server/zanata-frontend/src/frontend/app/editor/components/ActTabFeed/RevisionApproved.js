import React from 'react'
import { Well } from 'react-bootstrap'
import Icon from '../../../components/Icon'

class RevisionApproved extends React.Component {

  render () {
    return (
      <div>
        <p><Icon name="refresh" className="s0" />
          <img className="u-round activity-avatar" src="" /><a>Username</a>
          <span className="u-textHighlight"> <strong>approved</strong>
          </span> a translation.
        </p>
        <Well className="well-approved">নাম</Well>
        <p className="small u-textMuted">
          <Icon name="clock" className="n1" /> May 18 2017 at 15:00</p>
      </div>
    )
  }
}

export default RevisionApproved
