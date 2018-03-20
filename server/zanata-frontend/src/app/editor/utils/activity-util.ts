import {tuple} from '../../utils/tuple'
import {Status} from './phrase'
export const ALL = 'all'
export const COMMENTS = 'comments'
export const UPDATES = 'updates'
export const filterActivityTypes = tuple(ALL, COMMENTS, UPDATES)
export type ActivityFilter = typeof filterActivityTypes[number]

export interface User {
  name: string
}

export const activityTypes = tuple('comment', 'revision')
export type ActivityType = typeof activityTypes[number]

export interface ActivityItem {
  type: ActivityType,
  content: string,
  lastModifiedTime: Date,
  status: Status,
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
