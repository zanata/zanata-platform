import React from 'react'
import { storiesOf, action } from '@kadira/storybook'
import { Col, ControlLabel, FormGroup, FormControl, Form, InputGroup,
  Checkbox, Radio } from 'react-bootstrap'

storiesOf('Forms', module)
    .add('default', () => (
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
  ))

    .add('validation-states', () => (
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
    ))

    .add('fieldsets', () => (
    <Form componentClass='fieldset' horizontal>
<FormGroup controlId='formValidationError3' validationState='error'>
    <Col componentClass={ControlLabel} xs={3}>
    Input with error
</Col>
<Col xs={9}>
    <FormControl type='text' />
    <FormControl.Feedback />
    </Col>
    </FormGroup>
    <FormGroup controlId='formValidationSuccess4'
validationState='success'>
    <Col componentClass={ControlLabel} xs={3}>
    Input group with success
</Col>
<Col xs={9}>
    <InputGroup>
    <InputGroup.Addon>@</InputGroup.Addon>
    <FormControl type='text' />
    </InputGroup>
    <FormControl.Feedback />
    </Col>
    </FormGroup>
    </Form>
    ))

    .add('inline', () => (
    <Form componentClass='fieldset' inline>
<FormGroup controlId='formValidationWarning4'
validationState='warning'>
    <ControlLabel>Input with warning</ControlLabel>
{' '}
  <FormControl type='text' />
  <FormControl.Feedback />
  </FormGroup>
  {' '}
<FormGroup controlId='formValidationError4' validationState='error'>
  <ControlLabel>Input group with error</ControlLabel>
  {' '}
  <InputGroup>
    <InputGroup.Addon>@</InputGroup.Addon>
    <FormControl type='text' />
  </InputGroup>
  <FormControl.Feedback />
</FormGroup>
</Form>
    ))
        .add('success', () => (

    <Checkbox validationState='success'>
    Checkbox with success
</Checkbox>
        ))

.add('warning', () => (
<Radio validationState='warning'>
    Radio with warning
</Radio>
))

.add('error', () => (
<Checkbox validationState='error'>
    Checkbox with error
</Checkbox>
    ))
