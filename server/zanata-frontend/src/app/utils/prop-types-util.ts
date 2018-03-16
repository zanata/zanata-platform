import * as PropTypes from 'prop-types'
import {processStatusCodes, entityStatuses, EntityStatus} from './EnumValueUtils'

export const entityStatusPropType = PropTypes.oneOf(entityStatuses)

export const versionDtoPropType = PropTypes.shape({
  id: PropTypes.string.isRequired,
  status: entityStatusPropType
})

export interface VersionDto {
  id: string,
  // TODO does entityStatusPropType implicitly have isRequired?
  status?: EntityStatus,
}

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

export interface FromProjectVersion {
  projectSlug: string,
  version: VersionDto
}

export const processStatusCodeType = PropTypes.oneOf(processStatusCodes)

export const processStatusType = PropTypes.shape({
  url: PropTypes.string.isRequired,
  percentageComplete: PropTypes.number.isRequired,
  statusCode: processStatusCodeType.isRequired
})
