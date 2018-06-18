import ValidationDisplayRules from './ValidationDisplayRules'

export enum State {
    Off, Warning, Error
}

export default interface ValidationAction {
    readonly rules: ValidationDisplayRules
    readonly id: string
    readonly label: string
    readonly description: string
    state: State
    readonly sourceExample: string
    readonly targetExample: string
    readonly exclusiveValidations: ValidationAction[]
    mutuallyExclusive(exclusiveValidations: ValidationAction)
    doValidate(source: string, target: string): string[]
}
