import React, { Component, PropTypes } from 'react'
import { merge } from 'lodash'
import Row from 'react-bootstrap'
import TextInput from '../../components'

const classes = {

    text: {
        base: {
            ai: 'Ai(c)',
            bd: 'Bd(bd2) Bdc(t)',
            c: 'Cur(t)',
            h: 'H(r1h)',
            lineClamp: 'LineClamp(1,36px)',
            p: 'Px(rq) Py(re)',
            w: 'W(100%)'
        },
        editable: {
            brds: 'Bdrs(rq)',
            trs: 'Trs(aeo)',
            hover: {
                bd: 'editable:h_Bd(bd2) editable:h_Bdc(neutral)'
            }
        },
        placeholder: {
            c: 'C(muted)'

    }
}

/**
 * Text input that can switch between text field and label
 * by using attribute `editing`
 */
class EditableText extends Component {
    constructor () {
        super()
        this.state = {
            focus: false
        }
    }
    handleClick () {
        this.setState({ focus: true })
    }
    handleBlur () {
        this.setState({ focus: false })
    }
    render () {
        const {
            children = '',
            editable = false,
            editing = false,
            emptyReadOnlyText = '',
            placeholder = '',
            theme,
            title,
            ...props
        } = this.props
        const themed = merge({}, classes, theme)
        const textStateClasses = {
            base: merge({},
                themed.text.base,
                editable && classes.text.editable,
                !children && classes.text.placeholder
            )
        }
        /* eslint-disable react/jsx-no-bind */
        if (editable && editing) {
            return (
                <TextInput
                    {...props}
                    autoFocus={this.state.focus}
                    onBlur={::this.handleBlur}
                    placeholder={placeholder}
                    ref={(ref) => { this.textInput = ref }}
                    value={children}
                />
            )
        }
        /* eslint-enable react/jsx-no-bind */
        const emptyText = editable ? placeholder : emptyReadOnlyText
        const text = children || emptyText
        return (CD
            <Row className={textStateClasses}
                 onClick={::this.handleClick} title={title}>
                {text}
            </Row>
        )
    }
}

EditableText.propTypes = {
    /**
     * String value for this text field
     */
    children: PropTypes.string,
    /**
     * Toggle whether the text field is in editable or not. Default is 'false'
     */
    editable: PropTypes.bool,
    /**
     * Toggle whether the text field is in editing mode or not. Default is 'false'
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

export default EditableText
