import React, {PureRenderMixin} from 'react/addons';
import {Input} from 'zanata-ui';
import StringUtils from '../../utils/StringUtils';
import Actions from '../../actions/GlossaryActions';
import GlossaryStore from '../../stores/GlossaryStore';
import _ from 'lodash';

var InputCell = React.createClass({
  propTypes: {
    id: React.PropTypes.number.isRequired,
    field: React.PropTypes.string.isRequired,
    rowIndex: React.PropTypes.number.isRequired,
    placeholder: React.PropTypes.string,
    onFocusCallback: React.PropTypes.func,
    onBlurCallback: React.PropTypes.func,
    maxLength: React.PropTypes.number
  },

  TIMEOUT: 150,

  updateTimeout: null,

  getInitialState: function() {
    return this._getState();
  },

  _getState: function() {
    var entry = GlossaryStore.getEntry(this.props.id),
      value = _.get(entry, this.props.field),
      focusedRow = GlossaryStore.getFocusedRow(),
      isFocused = focusedRow && (focusedRow.rowIndex === this.props.rowIndex);

    return {
      value: value,
      isFocused : isFocused
    }
  },

  componentDidMount: function() {
    GlossaryStore.addChangeListener(this._onChange);
  },

  componentWillUnmount: function() {
    if(this.updateTimeout !== null) {
      clearTimeout(this.updateTimeout);
    }

    GlossaryStore.removeChangeListener(this._onChange);
  },

  _onChange: function() {
    if (this.isMounted()) {
      this.setState(this._getState());
    }
  },

  _onValueChange : function(event) {
    var value = event.target.value;
    this.setState({value: value});
    if(this.updateTimeout !== null) {
      clearTimeout(this.updateTimeout);
    }
    this.updateTimeout = setTimeout(() => {
      Actions.updateEntryField(this.props.id, this.props.field, value);
    }, this.TIMEOUT);
  },

  _onFocus: function(event) {
    if(this.props.onFocusCallback) {
      this.props.onFocusCallback(event, this.props.rowIndex);
    }
  },

  _onBlur: function(event) {
    if(this.props.onBlurCallback) {
      this.props.onBlurCallback(event, this.props.rowIndex);
    }
  },

  //handle reset from the input
  _onReset: function () {
    this.setState({value: this.props.value});
    Actions.updateEntryField(this.props.id, this.props.field, this.props.value);
  },

  render: function() {
    const maxLength = this.props.maxLength ? this.props.maxLength : -1;
    return (
      <div>
        <span className='cdtargetn mh1/2'>{this.state.value}</span>
        <Input
          className='cdtargetib'
          margin='mh1/8'
          border='outline'
          maxLength={maxLength}
          label={this.props.field}
          hideLabel
          value={this.state.value}
          placeholder={this.props.placeholder}
          onChange={this._onValueChange}
          onFocus={this._onFocus}
          onReset={this._onReset}
          onBlur={this._onBlur}
        />
      </div>
    );
  }
});

export default InputCell;
