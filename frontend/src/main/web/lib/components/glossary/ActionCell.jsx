import React, {PureRenderMixin} from 'react/addons';
import { Button, Icon, Tooltip, OverlayTrigger, Overlay } from 'zanata-ui';
import Actions from '../../actions/GlossaryActions';
import LoadingCell from './LoadingCell'
import GlossaryStore from '../../stores/GlossaryStore';
import StringUtils from '../../utils/StringUtils'
import _ from 'lodash';
import cx from 'classnames'

var ActionCell = React.createClass({
  propTypes: {
    id: React.PropTypes.number.isRequired,
    info: React.PropTypes.string.isRequired,
    rowIndex: React.PropTypes.number.isRequired,
    canUpdateEntry: React.PropTypes.bool
  },

  mixins: [PureRenderMixin],

  getInitialState: function() {
    return this._getState();
  },

  _getState: function() {
    var entry = GlossaryStore.getEntry(this.props.id);
    return {
      entry: entry,
      saving: false,
      comment: _.cloneDeep(entry.transTerm.comment),
      savingComment: false,
      showComment: false
    }
  },

  componentDidMount: function() {
    GlossaryStore.addChangeListener(this._onChange);
  },

  componentWillUnmount: function() {
    GlossaryStore.removeChangeListener(this._onChange);
  },

  _onChange: function() {
    if (this.isMounted()) {
      this.setState(this._getState());
    }
  },

  _handleUpdate: function() {
    this.setState({saving: true});
    Actions.updateGlossary(this.props.id);
  },

  _handleCancel: function() {
    Actions.resetEntry(this.props.id);
  },

  _onUpdateComment: function () {
    this.setState({savingComment: true});
    Actions.updateComment(this.props.id, this.state.comment);
  },

  _hasCommentChanged: function() {
    var initialValue = _.isUndefined(this.state.entry.transTerm.comment) ? '' : this.state.entry.transTerm.comment;
    var newValue = _.isUndefined(this.state.comment) ? '' : this.state.comment;
    return initialValue !== newValue;
  },

  _toggleComment: function () {
    this.setState({showComment: !this.state.showComment});
  },

  _onCommentChange: function(event) {
    this.setState({comment: event.target.value});
  },

  _onCancelComment: function () {
    var value = _.isUndefined(this.state.entry.transTerm.comment) ? '' : this.state.entry.transTerm.comment;
    this.setState({comment: value, showComment: false});
  },

  _handleCommentKeyUp: function (event) {
    if(event.key === 'Escape') {
      this._onCancelComment();
    }
  },

  render: function () {
    if (this.props.id === null || this.state.entry === null) {
      return <LoadingCell/>;
    } else {
      var isTransModified = this.state.entry.status.isTransModified;
      var canUpdateComment = this.state.entry.status.canUpdateTransComment;
      var isSaving = this.state.entry.status.isSaving || this.state.saving;

      var infoTooltip = <Tooltip id="info">{this.props.info}</Tooltip>;
      var info = (
        <OverlayTrigger placement='top' rootClose overlay={infoTooltip}>
          <Icon className="cpri" name="info"/>
        </OverlayTrigger>);

      var updateButton,
        cancelButton,
        readOnlyComment = !this.props.canUpdateEntry || !canUpdateComment || isSaving,
        disableCommentUpdate = !this._hasCommentChanged(),
        saveCommentButton;

      if(this.state.savingComment) {
        saveCommentButton = (
          <Button kind='primary' size={-1} disabled={disableCommentUpdate}
            onClick={this._onUpdateComment} loading>
            Update Comment
          </Button>
        );
      } else {
        saveCommentButton = (
          <Button kind='primary' size={-1} disabled={disableCommentUpdate}
            onClick={this._onUpdateComment}>
            Update Comment
          </Button>
        );
      }

      if(readOnlyComment !== true) {
        var tooltip = (
          <Tooltip id="comment" title="Comment">
            <textarea className="p1/4 w100p bd2 bdcsec30 bdrs1/4"
              onChange={this._onCommentChange}
              value={this.state.comment}
              onKeyUp={this._handleCommentKeyUp}/>
            <div className="mt1/4">
              <Button className="mr1/2" link
                onClick={this._onCancelComment}>
                Cancel
              </Button>
            {saveCommentButton}
            </div>
          </Tooltip>
        );
      } else {
        var comment = StringUtils.isEmptyOrNull(this.state.comment) ? (<i>No comment</i>) : (<span>{this.state.comment}</span>);
        tooltip = (<Tooltip id="comment">{comment}</Tooltip>);
      }

      var buttonClasses = cx(
        this.props.className,
        'mr1/2'
      );

      var comment = (
        <div className="dib">
          <Overlay placement='top'
            target={() => React.findDOMNode(this.refs.commentButton)}
            onHide={this._onCancelComment}
            rootClose show={this.state.showComment}>
            {tooltip}
          </Overlay>
          <Button link ref='commentButton'
            kind={StringUtils.isEmptyOrNull(this.state.comment) ? 'muted' : 'primary'}
            className={buttonClasses}
            onClick={this._toggleComment}>
            <Icon name='comment'/>
          </Button>
        </div>
      );

      if(isSaving) {
        return (
          <div>
            {info} {comment}
            <Button kind='primary' className="ml1/4" loading>Update</Button>
          </div>
        );
      }

      if(isTransModified) {
        updateButton = (
          <Button kind='primary' className='ml1/4' onClick={this._handleUpdate}>
            Update
          </Button>
        );
        cancelButton = (
          <Button className='ml1/4' link onClick={this._handleCancel}>
            Cancel
          </Button>
        );
      }

      return (
        <div>
          {info} {comment}
          <div className='cdtargetib'>
            {updateButton} {cancelButton}
          </div>
        </div>);
    }
  }
});

export default ActionCell;
