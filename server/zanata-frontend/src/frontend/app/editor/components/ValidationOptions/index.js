import React from 'react'
import { Checkbox } from 'react-bootstrap'

const validations = ['HTML/XML tags', 'Java variables', 'Leading/trailing' +
' newline (n)', 'Positional printf (XSI extension)', 'Printf variables',
  'Tab characters (t)', 'XML entity reference']

const ValidationOptions = () => {
  const checkboxes = validations.map((validation, index) => (
    <li key={index}>
      <Checkbox checked>
        {validation}
      </Checkbox>
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

export default ValidationOptions

