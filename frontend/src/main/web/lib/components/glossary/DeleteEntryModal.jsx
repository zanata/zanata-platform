import React, { PureRenderMixin } from 'react/addons';
import Actions from '../../actions/GlossaryActions';
import { Button, Icon, Tooltip, Overlay } from 'zanata-ui';

var DeleteEntryModal = React.createClass({
  propTypes: {
    id: React.PropTypes.number.isRequired,
    className: React.PropTypes.string,
    entry: React.PropTypes.object
  },

  deleteTimeout: null,

  getInitialState: function() {
    return {
      show: false,
      deleting: false
    }
  },

  componentWillUnmount: function() {
    if(this.deleteTimeout !== null) {
      clearTimeout(this.deleteTimeout);
    }
  },

  _handleDelete: function() {
    this.setState({deleting: true});

    if(this.deleteTimeout !== null) {
      clearTimeout(this.deleteTimeout);
    }

    this.deleteTimeout = setTimeout(() => {
      Actions.deleteGlossary(this.props.id);
      this._closeDialog()
    }, 100);
  },

  _toggleDialog: function () {
    this.setState({show: !this.state.show});
  },

  _closeDialog: function () {
    this.setState(this.getInitialState());
  },

  render: function () {
    var isDeleting = this.state.deleting,
      info = null;

    if(this.props.entry.termsCount > 0) {
      let translationPlural = this.props.entry.termsCount > 1
        ? 'translations' : 'translation';
      info = (
        <p>
          Are you sure you want to delete this term and&nbsp;
          <strong>{this.props.entry.termsCount}</strong> {translationPlural} ?
        </p>
      );
    } else {
      info = <p>Are you sure you want to delete this term ?</p>
    }

    const tooltip = (
      <Tooltip id="delete-glossary" title="Delete term and translations">
        {info}
        <div className="mt1/4">
          <Button className="mr1/2" link onClick={this._closeDialog}>Cancel</Button>
          <Button kind='danger'
            size={-1}
            loading={isDeleting}
            onClick={this._handleDelete}>
            Delete all
          </Button>
        </div>
      </Tooltip>
    );

    return (
      <div className={this.props.className + ' dib'}>
        <Overlay placement='top' target={() => React.findDOMNode(this)} onHide={this._closeDialog} rootClose show={this.state.show}>
          {tooltip}
        </Overlay>
        <Button kind='danger' loading={isDeleting} onClick={this._toggleDialog} link>
          <Icon name="trash" className='mr1/8'/><span>Delete</span>
        </Button>
      </div>
    );
  }
});

export default DeleteEntryModal;
