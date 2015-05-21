import React from 'react/addons';
import {ContentStates, ContentStateStyles} from '../constants/Options';
import Actions from '../actions/Actions';
import {PureRenderMixin} from 'react/addons';
import dateUtil from '../utils/DateHelper';

var DayMatrix = React.createClass({
  mixins: [PureRenderMixin],

  propTypes: {
    dateLabel: React.PropTypes.string.isRequired,
    date: React.PropTypes.string.isRequired,
    wordCount: React.PropTypes.number.isRequired,
    selectedDay: React.PropTypes.string
  },


  handleDayClick: function(event) {
    var dayChosen = this.props.date;
    if (this.props.selectedDay == dayChosen) {
      // click the same day again will cancel selection
      Actions.clearSelectedDay();
    } else {
      Actions.onDaySelected(dayChosen);
    }
  },

  render: function() {
    var cx = React.addons.classSet,
      selectedContentState = this.props.selectedContentState,
    // Note: this will make this component impure. But it will only become
    // impure when you render it between midnight, e.g. two re-render attempt
    // happen across two days with same props, which I think it's ok.
      dateIsInFuture = dateUtil.isInFuture(this.props.date),
      wordCount = dateIsInFuture ? '' : this.props.wordCount,
      rowClass;

    rowClass = {
      'cal__day': true,
      'is-disabled': dateIsInFuture,
      'is-active': this.props.date === this.props.selectedDay
    };



    ContentStates.forEach(function(option, index) {
      rowClass[ContentStateStyles[index]] = selectedContentState === option;
    });

    return (
      <td className={cx(rowClass)} onClick={this.handleDayClick} title={this.props.wordCount + ' words'}>
        <div className="cal__date">{this.props.dateLabel}</div>
        <div className="cal__date-info">{wordCount}</div>
      </td>
    );
  }
});

export default DayMatrix;
