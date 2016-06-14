import Dispatcher from '../dispatchers/UserMatrixDispatcher';
import {UserMatrixActionTypes} from '../constants/ActionTypes';


var UserMatrixActions = {
  changeDateRange: function(dateRangeOption) {
    Dispatcher.handleViewAction(
      {
        actionType: UserMatrixActionTypes.DATE_RANGE_UPDATE,
        data: dateRangeOption
      }
    );
  },

  changeContentState: function(contentState) {
    Dispatcher.handleViewAction(
      {
        actionType: UserMatrixActionTypes.CONTENT_STATE_UPDATE,
        data: contentState
      }
    );
  },

  onDaySelected: function(day) {
    Dispatcher.handleViewAction(
      {
        actionType: UserMatrixActionTypes.DAY_SELECTED,
        data: day
      }
    );
  },

  clearSelectedDay: function() {
    Dispatcher.handleViewAction(
      {
        actionType: UserMatrixActionTypes.DAY_SELECTED,
        data: null
      }
    );
  }
};

export default UserMatrixActions;
