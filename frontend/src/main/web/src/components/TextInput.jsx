import React, { Component, PropTypes } from 'react'
import ReactDOM from 'react-dom'
import TextareaAutosize from 'react-textarea-autosize'
import { flattenThemeClasses } from '../utils/styleUtils'
import { isEqual } from 'lodash'

const classes = {
  base: {
    ap: 'Ap(n)',
    bgc: 'Bgc(t)',
    bdc: 'Bdc(neutral)',
    bdw: 'Bdw(2px)',
    bds: 'Bds(s)',
    bdrs: 'Bdrs(rq)',
    bxz: 'Bxz(bb)',
    c: 'C(i)',
    ff: 'Ff(inh)',
    fw: 'Fw(inh)',
    fz: 'Fz(inh)',
    fs: 'Fs(inh)',
    o: 'O(n)',
    p: 'Px(rq) Py(re)',
    ph: 'Ph(neutral)',
    focus: {
      bdc: 'Bdc(pri):f'
    }
  }
}

/**
 * TextInput component <input> or <textArea> depending on property 'multiline'.
 */
class TextInput extends Component {
  constructor () {
    super()
  }

  _onBlur (e) {
    const { onBlur } = this.props
    if (onBlur) {
      onBlur(e)
    }
  }
  _onChange (e) {
    const { onChange, onChangeText } = this.props
    if (onChangeText) onChangeText(e.target.value)
    if (onChange) onChange(e)
  }

  _onFocus (e) {
    const { clearTextOnFocus, onFocus, selectTextOnFocus } = this.props
    const node = ReactDOM.findDOMNode(this)
    if (clearTextOnFocus) node.value = ''
    if (selectTextOnFocus) node.select()
    if (onFocus) onFocus(e)
  }

  _onSelectionChange (e) {
    const { onSelectionChange } = this.props
    const { selectionDirection, selectionEnd, selectionStart } = e.target
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

  _onKeyDown (e) {
    const { onKeyDown } = this.props
    if (onKeyDown) onKeyDown(e)
  }

  _onClear () {
    const node = ReactDOM.findDOMNode(this)
    node.value = ''
  }

  render () {
    const {
      id,
      accessibilityLabel,
      autoComplete,
      autoFocus,
      defaultValue,
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
      theme,
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
      className: flattenThemeClasses(classes, theme),
      defaultValue,
      maxLength,
      onBlur: onBlur && ::this._onBlur,
      onChange: (onChange || onChangeText) && ::this._onChange,
      onFocus: ::this._onFocus,
      onSelect: onSelectionChange && ::this._onSelectionChange,
      onKeyDown: (onKeyDown) && ::this._onKeyDown,
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
      return <TextareaAutosize  {...propsMultiline} />
    } else {
      const propsSingleline = {
        ...propsCommon,
        type
      }
      return <input {...propsSingleline}/>
    }
  }
}

TextInput.propTypes = {
  accessibilityLabel: PropTypes.string,
  autoComplete: PropTypes.bool,
  autoFocus: PropTypes.bool,
  clearTextOnFocus: PropTypes.bool,
  defaultValue: PropTypes.string,
  editable: PropTypes.bool,
  keyboardType: PropTypes.oneOf(['default', 'email-address', 'numeric',
    'phone-pad', 'url']),
  maxLength: PropTypes.number,
  maxNumberOfLines: PropTypes.number,
  multiline: PropTypes.bool,
  numberOfLines: PropTypes.number,
  onBlur: PropTypes.func,
  onKeyDown: PropTypes.func,
  onChange: PropTypes.func,
  onChangeText: PropTypes.func,
  onFocus: PropTypes.func,
  onSelectionChange: PropTypes.func,
  placeholder: PropTypes.string,
  placeholderTextColor: PropTypes.string,
  secureTextEntry: PropTypes.bool,
  selectTextOnFocus: PropTypes.bool,
  value: PropTypes.string
}

export default TextInput
