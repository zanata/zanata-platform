/*
 * Copyright 2018, Red Hat, Inc. and individual contributors as indicated by the
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

import { isEmpty } from "lodash";
import Input from "antd/lib/input";
import "antd/lib/input/style/css";
import Icon from "../../../components/Icon";
import * as React from "react";
import Button from "antd/lib/button";
import "antd/lib/button/style/css";

interface Props {
  postComment: (text: string) => void;
}

interface State {
  commentText: string
}

class CommentBox extends React.Component<Props, State> {
  private defaultState = {
    commentText: ""
  };

  constructor (props) {
    super(props);
    this.state = this.defaultState;
  }

  public render () {
    const { TextArea } = Input;
    return (
      <div className="TransUnit-commentBox mb4">
          <span>
            <Icon name="comment" className="s0" /> Post a comment
          </span><br />
          <TextArea
            autosize={{ minRows: 2, maxRows: 6 }}
            onChange={this.setCommentText}
            placeholder="..."
            value={this.state.commentText}
          />
        <Button disabled={isEmpty(this.state.commentText)}
          onClick={this.postComment}
          className="EditorButton Button--small u-rounded Button--primary u-pullRight mt2">
         Post comment
        </Button>
      </div>
    );
  }

  private postComment = () => {
    const text = this.state.commentText;
    this.props.postComment(text)
    // reset the input, avoid multiple posts
    this.setState(this.defaultState);
  }

  private setCommentText = (event) => {
    this.setState({commentText: event.target.value});
  }
}

export default CommentBox;
