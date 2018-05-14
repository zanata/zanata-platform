// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import { Icon } from '../../components'
import { isEmpty } from 'lodash'

import Button from 'antd/lib/button'
import 'antd/lib/button/style/index.less'
import Form from 'antd/lib/form'
import 'antd/lib/form/style/index.less'
import Input from 'antd/lib/input'
import 'antd/lib/input/style/index.less'
import Select from 'antd/lib/select'
import 'antd/lib/select/style/index.less'
import Switch from 'antd/lib/switch'
import 'antd/lib/switch/style/index.less'
import Tooltip from 'antd/lib/tooltip'
import 'antd/lib/tooltip/style/index.less'

const Option = Select.Option

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
    commentRequired: PropTypes.bool,
    // if it's in admin mode, we will allow user to update
    isAdminMode: PropTypes.bool.isRequired,
    key: PropTypes.number,
    // whether delete button shoud be displayed
    displayDelete: PropTypes.bool.isRequired,
    className: PropTypes.string,
    criterionId: PropTypes.string.isRequired
  }

  static defaultProps = {
    criterionId: 'review-criteria',
    commentRequired: false,
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
      isCommentRequired: this.props.commentRequired,
      priority: this.props.priority
    }
  }

  onEditableChange = e => {
    const checked = e.target.checked
    this.setState(_prevState => ({
      isCommentRequired: checked
    }))
  }
  onTextChange = e => {
    const text = e.target.value
    this.setState(_prevState => ({
      description: text
    }))
  }
  onPriorityChange = p => {
    this.setState(_prevState => ({
      priority: p
    }))
  }
  onSave = () => {
    this.props.onSave({
      ...this.state,
      id: this.props.entityId,
      commentRequired: this.state.isCommentRequired
    })
  }
  onDelete = () => {
    this.props.onDelete(this.props.entityId)
  }
  render () {
    const {
      commentRequired,
      className,
      isAdminMode,
      key,
      displayDelete,
      criteriaPlaceholder
    } = this.props
    const error = isEmpty(this.state.description)
    const priorityDisabled = !isAdminMode && !commentRequired
    const deleteBtn = displayDelete
      ? (
      <Tooltip title='Delete criteria'>
        <Button type='danger' className={className} onClick={this.onDelete}>
          <Icon name='trash' className='s0 iconEdit' />
        </Button>
      </Tooltip>
      ) : DO_NOT_RENDER
    const commentToggle = isAdminMode ? (
      <Form.Item label='Comment required'>
        <Switch
          checked={this.state.isCommentRequired}
          onChange={this.onEditableChange} />
      </Form.Item>
      )
      : DO_NOT_RENDER
    const formBtn = isAdminMode ? (
      <Form.Item>
        <Tooltip title='Save criteria'>
          <Button type='primary' className={className} onClick={this.onSave}
            disabled={error}>
            <Icon name='tick' className='s0 iconEdit' />
          </Button>
        </Tooltip>
        {deleteBtn}
      </Form.Item>
    ) : DO_NOT_RENDER
    return (
      <Form key={key} layout='inline'>
        <Form.Item label='Criteria'>
          { /* TODO: Fix layout style={{ width: '500px' }} */ }
          <Input.TextArea
            disabled={!isAdminMode}
            maxLength={255}
            onChange={this.onTextChange}
            placeholder={criteriaPlaceholder}
            rows={2}
            value={this.state.description} />
        </Form.Item>
        <Form.Item label='Priority'>
          <Select
            defaultValue={this.state.priority}
            disabled={priorityDisabled}
            onChange={this.onPriorityChange}
            label={this.state.priority} >
            <Option key={0} value={MINOR}>
              {priorityToDisplay(MINOR)}
            </Option>
            <Option key={1} value={MAJOR}>
              {priorityToDisplay(MAJOR)}
            </Option>
            <Option key={2} value={CRITICAL}>
              {priorityToDisplay(CRITICAL)}
            </Option>
          </Select>
        </Form.Item>
        {commentToggle}
        {formBtn}
      </Form>
    )
  }
}

export default RejectionsForm
