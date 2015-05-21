import React from 'react';
import Actions from '../actions/Actions';
import {PureRenderMixin} from 'react/addons';

var DropDown = React.createClass({
  mixins: [PureRenderMixin],

  getInitialState: function() {
    return {dropdownIsActive: false};
  },

  handleOptionClick: function(option) {
    if (this.props.selectedOption != option) {
      Actions.changeDateRange(option);
    }
    this.setState({dropdownIsActive: false});
  },

  handleButtonClick: function(e) {
    e.preventDefault();
    this.setState({dropdownIsActive: !this.state.dropdownIsActive});
  },

  render: function() {
    var options = this.props.options,
      selected = this.props.selectedOption,
      self = this,
      dropDownClass = 'Dropdown--simple',
      optionList;

    dropDownClass += this.state.dropdownIsActive ? ' is-active' : '';

    optionList = options.map(function (option) {
      var buttonClassName = 'button--link txt--nowrap';
      buttonClassName += option === selected ? ' is-active' : '';
      return <li key={option} className='Dropdown-item'>
        <button className={buttonClassName} onClick={self.handleOptionClick.bind(self, option)}>{option}</button>
      </li>
    });

    return (
      <div className={dropDownClass}>
      <button className="button--link" onClick={this.handleButtonClick}><span className='Dropdown-toggleIcon'><i className='i i--arrow-down'></i></span> {selected}</button>
        <ul className="Dropdown-content">
          {optionList}
        </ul>
      </div>
    )
  }
});

export default DropDown;
