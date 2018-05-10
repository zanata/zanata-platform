import ValidationDisplayRules from './ValidationDisplayRules'
import ValidationId from './ValidationId'

export enum State {
    Off, Warning, Error
}

export interface ValidationAction {
    readonly rules: ValidationDisplayRules
    readonly id: ValidationId
    readonly description: string
    state: State
    readonly sourceExample: string
    readonly targetExample: string
    readonly exclusiveValidations: ValidationAction[]
    mutuallyExclusive(exclusiveValidations: ValidationAction)
}
