import React, { PropTypes } from 'react'
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
  const checkboxes = validations.map((validation, index) => {
    const isChecked = !!states[validation]
    return (
      <li key={index}>
        <Checkbox checked={isChecked} onClick={updateValidationOption}>
          {validation}
        </Checkbox>
      </li>
    )
  })
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

export default ValidationOptions
