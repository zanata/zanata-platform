import stateChangeDispatchMiddleware from './state-change-dispatch'

/**
 * Middleware to set the browser title when workspace details change.
 *
 * e.g. My Project (1.0) to Afrikaans - Zanata Web Translation
 */
const titleUpdateMiddleware = stateChangeDispatchMiddleware(
  (dispatch, oldState, newState) => {
    const pre = oldState.context
    const { versionSlug } = newState.context

    const projectName = findProjectName(newState)
    const oldProjectName = findProjectName(oldState)

    const localeName = findLocaleName(newState)
    const oldLocaleName = findLocaleName(oldState)

    const workspaceChanged = oldProjectName !== projectName ||
      pre.versionSlug !== versionSlug ||
      oldLocaleName !== localeName

    if (workspaceChanged) {
      setTitle(projectName, versionSlug, localeName)
    }
  }
)

/**
 * Set the browser title.
 */
function setTitle (project, version, language) {
  if (window.document) {
    window.document.title =
      `${project} (${version}) to ${language} - Zanata Web Translation`
  }
}

/**
 * Look up the project name from the state, using the slug as a fallback.
 */
function findProjectName (state) {
  return state.headerData.context.projectVersion.project.name ||
    state.context.projectSlug
}

/**
 * Look up the locale name from the state, using the locale id as a fallback.
 */
function findLocaleName (state) {
  const localeId = state.context.lang
  const locale = state.ui.uiLocales[localeId]
  return locale && locale.name ? locale.name : localeId
}

export default titleUpdateMiddleware
