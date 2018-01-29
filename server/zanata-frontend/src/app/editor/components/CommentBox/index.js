/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

import * as React from 'react'
import { isEmpty } from 'lodash'
import * as PropTypes from 'prop-types'
import { Button, FormGroup, ControlLabel, FormControl } from 'react-bootstrap'
import Icon from '../../../components/Icon'

class CommentBox extends React.Component {
  static propTypes = {
    postComment: PropTypes.func.isRequired
  }

  constructor (props) {
    super(props)
    this.state = {commentText: ''}
  }

  postComment = () => {
    const text = this.state.commentText
    this.props.postComment(text)
  }

  setCommentText = (event) => {
    this.setState({commentText: event.target.value})
  }

  render () {
    return (
      <div className="TransUnit-commentBox">
        <FormGroup controlId="formControlsTextarea">
          <ControlLabel>
            <Icon name="comment" className="s0" /> Post a comment
          </ControlLabel><br />
          <FormControl componentClass="textarea"
            placeholder="..." onChange={this.setCommentText} />
        </FormGroup>
        <Button disabled={isEmpty(this.state.commentText)}
          onClick={this.postComment}
          className="EditorButton Button--small u-rounded Button--primary u-pullRight">
         Post comment
        </Button>
      </div>
    )
  }
}

export default CommentBox
