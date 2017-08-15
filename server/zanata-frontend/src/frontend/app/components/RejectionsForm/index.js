import React, { Component, PropTypes } from 'react'
import { Form, FormGroup, ControlLabel, DropdownButton, MenuItem, Button }
  from 'react-bootstrap'
import { Icon, TextInput } from '../../components'
/**
 * Reject Translations Administration panel
 */
class RejectionsForm extends Component {
  static PropTypes = {
    priority: PropTypes.oneOf([
      'Minor',
      'Major',
      'Critical'
    ]).isRequired,
    textState: PropTypes.oneOf([
      'text-info',
      'text-warning',
      'text-danger'
    ]).isRequired,
    subcatPlaceholder: PropTypes.string.isRequired,
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
        <Form className='rejections' inline>
          <FormGroup className='flex-grow1' controlId='formInlineCriteria'>
            <ControlLabel>Review Criteria</ControlLabel><br/>
            <TextInput className={this.props.focusClass} editable={this.props.editable} type='text' placeholder={this.props.criteriaPlaceholder}/>
          </FormGroup>
          <FormGroup className='flex-grow2' controlId='formInlineSubCat'>
            <ControlLabel>Sub-categories</ControlLabel><br/>
            <TextInput multiline={true} editable={this.props.editable} numberOfLines={2} type='text'
                       placeholder={this.props.subcatPlaceholder} />
          </FormGroup>
          <FormGroup controlId='formInlinePriority'>
            <ControlLabel>Priority</ControlLabel><br/>
            <DropdownButton bsStyle='default' title={title}
                            id='dropdown-basic'>
              <MenuItem><span className='text-info'>Minor</span></MenuItem>
              <MenuItem><span className='text-warning'>Major</span></MenuItem>
              <MenuItem><span className='text-danger'>Critical</span></MenuItem>
            </DropdownButton>
          </FormGroup>
          <FormGroup controlId='formInlineButtonEdit'>
            <ControlLabel>&nbsp;</ControlLabel><br/>
            <Button bsStyle='primary' className={this.props.className}>
              <Icon name='edit' className='s0 editicon'/>
            </Button>
          </FormGroup>
        </Form>
    )
  }
}

export default RejectionsForm
