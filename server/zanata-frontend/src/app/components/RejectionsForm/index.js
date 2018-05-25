// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import { Icon } from '../../components'
import { isEmpty } from 'lodash'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Form from 'antd/lib/form'
import 'antd/lib/form/style/css'
import Input from 'antd/lib/input'
import 'antd/lib/input/style/css'
import Select from 'antd/lib/select'
import 'antd/lib/select/style/css'
import Switch from 'antd/lib/switch'
import 'antd/lib/switch/style/css'
import Tooltip from 'antd/lib/tooltip'
import 'antd/lib/tooltip/style/css'
import Row from 'antd/lib/row'
import 'antd/lib/row/style/css'
import Col from 'antd/lib/col'
import 'antd/lib/col/style/css'

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
        <Button type='danger' className='btn-danger' onClick={this.onDelete}>
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
        <span className='pr3'>
          <Tooltip title='Save criteria'>
            <Button type='primary'
              onClick={this.onSave}
              disabled={error}>
              <Icon name='tick' className='s0 iconEdit' />
            </Button>
          </Tooltip>
        </span>
        {deleteBtn}
      </Form.Item>
    ) : DO_NOT_RENDER
    return (
      <Form key={key} layout='inline'>
        <Row className='pb4'>
          <Col span={12}>
            <Form.Item label='Criteria' className='w-100'>
              { /* TODO: Fix layout style={{ width: '500px' }} */ }
              <Input.TextArea
                disabled={!isAdminMode}
                maxLength={255}
                onChange={this.onTextChange}
                placeholder={criteriaPlaceholder}
                rows={2}
                className='w-90'
                value={this.state.description} />
            </Form.Item>
          </Col>
          <Col span={4}>
            <Form.Item label='Priority'>
              <Select
                key={key}
                defaultValue={this.state.priority}
                disabled={priorityDisabled}
                onChange={this.onPriorityChange}
                label={this.state.priority} >
                <Select.Option key={0} value={MINOR}>
                  {priorityToDisplay(MINOR)}
                </Select.Option>
                <Select.Option key={1} value={MAJOR}>
                  {priorityToDisplay(MAJOR)}
                </Select.Option>
                <Select.Option key={2} value={CRITICAL}>
                  {priorityToDisplay(CRITICAL)}
                </Select.Option>
              </Select>
            </Form.Item>
          </Col>
          <Col span={4}>
            {commentToggle}
          </Col>
          <Col span={4}>
            {formBtn}
          </Col>
        </Row>
      </Form>
    )
  }
}

export default RejectionsForm
