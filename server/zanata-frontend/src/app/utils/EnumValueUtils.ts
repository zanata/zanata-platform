import { tuple } from './tuple'

export const processStatusCodes = tuple(
  'NotAccepted', 'Waiting', 'Running', 'Finished', 'Cancelled', 'Failed')
export type ProcessStatusCode = typeof processStatusCodes[number]

const isStatusCodeEnded = (statusCode: ProcessStatusCode) => {
  return statusCode === 'Finished' || statusCode === 'Cancelled' ||
    statusCode === 'Failed'
}
/**
 * @returns whether a process status represents an ended process
 */
export function isProcessEnded (processStatus: {statusCode: ProcessStatusCode}) {
  return processStatus && isStatusCodeEnded(processStatus.statusCode)
}

export const entityStatuses = tuple('READONLY', 'ACTIVE', 'OBSOLETE')
export type EntityStatus = typeof entityStatuses[number]

export function isEntityStatusReadOnly (status: EntityStatus) {
  return status === 'READONLY'
}

export const internalTMChoice = tuple('SelectNone', 'SelectAny', 'SelectSome')
export type InternalTMChoice = typeof internalTMChoice[number]

/**
 * @param isFromAllProjects if we want to search TM from all projects
 * @param fromVersions
 */
export function toInternalTMSource (isFromAllProjects: boolean, fromVersions: string[]): {
  choice: InternalTMChoice;
  projectIterationIds?: string[];
} {
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
