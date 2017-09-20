import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Form, FormGroup, ControlLabel, Button }
  from 'react-bootstrap'
import { Icon, TextInput, SelectableDropdown } from '../../components'
import Toggle from 'react-toggle'
/**
 * Reject Translations Administration panel
 */
export const MINOR = 'Minor'
export const MAJOR = 'Major'
export const CRITICAL = 'Critical'
const DO_NOT_RENDER = undefined

function priorityToTextState (priority) {
  switch (priority) {
    case CRITICAL:
      return 'text-danger'
    case MAJOR:
      return 'text-warning'
    case MINOR:
      return 'text-info'
  }
}

const priorityToDisplay =
  p => <span className={priorityToTextState(p)}>{p}</span>

class RejectionsForm extends Component {
  static propTypes = {
    entityId: PropTypes.number,
    priority: PropTypes.oneOf([
      MINOR,
      MAJOR,
      CRITICAL
    ]).isRequired,
    criteriaPlaceholder: PropTypes.string.isRequired,
    description: PropTypes.string.isRequired,
    onSave: PropTypes.func.isRequired,
    onDelete: PropTypes.func,
    editable: PropTypes.bool,
    // if it's in admin mode, we will allow user to update
    isAdminMode: PropTypes.bool.isRequired,
    // whether delete button shoud be displayed
    displayDelete: PropTypes.bool.isRequired,
    className: PropTypes.string,
    criterionId: PropTypes.string.isRequired
  }

  static defaultProps = {
    criterionId: 'review-criteria',
    editable: false,
    description: '',
    isAdminMode: false,
    displayDelete: true,
    onSave: () => {},
    onDelete: () => {}
  }

  constructor (props) {
    super(props)
    this.state = {
      description: this.props.description,
      isEditable: this.props.editable,
      priority: this.props.priority
    }
  }

  onEditableChange = e => {
    const checked = e.target.checked
    this.setState(prevState => ({
      isEditable: checked
    }))
  }
  onTextChange = e => {
    const text = e.target.value
    this.setState(prevState => ({
      description: text
    }))
  }
  onPriorityChange = p => {
    this.setState(prevState => ({
      priority: p
    }))
  }
  onSave = () => {
    this.props.onSave({
      ...this.state,
      id: this.props.entityId,
      editable: this.state.isEditable
    })
  }
  onDelete = () => {
    this.props.onDelete(this.props.entityId)
  }

  render () {
    const {
      editable,
      className,
      isAdminMode,
      displayDelete,
      criteriaPlaceholder,
      criterionId
    } = this.props
    const textState = priorityToTextState(this.state.priority)
    const title = <span className={textState}>{this.state.priority}</span>
    const priorityDisabled = !isAdminMode && !editable
    const deleteBtn = displayDelete
      ? (
      <Button bsStyle='danger' className={className} onClick={this.onDelete}>
        <Icon name='trash' className='s0 editicon' />
      </Button>
      ) : DO_NOT_RENDER
    const editableToggle = isAdminMode ? (
      <FormGroup controlId='formInlineEditable'>
        <ControlLabel>Editable</ControlLabel><br />
        <Toggle icons={false} onChange={this.onEditableChange}
          checked={this.state.isEditable} />
      </FormGroup>
      )
      : DO_NOT_RENDER
    const formBtn = isAdminMode ? (
      <FormGroup controlId='formInlineButtonEdit'>
        <ControlLabel>&nbsp;</ControlLabel><br />
        <Button bsStyle='primary' className={className} onClick={this.onSave}>
          <Icon name='edit' className='s0 editicon' />
        </Button>
        {deleteBtn}
      </FormGroup>
    ) : DO_NOT_RENDER
    return (
      <Form className='rejections' inline>
        <FormGroup className='flex-grow1' controlId='formInlineCriteria'>
          <ControlLabel>Criteria</ControlLabel><br />
          <TextInput multiline editable={isAdminMode || editable}
            type='text' numberOfLines={2} onChange={this.onTextChange}
            placeholder={criteriaPlaceholder} value={this.state.description} />
        </FormGroup>
        <FormGroup controlId='formInlinePriority'>
          <ControlLabel>Priority</ControlLabel><br />
          <SelectableDropdown
            id={criterionId + 'review-criteria-dropdown-basic'}
            onSelectDropdownItem={this.onPriorityChange}
            selectedValue={this.state.priority}
            title={title}
            valueToDisplay={priorityToDisplay}
            values={[MINOR, MAJOR, CRITICAL]}
            disabled={priorityDisabled}
          />
        </FormGroup>
        {editableToggle}
        {formBtn}
      </Form>
    )
  }
}

export default RejectionsForm
