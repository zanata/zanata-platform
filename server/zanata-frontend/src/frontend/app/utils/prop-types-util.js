import PropTypes from 'prop-types'
import {processStatusCodes, entityStatuses} from './EnumValueUtils'

export const entityStatusPropType = PropTypes.oneOf(entityStatuses)

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

export const processStatusCodeType = PropTypes.oneOf(processStatusCodes)

export const processStatusType = PropTypes.shape({
  url: PropTypes.string.isRequired,
  percentageComplete: PropTypes.number.isRequired,
  statusCode: processStatusCodeType.isRequired
})
