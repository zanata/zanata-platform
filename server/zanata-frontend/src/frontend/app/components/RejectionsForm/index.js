import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Form, FormGroup, ControlLabel, DropdownButton, MenuItem, Button }
  from 'react-bootstrap'
import { Icon, TextInput } from '../../components'
/**
 * Reject Translations Administration panel
 */
export const MINOR = 'Minor'
export const MAJOR = 'Major'
export const CRITICAL = 'Critical'

class RejectionsForm extends Component {
  static PropTypes = {
    priority: PropTypes.oneOf([
      MINOR,
      MAJOR,
      CRITICAL
    ]).isRequired,
    textState: PropTypes.oneOf([
      'u-textIinfo',
      'u-textWarning',
      'u-textDanger'
    ]).isRequired,
    criteriaPlaceholder: PropTypes.string.isRequired,
    editable: PropTypes.boolean,
    editing: PropTypes.boolean,
    className: PropTypes.string
  }

  render() {
    const title = (
        <span className={this.props.textState}>{this.props.priority}</span>
    )
    return (
        <Form className='rejectionsForm' inline>
          <FormGroup className='flexGrow1' controlId='formInlineCriteria'>
            <ControlLabel>Criteria</ControlLabel><br/>
            <TextInput multiline={true}  editable={this.props.editable}
             type='text' numberOfLines={2} placeholder={this.props.criteriaPlaceholder}/>
          </FormGroup>
          <FormGroup controlId='formInlinePriority'>
            <ControlLabel>Priority</ControlLabel><br/>
            <DropdownButton bsStyle='default' title={title}
               id='dropdown-basic'>
              <MenuItem><span className='u-textInfo'>Minor</span></MenuItem>
              <MenuItem><span className='u-textWarning'>Major</span></MenuItem>
              <MenuItem><span className='u-textDanger'>Critical</span></MenuItem>
            </DropdownButton>
          </FormGroup>
          <FormGroup controlId='formInlineButtonEdit'>
            <ControlLabel>&nbsp;</ControlLabel><br/>
            <Button bsStyle='primary' className={this.props.className}>
              <Icon name='edit' className='s0 iconEdit'/>
            </Button>
            <Button bsStyle='danger' className={this.props.className}>
              <Icon name='trash' className='s0 iconEdit'/>
            </Button>
          </FormGroup>
        </Form>
    )
  }
}

export default RejectionsForm
