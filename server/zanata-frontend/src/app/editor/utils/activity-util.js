export const ALL = 'all'
export const COMMENTS = 'comments'
export const UPDATES = 'updates'
export const filterActivityTypes = [ALL, COMMENTS, UPDATES]
export const activityItems = [
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
