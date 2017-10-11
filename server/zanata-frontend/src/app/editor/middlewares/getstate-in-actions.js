/**
 * Middleware that just attaches the getState() function to every action.
 *
 * This is used for handling actions that need to refer to state that is not
 * avalable in the particular reducer.
 *
 * This is a bit of a hack, ideally the state would be structured so that all
 * state needed by a reducer is present in its particular state tree.
 * This just makes it easy to keep using the default combineReducers().
 *
 * An alternative is to include the specific needed state in the component where
 * it dispatches the action, or in the action creator by making it a thunk and
 * using dispatch() and getState() in the thunk to generate an action with all
 * the required state.
 *
 * Another alternative is to stop using combineReducers() and instead manually
 * call other reducer functions - that would give an opportunity to pass in
 * additional state to the custom reducer functions as needed.
 */

export default store => next => action => {
  const getState = :: store.getState
  return next({
    ...action,
    getState
  })
}
