import React, { PureRenderMixin } from 'react/addons';
import {Icon, Button, Input, Modal} from 'zanata-ui';
import Actions from '../../actions/GlossaryActions';
import StringUtils from '../../utils/StringUtils'
import _ from 'lodash';
import GlossaryStore from '../../stores/GlossaryStore';

var NewEntryModal = React.createClass({
  propTypes: {
    className: React.PropTypes.string,
    srcLocale: React.PropTypes.shape({
      locale: React.PropTypes.shape({
        localeId: React.PropTypes.string.isRequired,
        displayName: React.PropTypes.string.isRequired,
        alias: React.PropTypes.string.isRequired
      }).isRequired,
      numberOfTerms: React.PropTypes.number.isRequired
    })
  },

  mixins: [PureRenderMixin],

  getInitialState: function() {
    return this._getState();
  },

  _getState: function() {
    return GlossaryStore.getNewEntryState();
  },

  componentDidMount: function() {
    GlossaryStore.addChangeListener(this._onChange);
  },

  componentWillUnmount: function() {
    GlossaryStore.removeChangeListener(this._onChange);
  },

  _onChange: function() {
    this.setState(this._getState());
  },

  _showModal: function () {
    this.setState({show: true})
  },

  _closeModal: function () {
    this.setState(this.getInitialState());
  },

  _save: function () {
    Actions.saveGlossary(this.props.srcLocale.locale.localeId, this.state.term, this.state.pos, this.state.description);
  },

  _onTermChange: function(event) {
    this._setTermValue(event.target.value);
  },

  _onTermReset: function(event) {
    this._setTermValue('');
  },

  _setTermValue: function(value) {
    value = StringUtils.trimLeadingSpace(value);
    var isAllowSave = !StringUtils.isEmptyOrNull(value);
    this.setState({term: value, isAllowSave: isAllowSave});
  },

  _onPosChange: function(event) {
    this.setState({pos: event.target.value});
  },

  _onPosReset: function(event) {
    this.setState({pos: ''});
  },

  _onDescChange: function(event) {
    this.setState({description: event.target.value});
  },

  _onDescReset: function(event) {
    this.setState({description: ''});
  },

  render: function () {
    return (
      <div className={this.props.className}>
        <Button className='whsnw tove ovh' onClick={this._showModal} link>
          <Icon name='plus' className='mr1/8'/><span>New Term</span>
        </Button>
        <Modal show={this.state.show} onHide={this._closeModal}>
          <Modal.Header>
            <Modal.Title>New Term</Modal.Title>
          </Modal.Header>
          <Modal.Body className='tal'>
            <Input
              margin='mb1/2'
              border='underline'
              label='Term'
              value={this.state.term}
              placeholder='The new term'
              onChange={this._onTermChange}
              onReset={this._onTermReset}
              />

            <Input
              margin='mb1/2'
              border='underline'
              label='Part of speech'
              value={this.state.pos}
              placeholder='Noun, Verb, etc'
              maxLength='255'
              onChange={this._onPosChange}
              onReset={this._onPosReset}
              />

            <Input
              margin='mb1/2'
              border='underline'
              label='Description'
              value={this.state.description}
              placeholder='The definition of this term'
              maxLength='255'
              onChange={this._onDescChange}
              onReset={this._onDescReset}
              />
          </Modal.Body>
          <Modal.Footer>
            <Button
              className='mr1'
              link disabled={this.state.isSaving}
              onClick={this._closeModal}>
              Cancel
            </Button>
            <Button
              kind='primary'
              disabled={!this.state.isAllowSave || this.state.isSaving}
              onClick={this._save}
              loading={this.state.isSaving}>
              Save
            </Button>
          </Modal.Footer>
        </Modal>
      </div>);
  }
});

export default NewEntryModal;
