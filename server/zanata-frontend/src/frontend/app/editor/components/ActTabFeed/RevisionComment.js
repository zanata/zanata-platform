import React from 'react'
import { Well } from 'react-bootstrap'
import Icon from '../../../components/Icon'

class RevisionComment extends React.Component {

  render () {
    return (
      <div>
        <p><Icon name="comment" className="s0" />
          <img className="u-round activity-avatar" src="" /><a>Username</a>
          &nbsp;commented on a <strong>Spanish</strong> translation.</p>
        <Well>I have no idea what this translation means</Well>
        <p className="small u-textMuted">
          <Icon name="clock" className="n1" /> May 20 2017 at 06:00</p>
      </div>
    )
  }
}

export default RevisionComment
