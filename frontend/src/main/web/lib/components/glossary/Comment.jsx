import React, {PureRenderMixin} from 'react/addons';
import { Button, Icon, Tooltip, OverlayTrigger, Overlay } from 'zanata-ui';
import StringUtils from '../../utils/StringUtils'
import _ from 'lodash';
import cx from 'classnames'


var Comment = React.createClass({
  propTypes: {
    readOnly: React.PropTypes.bool,
    className: React.PropTypes.string,
    value: React.PropTypes.string.isRequired,
    onUpdateCommentCallback: React.PropTypes.func.isRequired
  },

  mixins: [PureRenderMixin],

  getInitialState: function() {
    return {
      value: this.props.value,
      saving: false
    }
  },

  _onCommentChange: function(event) {
    this.setState({value: event.target.value});
  },

  _onUpdateComment: function () {
    this.setState({saving: true});
    this.props.onUpdateCommentCallback(this.state.value);
    this.setState({showComment: false});
  },

  _onCancelComment: function () {
    var value = _.isUndefined(this.props.value) ? '' : this.props.value;
    this.setState({value: value, showComment: false});
  },

  _handleKeyUp: function (event) {
    if(event.key === 'Escape') {
      this._onCancelComment();
    }
  },

  _hasValueChanged: function() {
    var initialValue = _.isUndefined(this.props.value) ? '' : this.props.value;
    var newValue = _.isUndefined(this.state.value) ? '' : this.state.value;
    return initialValue !== newValue;
  },

  _toggleComment: function () {
    this.setState({showComment: !this.state.showComment});
  },

  render: function () {
    var tooltip,
      saveButton,
      disableUpdate = !this._hasValueChanged();

    if(this.state.saving) {
      saveButton = (
        <Button kind='primary' size={-1} disabled={disableUpdate}
          onClick={this._onUpdateComment} loading>
          Update Comment
        </Button>
      );
    } else {
      saveButton = (
        <Button kind='primary' size={-1} disabled={disableUpdate}
          onClick={this._onUpdateComment}>
          Update Comment
        </Button>
      );
    }

    if(this.props.readOnly !== true) {
      var tooltip = (
        <Tooltip id="comment" title="Comment">
          <textarea className="p1/4 w100p bd2 bdcsec30 bdrs1/4"
            onChange={this._onCommentChange}
            value={this.state.value}
            onKeyUp={this._handleKeyUp}/>
          <div className="mt1/4">
            <Button className="mr1/2" link
              onClick={this._onCancelComment}>
              Cancel
            </Button>
            {saveButton}
          </div>
        </Tooltip>
      );
    } else {
      var comment = StringUtils.isEmptyOrNull(this.state.value) ? (<i>No comment</i>) : (<span>{this.state.value}</span>);
      tooltip = (<Tooltip id="comment">{comment}</Tooltip>);
    }

    var buttonClasses = cx(
      this.props.className,
      'mr1/2'
    );

    return (
      <div className="dib">
        <Overlay placement='top'
          target={() => React.findDOMNode(this)}
          onHide={this._onCancelComment}
          rootClose show={this.state.showComment}>
          {tooltip}
        </Overlay>
        <Button link
          kind={StringUtils.isEmptyOrNull(this.state.value) ? 'muted' : 'primary'}
          className={buttonClasses}
          onClick={this._toggleComment}>
          <Icon name='comment'/>
        </Button>
      </div>
    );
  }
});

export default Comment;
