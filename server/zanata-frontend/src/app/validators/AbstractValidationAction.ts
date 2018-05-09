import ValidationAction, { State } from './ValidationAction'
import ValidationDisplayRules from './ValidationDisplayRules'
import ValidationId from './ValidationId'
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

  public id: ValidationId
  public description: string
  public messages: ValidationMessages
  public sourceExample: string
  public targetExample: string

  public rules: ValidationDisplayRules
  public state: State = State.Warning
  set State(state) {
    this.rules.updateRules(state)
  }

  public exclusiveValidations: ValidationAction[]
  get ExclusiveValidations(): ValidationAction[] {
    return this.exclusiveVals;
  }
  private exclusiveVals: ValidationAction[]

  constructor(id: ValidationId, description: string, messages: ValidationMessages) {
    this.id = id
    this.description = description
    this.messages = messages
    this.rules = new ValidationDisplayRules(this.state)
  }

  public validate(source?: string, target?: string): string[] {
    if (target && source) {
        return this.doValidate(source, target)
    } else {
      return []
    }
  }

  public mutuallyExclusive(exclusiveValidations: ValidationAction) {
    this.exclusiveVals = [exclusiveValidations]
  }

  protected abstract doValidate(source: string, target: string): string[]
}
