import PropTypes from 'prop-types'

export const entityStatusPropType = PropTypes.oneOf(['READONLY', 'ACTIVE'])

export const versionDtoPropType = PropTypes.shape({
  id: PropTypes.string.isRequired,
  status: entityStatusPropType
})

export const ProjectType = PropTypes.shape({
  id: PropTypes.string.isRequired,
  status: PropTypes.string.isRequired,
  title: PropTypes.string.isRequired,
  versions: PropTypes.arrayOf(versionDtoPropType).isRequired
})

export const LocaleType = PropTypes.shape({
  displayName: PropTypes.string.isRequired,
  localeId: PropTypes.string.isRequired,
  nativeName: PropTypes.string.isRequired
})

export const FromProjectVersionType = PropTypes.shape({
  projectSlug: PropTypes.string.isRequired,
  version: versionDtoPropType.isRequired
})

export const processStatusPropType = PropTypes.oneOf([
  'NotAccepted', 'Waiting', 'Running', 'Finished', 'Cancelled', 'Failed'])

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
