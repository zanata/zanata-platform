import {Status} from './phrase'
import * as t from 'io-ts'
import * as PropTypes from "prop-types"

export const ALL = 'all'
export const COMMENTS = 'comments'
export const UPDATES = 'updates'

const filterActivityLiterals = t.tuple([t.literal(ALL), t.literal(COMMENTS), t.literal(UPDATES)])
const filterActivityTypes = filterActivityLiterals.types.map(lit => lit.value)
export const filterActivityPropType = PropTypes.oneOf(filterActivityTypes)
export const ActivityFilterUnion = t.union(filterActivityLiterals.types)
export type ActivityFilter = t.TypeOf<typeof ActivityFilterUnion>

export interface User {
  name?: string,
  username?: string
}

export enum activityTypes {
  comment = "comment",
  revision = "revision",
}

export type ActivityType = typeof activityTypes[number]

export interface ActivityItem {
  type: ActivityType,
  content: string,
  commentText?: string,
  lastModifiedTime: Date,
  status?: Status,
  user: User
}

export interface ActivityItemList extends Array<ActivityItem> {}

// A SelectButtonList button item
// TODO: convert SelectButtonList and SelectButton to typescript with this interface
export interface SelectButtonData {
  id: ActivityFilter,
  icon: string,
  label: string
}

export const filterButtons: SelectButtonData[] = [
  {
    id: ALL,
    icon: 'clock',
    label: 'All'
  },
  {
    id: COMMENTS,
    icon: 'comment',
    label: 'Comments'
  },
  {
    id: UPDATES,
    icon: 'refresh',
    label: 'Updates'
  }
]
