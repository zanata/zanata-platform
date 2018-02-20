export const MINOR = 'Minor'
export const MAJOR = 'Major'
export const CRITICAL = 'Critical'
export const priorities = [MINOR, MAJOR, CRITICAL]
export const UNSPECIFIEDTEXT = 'Unspecified Criteria'

export interface Criteria {
  id?: number,
  editable: boolean,
  description: string,
  priority: any
}

export const UNSPECIFIED: Criteria = {
  id: undefined,
  editable: true,
  description: UNSPECIFIEDTEXT,
  priority: MINOR
}

export const textState = (selectedPriority) => {
  if (selectedPriority === MAJOR) {
    return 'u-textWarning'
  } else if (selectedPriority === CRITICAL) {
    return 'u-textDanger'
  } else {
    return ''
  }
}
