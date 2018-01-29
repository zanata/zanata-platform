import * as React from 'react'
import { storiesOf } from '@storybook/react'
import { Col, ControlLabel, FormGroup, FormControl, Form, InputGroup,
  Checkbox, Radio, Well } from 'react-bootstrap'

storiesOf('Form', module)
    .add('default', () => (
        <span>
         <h2><img src="https://i.imgur.com/v4qLk4p.png" width="42px" />Forms</h2>
          <Well bsSize='large'><ul><li><a href="https://react-bootstrap.github.io/components.html#forms">Props for react-boostrap Form component</a></li></ul></Well>
        <Form>
          <FormGroup>
            <FormControl type='text' placeholder='Text' />
            <FormControl type='text' disabled placeholder='disabled' />
          </FormGroup>
          <FormGroup bsSize='lg'>
            <FormControl type='text' placeholder='large' />
          </FormGroup>
          <FormGroup bsSize='sm'>
            <FormControl type='text' placeholder='small' />
          </FormGroup>
        </Form>
          <hr />
          <h3>Input sizes</h3>
          <p>Use <code>bsSize</code> on <code>FormGroup</code> or <code>InputGroup</code> to change the size of inputs. It also works with add-ons and most other options.</p>
        </span>
    ))

    .add('validation states', () => (
        <span>
          <h2><img src="https://i.imgur.com/v4qLk4p.png" width="42px" />Form validation states</h2>
          <Well bsSize="large">Set validationState to one of 'success', 'warning' or 'error' to show validation state. Set <code>validationState</code> to null (or undefined) to hide validation state. Add <code>FormControl.Feedback</code> for a feedback icon when validation state is set.</Well>
        <Form>
          <FormGroup controlId='formValidationSuccess1'
                     validationState='success'>
            <ControlLabel>Input with success</ControlLabel>
            <FormControl type='text' />
          </FormGroup>
          <FormGroup controlId='formValidationWarning1'
                     validationState='warning'>
            <ControlLabel>Input with warning</ControlLabel>
            <FormControl type='text' />
          </FormGroup>
          <FormGroup controlId='formValidationError1' validationState='error'>
            <ControlLabel>Input with error</ControlLabel>
            <FormControl type='text' />
          </FormGroup>
          <FormGroup controlId='formValidationWarning3'
                     validationState='warning'>
            <ControlLabel>Input group with warning</ControlLabel>
            <InputGroup>
              <InputGroup.Addon>@</InputGroup.Addon>
              <FormControl type='text' />
            </InputGroup>
            <FormControl.Feedback />
          </FormGroup>
        </Form>
          <h3>Checkboxes and radios</h3>
        <Checkbox validationState='success'>
         Checkbox with success
        </Checkbox>
        <Radio validationState='warning'>
          Radio with warning
        </Radio>
        <Checkbox validationState='error'>
          Checkbox with error
        </Checkbox>
      </span>
    ))
