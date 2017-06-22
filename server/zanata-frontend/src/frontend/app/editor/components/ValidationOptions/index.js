import React from 'react'
import PropTypes from 'prop-types'
import { Checkbox } from 'react-bootstrap'

const validations =
    ['HTML/XML tags',
      'Java variables',
      'Leading/trailing newline (n)',
      'Positional printf (XSI extension)',
      'Printf variables',
      'Tab characters (t)',
      'XML entity reference']

const ValidationOptions = ({states, updateValidationOption}) => {
  const checkboxes = validations.map((validation, index) => (
    <li key={index}>
      <ValidationCheckbox
        validation={validation}
        checked={states[validation]}
        onChange={updateValidationOption} />
    </li>
  ))
  return (
    <div>
      <h2 className="validation">Validation options</h2>
      <ul>
        {checkboxes}
      </ul>
    </div>
  )
}

ValidationOptions.propTypes = {
  states: PropTypes.shape({
    'HTML/XML tags': PropTypes.bool.isRequired,
    'Java variables': PropTypes.bool.isRequired,
    'Leading/trailing newline (n)': PropTypes.bool.isRequired,
    'Positional printf (XSI extension)': PropTypes.bool.isRequired,
    'Printf variables': PropTypes.bool.isRequired,
    'Tab characters (t)': PropTypes.bool.isRequired,
    'XML entity reference': PropTypes.bool.isRequired
  }).isRequired,
  updateValidationOption: PropTypes.func.isRequired
}

class ValidationCheckbox extends React.Component {
  static propTypes = {
    validation: PropTypes.string.isRequired,
    checked: PropTypes.bool.isRequired,
    /* Will be called with (validation, newValue) */
    onChange: PropTypes.func.isRequired
  }

  onChange = (event) => {
    this.props.onChange(this.props.validation, event.target.checked)
  }

  render () {
    const { validation, checked } = this.props
    return (
      <Checkbox checked={checked}
        onChange={this.onChange}>
        {validation}
      </Checkbox>
    )
  }
}

export default ValidationOptions
