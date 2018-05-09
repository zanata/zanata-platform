import ValidationDisplayRules from './ValidationDisplayRules'
import ValidationId from './ValidationId'

export enum State {
    Off, Warning, Error
}

interface ValidationAction {
    rules: ValidationDisplayRules
    id: ValidationId
    description: string
    state: State
    sourceExample: string
    targetExample: string
    exclusiveValidations: ValidationAction[]
    mutuallyExclusive(exclusiveValidations: ValidationAction)
}

export default ValidationAction
