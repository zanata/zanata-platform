import * as PropTypes from 'prop-types'
import {processStatusCodes, entityStatuses, EntityStatus, ProcessStatusCode} from './EnumValueUtils'
import { getPropTypes, PropTypeable } from 'prop-types-ts';
import * as t from 'io-ts'

// sfc() is based on https://github.com/gcanti/prop-types-ts/issues/15#issuecomment-378163613

// If you want to specify the type U (eg HTMLAttributes<HTMLSpanElement>),
// you also have to pass in the type T (`typeof type` where 'type' is the first arg),
// even though it could be inferred.
// Keep an eye on these TypeScript issues for better inferencing:
// https://github.com/Microsoft/TypeScript/issues/20122
// https://github.com/Microsoft/TypeScript/issues/10571
// https://github.com/Microsoft/TypeScript/pull/23696
export const sfc = <T extends PropTypeable, U>
  (type: T, f: React.SFC<t.TypeOf<T>>): React.SFC<t.TypeOf<T> & U> => {
  f.propTypes = getPropTypes(type) as any
  return f
}

export const entityStatusPropType = PropTypes.oneOf(entityStatuses)

export const versionDtoPropType = PropTypes.shape({
  id: PropTypes.string.isRequired,
  status: entityStatusPropType.isRequired
})

// this shape seems to be org.zanata.rest.search.dto.ProjectVersionSearchResult (partial)
export interface VersionDto {
  id: string,
  status: EntityStatus,
}

// this shape seems to be org.zanata.rest.search.dto.ProjectSearchResult (partial)
export interface ProjectSearchResult {
  id: string
  status: string
  title: string
  versions: VersionDto[]
}
// matches ProjectSearchResult above
export const ProjectType = PropTypes.shape({
  id: PropTypes.string.isRequired,
  status: PropTypes.string.isRequired,
  title: PropTypes.string.isRequired,
  versions: PropTypes.arrayOf(versionDtoPropType).isRequired
})

// in future, it would be good to use a tag type for locale IDs
// https://github.com/Microsoft/TypeScript/issues/4895
export type LocaleId = string

export interface Locale {
  displayName: string
  localeId: LocaleId
  nativeName: string
}

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

export interface ProcessStatus {
  url: string
  percentageComplete: number
  statusCode: ProcessStatusCode
}
