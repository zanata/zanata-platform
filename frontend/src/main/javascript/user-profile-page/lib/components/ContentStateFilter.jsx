import React from 'react';
import Actions from '../actions/Actions';
import {ContentStates, ContentStateStyles} from '../constants/Options';
import {PureRenderMixin} from 'react/addons';

var ContentStateFilter = React.createClass({
  mixins: [PureRenderMixin],

  propTypes: {
    selectedContentState: React.PropTypes.oneOf(ContentStates).isRequired
  },

  onFilterOptionClicked: function(option, event) {
    if (this.props.selectedContentState !== option) {
      Actions.changeContentState(option);
    }
  },

  render: function() {
    var contentStateFilter= this,
      selected = this.props.selectedContentState,
      clickHandler = this.onFilterOptionClicked,
      optionItems;

    optionItems = ContentStates.map(function(option, index) {
      var optionStyle = 'pill--' + ContentStateStyles[index],
          buttonStyle = 'pill pill--inline ';
          buttonStyle += selected === option ? optionStyle + ' is-active' : optionStyle;

      return (
        <span key={option} onClick={ clickHandler.bind(contentStateFilter, option) } className={buttonStyle}>
          {option}
        </span>
      );

    });
    return (
      <div className='l--pad-bottom-half'>{optionItems}</div>
    );
  }
});

export default ContentStateFilter;
