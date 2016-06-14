import assign from 'object-assign';
import {Dispatcher} from 'flux';

var UserMatrixDispatcher = assign({}, new Dispatcher(), Dispatcher.prototype, {

  /**
   * A bridge function between the views and the dispatcher, marking the action
   * as a view action.  Another variant here could be handleServerAction.
   * @param  {object} action The data coming from the view.
   */
  handleViewAction: function(action) {
    this.dispatch({
      source: 'VIEW_ACTION',
      action: action
    });
  }
});

export default UserMatrixDispatcher;
