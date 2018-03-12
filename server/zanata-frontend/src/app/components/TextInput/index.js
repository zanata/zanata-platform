// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import * as ReactDOM from 'react-dom'
import TextareaAutosize from 'react-textarea-autosize'

/**
 * TextInput component <input> or <textArea> depending on property 'multiline'.
 */
class TextInput extends Component {
  static propTypes = {
    /**
     * HTML 'id' attribute
     */
    id: PropTypes.string,
    /**
     * HTML 'aria-label' attribute
     */
    accessibilityLabel: PropTypes.string,
    /**
     * HTML 'autocomplete' attribute
     */
    autoComplete: PropTypes.bool,
    /**
     * HTML 'autofocus' attribute
     */
    autoFocus: PropTypes.bool,
    /**
     * Toggle whether to clear text when component is focused
     */
    clearTextOnFocus: PropTypes.bool,
    /**
     * Value to display if empty when component is first initialized
     */
    defaultValue: PropTypes.string,
    /**
     * Toggle whether this is readonly or not.
     * HTML 'readonly' attribute
     */
    editable: PropTypes.bool,
    /**
     * HTML 'type' attribute for <input>
     */
    keyboardType: PropTypes.oneOf(['default', 'email-address', 'numeric',
      'phone-pad', 'url']),
    /**
     * Maximum length of text field. <input> 'maxlength' attribute
     */
    maxLength: PropTypes.number,
    /**
     * Maximum line of text area. <textarea> 'rows' attribute
     */
    maxNumberOfLines: PropTypes.number,
    /**
     * Toggle whether to it is text field or text area
     */
    multiline: PropTypes.bool,
    /**
     * Initial number of line to display for text area.
     * Default is '2'
     */
    numberOfLines: PropTypes.number,
    /**
     * Event handler for onBlur
     */
    onBlur: PropTypes.func,
    /**
     * Event handler for onKeyDown
     */
    onKeyDown: PropTypes.func,
    /**
     * Event handler for onChange
     */
    onChange: PropTypes.func,
    /**
     * Same as onChange
     */
    onChangeText: PropTypes.func,
    /**
     * Event handler for onFocus
     */
    onFocus: PropTypes.func,
    /**
     * Event handler for onSelect
     */
    onSelectionChange: PropTypes.func,
    /**
     * HTML 'placeholder' attribute
     */
    placeholder: PropTypes.string,
    /**
     * Toggle whether to display value in text or as password
     */
    secureTextEntry: PropTypes.bool,
    /**
     * Toggle whether to select text in text field
     * when component is focused
     */
    selectTextOnFocus: PropTypes.bool,
    /**
     * String value for this text field/text area
     */
    value: PropTypes.string
  }

  _onBlur = (e) => {
    const {onBlur} = this.props
    if (onBlur) {
      onBlur(e)
    }
  }

  _onChange = (e) => {
    const {onChange, onChangeText} = this.props
    if (onChangeText) onChangeText(e.target.value)
    if (onChange) onChange(e)
  }

  _onFocus = (e) => {
    const {clearTextOnFocus, onFocus, selectTextOnFocus} = this.props
    const node = ReactDOM.findDOMNode(this)
    if (clearTextOnFocus) node.value = ''
    if (selectTextOnFocus) node.select()
    if (onFocus) onFocus(e)
  }

  _onSelectionChange = (e) => {
    const {onSelectionChange} = this.props
    const {selectionDirection, selectionEnd, selectionStart} = e.target
    if (onSelectionChange) {
      const event = {
        selectionDirection,
        selectionEnd,
        selectionStart,
        nativeEvent: e.nativeEvent
      }
      onSelectionChange(event)
    }
  }

  _onKeyDown = (e) => {
    const {onKeyDown} = this.props
    if (onKeyDown) onKeyDown(e)
  }

  _onClear = () => {
    const node = ReactDOM.findDOMNode(this)
    node.value = ''
  }

  render () {
    const {
      id,
      accessibilityLabel,
      autoComplete,
      autoFocus,
      editable = true,
      keyboardType = 'default',
      maxLength,
      maxNumberOfLines,
      multiline = false,
      numberOfLines = 2,
      onBlur,
      onChange,
      onChangeText,
      onKeyDown,
      onSelectionChange,
      placeholder,
      secureTextEntry = false,
      value
    } = this.props

    let type

    switch (keyboardType) {
      case 'default':
        break
      case 'email-address':
        type = 'email'
        break
      case 'numeric':
        type = 'number'
        break
      case 'phone-pad':
        type = 'tel'
        break
      case 'url':
        type = 'url'
        break
      default:
        console.error('Unsupported keyboardType.', keyboardType)
        break
    }

    if (secureTextEntry) {
      type = 'password'
    }

    const propsCommon = {
      id: id,
      'aria-label': accessibilityLabel,
      autoComplete: autoComplete && 'on',
      autoFocus,
      className: 'textInput',
      maxLength,
      onBlur: onBlur && this._onBlur,
      onChange: (onChange || onChangeText) && this._onChange,
      onFocus: this._onFocus,
      onSelect: onSelectionChange && this._onSelectionChange,
      onKeyDown: (onKeyDown) && this._onKeyDown,
      placeholder,
      readOnly: !editable,
      value
    }

    if (multiline) {
      const propsMultiline = {
        ...propsCommon,
        maxRows: maxNumberOfLines || numberOfLines,
        minRows: numberOfLines
      }
      return <TextareaAutosize {...propsMultiline} />
    } else {
      const propsSingleline = {
        ...propsCommon,
        type
      }
      return <input {...propsSingleline} />
    }
  }
}

export default TextInput
