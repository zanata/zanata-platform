// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import { connect } from 'react-redux'
import { isEmpty, debounce } from 'lodash'
import { Icon } from '../../components'
import Autosuggest from 'react-autosuggest'
import Modal from 'antd/lib/modal'
import 'antd/lib/modal/style/index.less'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/index.less'
import Checkbox from 'antd/lib/checkbox'
import 'antd/lib/checkbox/style/index.less'
import Form from 'antd/lib/form'
import 'antd/lib/form/style/'
import Input from 'antd/lib/input'
import 'antd/lib/input/style/'

import {
  handleNewLanguageDisplay,
  handleLoadSuggestion,
  handleSaveNewLanguage
} from '../../actions/languages-actions'

class NewLanguageModal extends Component {
  static propTypes = {
    show: PropTypes.bool,
    saving: PropTypes.bool,
    searchResults: PropTypes.array,
    handleOnClose: PropTypes.func,
    handleOnSave: PropTypes.func,
    loadSuggestion: PropTypes.func,
    form: PropTypes.any
  }

  constructor (props) {
    super(props)
    this.state = {
      details: {
        enabledByDefault: true,
        enabled: true,
        displayName: '',
        nativeName: '',
        pluralForms: ''
      },
      query: '',
      validFields: true,
      suggestions: props.searchResults
    }
  }

  handleCancel = () => {
    this.resetFields()
    this.props.handleOnClose()
  }

  resetFields = () => {
    this.props.form.resetFields()
    this.setState({
      details: {
        enabledByDefault: true,
        enabled: true,
        displayName: '',
        nativeName: '',
        pluralForms: ''
      },
      validFields: true,
      suggestions: [],
      query: ''
    })
  }

  updateField = (field, e) => {
    const value = e.target.value
    this.props.form.setFieldsValue({
      [field]: value
    })
    this.setState(prevState => ({
      details: {
        ...prevState.details,
        [field]: value
      }
    }))
  }

  updateCheckbox = (field) => {
    this.setState(prevState => ({
      details: {
        ...prevState.details,
        [field]: !prevState.details[field]
      }
    }))
  }

  validateDetails = () => {
    const displayName = this.state.details.displayName
    const query = this.state.query
    if (isEmpty(displayName) && isEmpty(query)) {
      this.setState({
        validFields: false
      })
    } else {
      const details = {
        ...this.state.details,
        localeId: query.replace('_', '-')
      }
      this.props.handleOnSave(details)
      this.resetFields()
    }
  }

  onSearchChange = (event, { newValue }) => {
    this.setState({
      query: newValue
    })
  }

  onSuggestionsClearRequested = () => {
  }

  getSuggestionValue = (selectedLocale) => {
    return selectedLocale.localeId
  }

  renderSuggestion = (suggestion) => {
    return (
      <span name='new-language-displayName'>
        <span className='u-textLight'>
          {suggestion.displayName}
        </span> <span className='u-textSuggestion'>
          {suggestion.localeId}
        </span>
      </span>
    )
  }

  onSuggestionSelected = (event, {
     suggestion, suggestionValue, sectionIndex, method }) => {
    // eslint-disable-next-line
    const { enabled, enabledByDefault, rtl, localeId, ...fieldValues } =
      suggestion
    this.props.form.setFieldsValue({ ...fieldValues })
    this.setState({
      details: {
        ...suggestion,
        enabledByDefault: true,
        enabled: true
      }
    })
  }

