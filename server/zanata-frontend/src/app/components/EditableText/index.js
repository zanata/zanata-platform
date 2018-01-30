import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import {Row} from 'react-bootstrap'
import {TextInput} from '../../components'

/**
 * Text input that can switch between text field and label
 * by using attribute `editing`
 */

class EditableText extends Component {
  static propTypes = {
    /**
     * String value for this text field
     */
    children: PropTypes.string,
    /**
     * Toggle whether the text field is in editable or not. Default is 'false'
     */
    editable: PropTypes.bool,
    /**
     * Toggle whether the text field is in editing mode or not.
     * Default is 'false'
     */
    editing: PropTypes.bool,
    /**
     * Placeholder
     */
    placeholder: PropTypes.string,
    /**
     * String to display if it is editable and children is
     * empty and there is not placeholder
     */
    emptyReadOnlyText: PropTypes.string,
    /**
     * Tooltip
     */
    title: PropTypes.string
  }

  constructor () {
    super()
    this.state = {
      focus: false
    }
  }

  handleClick = () => {
    this.setState({
      focus: true
    })
  }

  handleBlur = () => {
    this.setState({
      focus: false
    })
  }

  render () {
    const {
      children = '',
      editable = false,
      editing = false,
      emptyReadOnlyText = '',
      placeholder = '',
      title,
      ...props
    } = this.props

    if (editable && editing) {
      const cssClass = 'textInput textState ' +
        (editable ? 'editable' : 'text') + (children ? '' : ' u-textMuted')
      return (
        <TextInput className={cssClass}
          {...props}
          autoFocus={this.state.focus}
          onBlur={this.handleBlur}
          placeholder={placeholder}
          value={children}
        />
      )
    }
    const emptyText = editable ? placeholder : emptyReadOnlyText
    const content = children ||
      <span className='u-textMuted'>{emptyText}</span>
    return (
      <Row className='textInput textState text'
        onClick={this.handleClick} title={title}>
        {content}
      </Row>
      ) }
 }

export default EditableText
