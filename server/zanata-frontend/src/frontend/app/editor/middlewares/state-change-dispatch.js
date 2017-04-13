/**
 * Generic middleware for dispatching actions in response to state changes.
 *
 * Create:
 *   stateChangeDispatchMiddleware(
 *     (dispatch, oldState, newState) => {
 *       // compare states
 *       // decide whether to dispatch an action
 *     },
 *     (dispatch, oldState, newState) => {
 *       // compare states
 *       // decide whether to dispatch an action
 *     })
 *
 * You can give it as many callbacks as you like, and it will call each
 * in the order they are provided. Use this to separate unrelated comparisons
 * to different blocks or files.
 *
 * CAUTION:
 *  You can make an infinite loop of state-change->action->state-change
 *  Do not dispatch an action that will change the state you are observing.
 *
 * It is ok to have a cascade of dispatches, but make absolutely sure that
 * there is an end to it.
 *
 * e.g.
 *   projectSlug/versionSlug changed -> fetch document list
 *   -> selected doc changed -> fetch phrase list
 *   -> (end, nothing responding to change in phrase list)
 */
const stateChangeDispatchMiddleware =
  (...callbacks) => store => next => action => {
    const stateBefore = store.getState()
    const result = next(action)
    const stateAfter = store.getState()
    callbacks.forEach(callback => {
      // Note :: is shorthand for foo.fun.bind(foo)
      //   it lets you pass an instance function around as a callback
      //   without messing up the 'this' binding
      callback(::store.dispatch, stateBefore, stateAfter)
    })
    return result
  }

export default stateChangeDispatchMiddleware
