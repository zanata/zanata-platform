export const MINOR = 'Minor'
export const MAJOR = 'Major'
export const CRITICAL = 'Critical'
export const priorities = [MINOR, MAJOR, CRITICAL]

export const textState = (selectedPriority) => {
  if (selectedPriority === MAJOR) {
    return 'u-textWarning'
  } else if (selectedPriority === CRITICAL) {
    return 'u-textDanger'
  } else {
    return ''
  }
}
