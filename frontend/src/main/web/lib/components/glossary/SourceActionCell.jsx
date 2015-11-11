import React, {PureRenderMixin} from 'react/addons';
import { Button, Icon, Tooltip, OverlayTrigger } from 'zanata-ui';
import Actions from '../../actions/GlossaryActions';
import LoadingCell from './LoadingCell'
import DeleteEntryModal from './DeleteEntryModal'
import GlossaryStore from '../../stores/GlossaryStore';
import _ from 'lodash';

var SourceActionCell = React.createClass({
  propTypes: {
    id: React.PropTypes.number.isRequired,
    info: React.PropTypes.string.isRequired,
    rowIndex: React.PropTypes.number.isRequired,
    srcLocaleId: React.PropTypes.string.isRequired,
    canUpdateEntry: React.PropTypes.bool,
    canDeleteEntry: React.PropTypes.bool
  },

  mixins: [PureRenderMixin],

  getInitialState: function() {
    return this._getState();
  },

  _getState: function () {
    return {
      entry: GlossaryStore.getEntry(this.props.id),
      saving: false
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

  render: function() {
    if(this.props.id === null || this.state.entry === null) {
      return <LoadingCell/>;
    } else {
      var classes = this.props.info === Actions.NO_INFO_MESSAGE ? 'csec50' : 'cpri';
      var info = (
        <OverlayTrigger placement='top'
          rootClose
          overlay={<Tooltip id='src-info'>{this.props.info}</Tooltip>}>
          <Icon className={classes} name="info"/>
        </OverlayTrigger>
      );

      if(this.state.entry.status.isSaving || this.state.saving) {
        return <div>{info} <Button kind='primary' className="ml1/4" loading>Update</Button></div>;
      } else {
        var deleteButton;

        if(this.props.canDeleteEntry) {
          deleteButton = <DeleteEntryModal className='ml1/4' id={this.props.id} entry={this.state.entry}/>;
        }

        var isSrcModified= this.state.entry.status.isSrcModified,
          isSrcValid = this.state.entry.status.isSrcValid;

        if(isSrcModified) {
          var updateButton,
            cancelButton = <Button className='ml1/4' link onClick={this._handleCancel}>Cancel</Button>;
          if(this.props.canUpdateEntry && isSrcValid) {
            updateButton = <Button kind='primary' className="ml1/4" onClick={this._handleUpdate}>Update</Button>;
          }
          return (
              <div className='difx aic'>
                <div className='cdtargetib'>{info}</div>
                {updateButton}
                {cancelButton}
                {deleteButton}
              </div>)
        } else {
          return <div>{info}<div className='cdtargetib'>{deleteButton}</div></div>;
        }
      }
    }
  }
});

export default SourceActionCell;
