
export const MOVE_NEXT = Symbol('MOVE_NEXT')
export function moveNext () {
  return { type: MOVE_NEXT }
}

export const MOVE_PREVIOUS = Symbol('MOVE_PREVIOUS')
export function movePrevious () {
  return { type: MOVE_PREVIOUS }
}
