import React from 'react'
import { Well } from 'react-bootstrap'
import Icon from '../../../components/Icon'

class RevisionFuzzy extends React.Component {

  render () {
    return (
      <div>
        <p><Icon name="refresh" className="s0" />
          <img className="u-round activity-avatar" src="" /><a>Username</a>
          created a
          <span className="u-textUnsure"> <strong> fuzzy </strong>
          </span> revision.
        </p>
        <Well className="well-fuzzy">নাম</Well>
        <p className="small u-textMuted">
          <Icon name="clock" className="n1" /> May 19 2017 at 10:00</p>
      </div>
    )
  }
}

export default RevisionFuzzy
