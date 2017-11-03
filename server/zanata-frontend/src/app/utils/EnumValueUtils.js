
export const processStatusCodes = [
  'NotAccepted', 'Waiting', 'Running', 'Finished', 'Cancelled', 'Failed']

const isStatusCodeEnded = statusCode => {
  return statusCode === 'Finished' || statusCode === 'Cancelled' ||
    statusCode === 'Failed'
}
/**
 * @param {{statusCode: string}} processStatus
 * @returns {boolean} whether a process status represents an ended process
 */
export function isProcessEnded (processStatus) {
  return processStatus && isStatusCodeEnded(processStatus.statusCode)
}

export const entityStatuses = ['READONLY', 'ACTIVE', 'OBSOLETE']
export function isEntityStatusReadOnly (status) {
  return status === 'READONLY'
}

export const internalTMChoice = ['SelectNone', 'SelectAny', 'SelectSome']

/**
 *
 * @param {boolean} isFromAllProjects if we want to search TM from all projects
 * @param {Array.<string>} fromVersions
 * @returns {*}
 */
export function toInternalTMSource (isFromAllProjects, fromVersions) {
  if (isFromAllProjects) {
    return {
      choice: 'SelectAny'
    }
  } else if (fromVersions.length === 0) {
    return {
      choice: 'SelectNone'
    }
  }
  return {
    choice: 'SelectSome',
    projectIterationIds: fromVersions
  }
}

/**
 * TM merge rules
 */
export const IGNORE_CHECK = 'IGNORE_CHECK'
export const FUZZY = 'FUZZY'
export const REJECT = 'REJECT'
