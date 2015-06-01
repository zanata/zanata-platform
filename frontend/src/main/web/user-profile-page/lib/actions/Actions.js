import Dispatcher from '../dispatchers/UserMatrixDispatcher';
import ActionTypes from '../constants/ActionTypes';


var Actions = {
  changeDateRange: function(dateRangeOption) {
    Dispatcher.handleViewAction(
      {
        actionType: ActionTypes.DATE_RANGE_UPDATE,
        data: dateRangeOption
      }
    );
  },

  changeContentState: function(contentState) {
    Dispatcher.handleViewAction(
      {
        actionType: ActionTypes.CONTENT_STATE_UPDATE,
        data: contentState
      }
    );
  },

  onDaySelected: function(day) {
    Dispatcher.handleViewAction(
      {
        actionType: ActionTypes.DAY_SELECTED,
        data: day
      }
    );
  },

  clearSelectedDay: function() {
    Dispatcher.handleViewAction(
      {
        actionType: ActionTypes.DAY_SELECTED,
        data: null
      }
    );
  }

};

export default Actions;
