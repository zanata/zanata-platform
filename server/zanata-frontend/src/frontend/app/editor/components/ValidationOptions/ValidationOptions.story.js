import React, { PropTypes } from 'react'
import { storiesOf, action } from '@kadira/storybook'
import RealValidationOptions from '.'

/* Wrapper class for storybook.

 * The checkbox states will be stored and updated in the live
 * app. This wrapper stores the states so that we can see
 * them working in storybook too.
 */
class ValidationOptions extends React.Component {
  static propTypes = {
    states: PropTypes.object.isRequired,
    updateValidationOption: PropTypes.func.isRequired
  }
  constructor (props) {
    super(props)
    this.state = props.states
  }
  updateValidationOption = (validation, checked) => {
    // record the check state in the wrapper
    this.setState({ [validation]: checked })
    // call the real one that was passed in
    this.props.updateValidationOption(validation, checked)
  }
  render () {
    return (
      <RealValidationOptions
        updateValidationOption={this.updateValidationOption}
        states={this.state} />
    )
  }
}

const updateAction = action('updateValidationOption')
/*
 * See .storybook/README.md for info on the component storybook.
 */
storiesOf('ValidationOptions', module)
  .add('default', () => (
    <ValidationOptions
      updateValidationOption={updateAction}
      states={{
        'HTML/XML tags': false,
        'Java variables': false,
        'Leading/trailing newline (n)': false,
        'Positional printf (XSI extension)': false,
        'Printf variables': false,
        'Tab characters (t)': false,
        'XML entity reference': false
      }} />
  ))

  .add('half checked', () => (
    <ValidationOptions
      updateValidationOption={updateAction}
      states={{
        'HTML/XML tags': true,
        'Java variables': true,
        'Leading/trailing newline (n)': false,
        'Positional printf (XSI extension)': false,
        'Printf variables': false,
        'Tab characters (t)': true,
        'XML entity reference': true
      }} />
  ))

  .add('all checked', () => (
    <ValidationOptions
      updateValidationOption={updateAction}
      states={{
        'HTML/XML tags': true,
        'Java variables': true,
        'Leading/trailing newline (n)': true,
        'Positional printf (XSI extension)': true,
        'Printf variables': true,
        'Tab characters (t)': true,
        'XML entity reference': true
      }} />
  ))
