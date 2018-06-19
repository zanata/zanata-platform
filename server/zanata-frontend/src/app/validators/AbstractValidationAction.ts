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
  public readonly locale: string
  public abstract readonly id: string
  public abstract readonly label: string
  public abstract readonly description: string
  public abstract readonly sourceExample: string
  public abstract readonly targetExample: string

  public readonly rules: ValidationDisplayRules
  protected abstract messages: ValidationMessages

  private _state: State = State.Warning
  public get state() {return this._state}
  public set state(state: State) {
    this._state = state
    this.rules.updateRules(state)
  }
  private exclusiveVals: ValidationAction[]

  protected constructor(locale: string) {
    this.locale = locale
    this.rules = new ValidationDisplayRules(this.state)
    this.exclusiveVals = []
  }

  public get exclusiveValidations() {
    return this.exclusiveVals
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
