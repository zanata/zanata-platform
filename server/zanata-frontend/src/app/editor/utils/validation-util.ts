import {tuple} from '../../utils/tuple'

export const ERROR = 'Error'
export const WARNING = 'Warning'
export const OFF = 'Off'

const validationstatuses = tuple(
  ERROR,
  WARNING,
  OFF
)

export type ValidationStatus = typeof validationstatuses[number]
