import { createAction } from 'typesafe-actions'

export const MOVE_NEXT = 'MOVE_NEXT'
export const moveNext = createAction(MOVE_NEXT)

export const MOVE_PREVIOUS = 'MOVE_PREVIOUS'
export const movePrevious = createAction(MOVE_PREVIOUS)
