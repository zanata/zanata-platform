import React, {PropTypes, Component} from 'react'
import { connect } from 'react-redux'
import { cloneDeep, isEmpty, debounce } from 'lodash'
import {
  Modal
} from 'zanata-ui'

import { Autosuggest } from 'react-autosuggest'

import {
  FormGroup,
  FormControl,
  ControlLabel,
  Button,
  Checkbox
} from 'react-bootstrap'

import {
  handleNewLanguageDisplay,
  handleLoadSuggestion,
  handleSaveNewLanguage
} from '../../actions/languages'

const getSuggestions = value => {
  const inputValue = value.trim().toLowerCase()
  const query = value
  const inputLength = inputValue.length

  return inputLength === 0 ? [] : query.filter(lang =>
      lang.name.toLowerCase().slice(0, inputLength) === inputValue
  )
}

const {
    show,
    details,
    saving,
    validFields,
    suggestions,
    getSuggestionValue,
    renderSuggestion,
    inputProps
} = this.props

class NewLanguageModal extends Component {
  constructor (props) {
    super(props)
    this.state = {
      details: cloneDeep(props.details),
      query: '',
      validFields: true,
      value: '',
      suggestions: []
    }
  }

  handleCancel () {
    this.resetFields()
    this.props.handleOnClose()
  }

  resetFields () {
    this.setState({
      details: cloneDeep(this.props.details),
      validFields: true,
      query: ''
    })
  }

  updateField (field, e) {
    this.setState({
      details: {
        ...this.state.details,
        [field]: e.target.value
      }
    })
  }

  updateCheckbox (field) {
    this.setState({
      details: {
        ...this.state.details,
        [field]: !this.state.details[field]
      }
    })
  }

  validateDetails () {
    const displayName = this.state.details
    if (!isEmpty(displayName)) {
      this.props.handleOnSave(this.state.details)
    } else {
      this.setState({
        validFields: false
      })
    }
  }

  onChange (event, {newValue}) {
    this.setState({
      value: newValue
    })
  }

  onSuggestionsFetchRequested ({value}) {
    this.setState({
      suggestions: getSuggestions(value)
    })
  }

  onSuggestionsClearRequested () {
    this.setState({
      suggestions: []
    })
  }

  /* eslint-disable react/jsx-no-bind, react/jsx-boolean-value */
  render () {
    // TODO: search results from autocomplete
    return (
      <Modal
        show={show}
        onHide={() => this.handleCancel()} rootClose>
        <Modal.Header closeButton>
          <Modal.Title>Add a new language</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <div className='bootstrap'>
            <FormGroup validationState={!validFields ? 'error' : ''}>
              <ControlLabel>Language</ControlLabel>
              <Autosuggest
                suggestions={suggestions}
                onSuggestionsFetchRequested={this.onSuggestionsFetchRequested}
                onSuggestionsClearRequested={this.onSuggestionsClearRequested}
                getSuggestionValue={getSuggestionValue}
                renderSuggestion={renderSuggestion}
                inputProps={inputProps}
                />
            </FormGroup>
            <FormGroup validationState={!validFields ? 'error' : ''}>
              <ControlLabel>Name</ControlLabel>
              <FormControl type='text'
                onChange={(e) => this.updateField('displayName', e)}
                placeholder='Default display name'
                value={details.displayName} />
              <FormControl.Feedback />
            </FormGroup>
            <FormGroup>
              <ControlLabel>Native Name</ControlLabel>
              <FormControl type='text'
                onChange={(e) => this.updateField('nativeName', e)}
                placeholder='Default native name'
                value={details.nativeName} />
            </FormGroup>
            <FormGroup>
              <strong className='Mend(eq)'>Language Code</strong>
              <span className={details.localeId ? '' : 'C(muted)'}>
                {details.localeId || 'None'}
              </span>
            </FormGroup>
            <FormGroup>
              <ControlLabel>Alias</ControlLabel>
              <FormControl type='text'
                onChange={(e) => this.updateField('alias', e)}
                placeholder='eg. en-US'
                value={details.alias} />
            </FormGroup>
            <FormGroup>
              <ControlLabel>Plural forms</ControlLabel>
              <FormControl
                type='text'
                onChange={(e) => this.updateField('pluralForms', e)}
                placeholder='Default plural forms (if empty):
                nplurals=2;plural=(n>1)'
                value={details.pluralForms} />
            </FormGroup>
            <FormGroup>
              <Checkbox
                onChange={() => this.updateCheckbox('enabledByDefault')}
                checked={details.enabledByDefault}>
                Enabled by default
              </Checkbox>
              <Checkbox
                onChange={() => this.updateCheckbox('enabled')}
                checked={details.enabled}>
                Enabled language
              </Checkbox>
            </FormGroup>
          </div>
        </Modal.Body>
        <Modal.Footer>
          <div className='bootstrap Ta(end)'>
            <Button className='btn-left'
              disabled={saving}
              onClick={() => this.handleCancel()}>
              Close
            </Button>
            <Button
              disabled={saving} bsStyle='primary'
              onClick={() => this.validateDetails()}>
              Save
            </Button>
          </div>
        </Modal.Footer>
      </Modal>
    )
    /* eslint-enable react/jsx-no-bind, react/jsx-boolean-value */
  }
}

NewLanguageModal.propTypes = {
  show: PropTypes.bool,
  saving: PropTypes.bool,
  details: PropTypes.object,
  searchResults: PropTypes.array,
  handleOnClose: PropTypes.func,
  handleOnSave: PropTypes.func,
  loadSuggestion: PropTypes.func
}

const mapStateToProps = (state) => {
  const {
    show,
    saving,
    details,
    searchResults
  } = state.languages.newLanguage
  return {
    show,
    saving,
    details,
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
      updateSuggestion(query || '')
    }
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(NewLanguageModal)
