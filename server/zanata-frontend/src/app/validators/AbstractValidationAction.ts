import ValidationAction, { State } from './ValidationAction'
import ValidationDisplayRules from './ValidationDisplayRules'
import ValidationMessages from './ValidationMessages'

/**
 *
 * @author Alex Eng [aeng@redhat.com](mailto:aeng@redhat.com)
 *
 * @see HtmlXmlTagValidation
 *
 * @see JavaVariablesValidation
 *
 * @see NewlineLeadTrailValidation
 *
 * @see PrintfVariablesValidation
 *
 * @see PrintfXSIExtensionValidation
 *
 * @see TabValidation
 *
 * @see XmlEntityValidation
 */
abstract class AbstractValidationAction implements ValidationAction {
  public readonly id: string
  public readonly messages: ValidationMessages
  public readonly label: string
  public readonly locale: string
  public readonly description: string
  public readonly sourceExample: string
  public readonly targetExample: string

  public readonly rules: ValidationDisplayRules

  private _state: State = State.Warning
  public get state() {return this._state}
  public set state(state: State) {
    this._state = state
    this.rules.updateRules(state)
  }
  private exclusiveVals: ValidationAction[]

  public get exclusiveValidations() {
    return this.exclusiveVals
  }

  constructor(messages: ValidationMessages, locale?: string) {
    this.messages = messages
    this.locale = locale ? locale : 'en-US' // default to en-US locale
    this.rules = new ValidationDisplayRules(this.state)
  }

  public validate(source?: string, target?: string): string[] {
    if (target && source) {
        return this.doValidate(source, target)
    } else {
      return []
    }
  }
  // TODO: Turn into setter for exclusiveValidations
  public mutuallyExclusive(...exclusiveValidations: ValidationAction[]) {
    this.exclusiveVals = exclusiveValidations
  }
  public abstract doValidate(source: string, target: string): string[]
}

export default AbstractValidationAction
