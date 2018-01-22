import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Form, FormGroup, ControlLabel, Button, OverlayTrigger, Tooltip }
  from 'react-bootstrap'
import { Icon, TextInput, SelectableDropdown } from '../../components'
import Toggle from 'react-toggle'
import { isEmpty } from 'lodash'
/**
 * Reject Translations Administration panel
 */
export const MINOR = 'Minor'
export const MAJOR = 'Major'
export const CRITICAL = 'Critical'
const DO_NOT_RENDER = undefined

const tooltip = (<Tooltip id='tooltip'>Save criteria</Tooltip>)

function priorityToTextState (priority) {
  switch (priority) {
    case CRITICAL:
      return 'u-textDanger'
    case MAJOR:
      return 'u-textWarning'
    case MINOR:
      return 'u-textInfo'
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
    const error = isEmpty(this.state.description)
    const title = <span className={textState}>{this.state.priority}</span>
    const priorityDisabled = !isAdminMode && !editable
    const deleteBtn = displayDelete
      ? (
      <Button bsStyle='danger' className={className} onClick={this.onDelete}>
        <Icon name='trash' className='s0 iconEdit' />
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
    const formBtn = true ? (
      <FormGroup controlId='formInlineButtonEdit'>
        <ControlLabel>&nbsp;</ControlLabel><br />
        <OverlayTrigger placement='top' overlay={tooltip}>
          <Button bsStyle='primary' className={className} onClick={this.onSave}
            disabled={error}>
            <Icon name='tick' className='s0 iconEdit' />
          </Button>
        </OverlayTrigger>
        {deleteBtn}
      </FormGroup>
    ) : DO_NOT_RENDER
    return (
      <Form className='rejectionsForm' inline>
        <FormGroup className={error ? 'has-error u-flexGrow1' : 'u-flexGrow1'}
          controlId='formInlineCriteria'>
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
