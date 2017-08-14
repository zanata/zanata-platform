import React, { Component, PropTypes } from 'react'
import { Form, FormGroup, ControlLabel, DropdownButton, MenuItem, Button }
  from 'react-bootstrap'
import { Icon, TextInput } from '../../components'
/**
 * Reject Translations Administration panel
 */
class RejectTranslationAdmin extends Component {

  render () {
    return (
      <div className='container'>
        <h1>Reject translations settings</h1>
        <Form className='rejections' inline>
          <FormGroup className='flex-grow1' controlId='formInlineName'>
            <ControlLabel>Review Criteria</ControlLabel><br />
            <TextInput editable type='text' placeholder='Format' />
          </FormGroup>
          <FormGroup className='flex-grow2' controlId='formInlineSubCat'>
            <ControlLabel>Sub-categories</ControlLabel><br />
              <TextInput multiline={true} numberOfLines={2} type='text'
                 placeholder='mismatches, white-spaces' />
          </FormGroup>
          <FormGroup controlId='formInlinePriority'>
          <ControlLabel>Priority</ControlLabel><br />
          <DropdownButton bsStyle='default' title='Minor'
             id='dropdown-basic'>
            <MenuItem>Minor</MenuItem>
            <MenuItem>Major</MenuItem>
            <MenuItem>Critical</MenuItem>
          </DropdownButton>
          </FormGroup>
          <FormGroup controlId='formInlineButton'>
            <ControlLabel>&nbsp;</ControlLabel><br />
            <Button bsStyle='primary'>
                <Icon name='edit' className='s0 editicon' />
            </Button>
          </FormGroup>
        </Form>

        <Button bsStyle='primary' className='btn-left'>
          <Icon name='plus' className='s0' /> Add another review criteria
        </Button>
        <Button bsStyle='info'>
          <Icon name='refresh' className='s0' /> Revert to default
        </Button>
      </div>
    )
  }
}

export default RejectTranslationAdmin
