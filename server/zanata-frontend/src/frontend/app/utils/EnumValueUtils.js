
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
