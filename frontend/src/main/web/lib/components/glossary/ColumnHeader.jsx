import React, {PureRenderMixin} from 'react/addons';
import { Icon } from 'zanata-ui';
import _ from 'lodash';
import GlossaryStore from '../../stores/GlossaryStore';

var ColumnHeader = React.createClass({
  propTypes: {
    value: React.PropTypes.string.isRequired,
    allowSort: React.PropTypes.bool.isRequired,
    field: React.PropTypes.oneOf(['src_content', 'trans_content', 'part_of_speech', 'desc', 'trans_count']),
    onClickCallback: React.PropTypes.func
  },

  mixins: [PureRenderMixin],

  getInitialState: function() {
    return {
      //true, false, undefined
      sort: this._getState(this.props.field)
    };
  },

  componentDidMount: function() {
    GlossaryStore.addChangeListener(this._onChange);
  },

  componentWillUnmount: function() {
    GlossaryStore.removeChangeListener(this._onChange);
  },

  _onChange: function() {
    this.setState(this.getInitialState());
  },

  _getState: function() {
    return GlossaryStore.getSort(this.props.field);
  },

  _handleOnClick: function (event) {
    if(this.props.allowSort) {
      if(this.props.onClickCallback) {
        this.props.onClickCallback(this.props.field, !this.state.sort);
      }
    }
  },

  render: function() {
    var sortIcon,
      styleClass = 'csec ph1/2';

    if(this.props.allowSort) {
      if(!_.isUndefined(this.state.sort)) {
        var iconName = this.state.sort === true ? 'chevron-down' : 'chevron-up';
        sortIcon = <Icon name={iconName}/>;
        styleClass += ' fwsb';
      }
      return <button className={styleClass} onClick={this._handleOnClick}>{this.props.value} {sortIcon}</button>;
    } else {
      return <span className={styleClass}>{this.props.value}</span>;
    }
  }

});

export default ColumnHeader;
