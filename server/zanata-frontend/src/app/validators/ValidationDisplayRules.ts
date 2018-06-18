import { State } from './ValidationAction'

/**
 * Holds display rules of this validation according to the state
 *
 * @author Alex Eng [aeng@redhat.com](mailto:aeng@redhat.com)
 */
class ValidationDisplayRules {
    public isEnabled: boolean = false
    public isLocked: boolean = false

    constructor(state: State) {
        this.updateRules(state)
    }
    /**
     * Update validation state Off : enabled = false, locked = false; Warning :
     * enabled = true, locked = false; Error : enabled = true, locked = true;
     */
    public updateRules(state: State) {
        if (state === State.Off) {
            this.isEnabled = false
            this.isLocked = false
        } else if (state === State.Warning) {
            this.isEnabled = true
            this.isLocked = false
        } else if (state === State.Error) {
            this.isEnabled = true
            this.isLocked = true
        }
    }
}

export default ValidationDisplayRules