  /* eslint-disable react/jsx-no-bind, react/jsx-boolean-value */
  render () {
    const {show, saving, loadSuggestion, searchResults} = this.props
    const { details, query } = this.state
    const { getFieldDecorator } = this.props.form
    const inputProps = {
      placeholder: 'Search for languages',
      maxLength: 256,
      onChange: this.onSearchChange,
      value: query
    }

    const showPluralFormsWarning = isEmpty(searchResults) && !isEmpty(query)

    return (
      <Modal
        width={'48rem'}
        title={'Add a new language'}
        id='newLang'
        visible={show}
        onCancel={this.handleCancel}
        footer={[
          <Button
            key='back'
            aria-label='button'
            id='btn-new-language-cancel'
            disabled={saving}
            onClick={this.handleCancel}>
            Close
          </Button>,
          <Button
            key='ok'
            aria-label='button'
            loading={saving}
            disabled={(isEmpty(query) || isEmpty(details.nativeName) ||
              isEmpty(details.displayName || isEmpty(details.pluralForms)))}
            id='btn-new-language-save'
            type='primary'
            onClick={this.validateDetails}>
            Save
          </Button>
        ]}>
        <Form layout='vertical'>
          <Form.Item label={'Language Code'} title={'Language Code'}>
            <Autosuggest
              name='new-language-code'
              suggestions={searchResults}
              onSuggestionSelected={this.onSuggestionSelected}
              getSuggestionValue={this.getSuggestionValue}
              onSuggestionsFetchRequested={loadSuggestion}
              onSuggestionsClearRequested={this.onSuggestionsClearRequested}
              renderSuggestion={this.renderSuggestion}
              inputProps={inputProps}
            />
          </Form.Item>
          <Form.Item label={'Name'} title={'Name'}>
            {getFieldDecorator('displayName', {
              setFieldsInitialValue: details.displayName,
              rules: [{required: true,
                message: 'Please input a Language Display Name'}]
            })(
              <Input
                // id='displayName' set by getFieldDecorator
                maxLength={100}
                className='textInput'
                onChange={(e) => this.updateField('displayName', e)}
                placeholder='Display name' />
            )}
          </Form.Item>
          <Form.Item label={'Native Name'} title={'Native Name'}>
            {getFieldDecorator('nativeName', {
              setFieldsInitialValue: details.nativeName,
              rules: [{required: true,
                message: 'Please input a Language Native Name'}]
            })(
              <Input
                // id='nativeName' set by getFieldDecorator
                maxLength={100}
                className='textInput'
                onChange={(e) => this.updateField('nativeName', e)}
                placeholder='Native name' />
            )}
          </Form.Item>
          <Form.Item
            title={'Plural Forms'}
            label={
              <span>Plural forms {' '}
                <a href='http://docs.translatehouse.org/projects/localization-guide/en/latest/l10n/pluralforms.html?id=l10n/pluralforms' // eslint-disable-line max-len
                  target='_blank'>
                  <Icon name='info' className='s0' parentClassName='iconInfo'
                    title='Help' />
                </a>
                {showPluralFormsWarning &&
                  <div className='u-textSmall'
                    id='new-language-pluralforms-warning'>
                    No plural information available. Assuming no plurals.
                  </div>
                }</span>
              }>
              {getFieldDecorator('pluralForms', {
                setFieldsInitialValue: details.pluralForms,
                rules: [{required: true,
                  message: 'Please input the languages Plural Forms'}]
              })(
                <Input
                  maxLength={255}
                  className='textInput'
                  onChange={(e) => this.updateField('pluralForms', e)}
                  placeholder='Plural forms' />
              )}
          </Form.Item>
          <Form.Item>
            <Checkbox
              id='chk-new-language-enabled'
              onChange={() => this.updateCheckbox('enabledByDefault')}
              checked={details.enabledByDefault}>
              Enabled by default
            </Checkbox>
            <Checkbox
              onChange={() => this.updateCheckbox('enabled')}
              checked={details.enabled}>
              Enabled language
            </Checkbox>
          </Form.Item>
        </Form>
      </Modal>
    )
    /* eslint-enable react/jsx-no-bind, react/jsx-boolean-value */
  }
}

const mapStateToProps = (state) => {
  const {
    show,
    saving,
    searchResults
  } = state.languages.newLanguage
  return {
    show,
    saving,
    searchResults
  }
}

const mapDispatchToProps = (dispatch) => {
  const updateSuggestion = debounce((val) =>
    dispatch(handleLoadSuggestion(val)), 300)

  return {
    handleOnClose: () => {
      dispatch(handleNewLanguageDisplay(false))
    },
    handleOnSave: (details) => {
      dispatch(handleSaveNewLanguage(details))
    },
    loadSuggestion: (query) => {
      updateSuggestion(query.value)
    }
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(
  Form.create()(NewLanguageModal))
