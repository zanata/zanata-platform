import React, {PropTypes, Component} from 'react'
import { connect } from 'react-redux'
import { isEmpty, debounce } from 'lodash'
import { Modal, Icon } from '../../components'
import Autosuggest from 'react-autosuggest'

import {
  FormGroup,
  FormControl,
  ControlLabel,
  Button,
  Checkbox,
  Row
} from 'react-bootstrap'

import {
  handleNewLanguageDisplay,
  handleLoadSuggestion,
  handleSaveNewLanguage
} from '../../actions/languages'

class NewLanguageModal extends Component {
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

  handleCancel () {
    this.resetFields()
    this.props.handleOnClose()
  }

  resetFields () {
    this.setState({
      details: {
        enabledByDefault: true,
        enabled: true
      },
      validFields: true,
      suggestions: [],
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

  getSuggestionValue (selectedLocale) {
    return selectedLocale.localeId
  }

  renderSuggestion (suggestion) {
    return (
      <span name='new-language-displayName'>
        <span className='Fw(400)'>
          {suggestion.displayName}
        </span>
        <span className='C(muted) Fz(msn1) Mstart(eq)'>
          {suggestion.localeId}
        </span>
      </span>
    )
  }

  onSuggestionSelected = (event,
    { suggestion, suggestionValue, sectionIndex, method }) => {
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
    const { details, query, validFields } = this.state

    const inputProps = {
      placeholder: 'Search for languages',
      maxLength: 256,
      onChange: this.onSearchChange,
      value: query
    }

    const showPluralFormsWarning = isEmpty(searchResults) && !isEmpty(query)

    return (
      <Modal
        show={show}
        onHide={() => this.handleCancel()}>
        <Modal.Header>
          <Modal.Title>Add a new language</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <div className='bootstrap'>
            <FormGroup validationState={!validFields ? 'error' : undefined}>
              <ControlLabel>Language Code</ControlLabel>
              <span className='form-control'>
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
              </span>
            </FormGroup>
            <FormGroup validationState={!validFields ? 'error' : undefined}>
              <ControlLabel>Name</ControlLabel>
              <FormControl type='text'
                maxLength={100}
                id='new-language-name'
                onChange={(e) => this.updateField('displayName', e)}
                placeholder='Display name'
                value={details.displayName} />
              <FormControl.Feedback />
            </FormGroup>
            <FormGroup>
              <ControlLabel>Native Name</ControlLabel>
              <FormControl type='text'
                id='new-language-nativeName'
                maxLength={100}
                onChange={(e) => this.updateField('nativeName', e)}
                placeholder='Native name'
                value={details.nativeName} />
            </FormGroup>
            <FormGroup validationState={showPluralFormsWarning
              ? 'warning' : undefined}>
              <ControlLabel>
                Plural forms
                <a href='http://docs.translatehouse.org/projects/localization-guide/en/latest/l10n/pluralforms.html?id=l10n/pluralforms' // eslint-disable-line max-len
                  target='_blank'>
                  <Icon name='info' className='s0 infoicon' title='Help' />
                </a>
                {showPluralFormsWarning &&
                  <div className='Fz(msn1)'
                    id='new-language-pluralforms-warning'>
                    No plural information available. Assuming no plurals.
                  </div>
                }
              </ControlLabel>
              <FormControl
                type='text'
                maxLength={255}
                onChange={(e) => this.updateField('pluralForms', e)}
                placeholder='Plural forms'
                value={details.pluralForms} />
            </FormGroup>
            <FormGroup>
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
            </FormGroup>
          </div>
        </Modal.Body>
        <Modal.Footer>
          <span className='bootstrap pull-right'>
            <Row>
              <Button bsStyle='link'
                id='btn-new-language-cancel' className='btn-left'
                disabled={saving}
                onClick={() => this.handleCancel()}>
                Close
              </Button>
              <Button
                disabled={saving ||
                  (isEmpty(details.localeId) && isEmpty(query))}
                id='btn-new-language-save'
                bsStyle='primary'
                onClick={() => this.validateDetails()}>
                Save
              </Button>
            </Row>
          </span>
        </Modal.Footer>
      </Modal>
    )
    /* eslint-enable react/jsx-no-bind, react/jsx-boolean-value */
  }
}

NewLanguageModal.propTypes = {
  show: PropTypes.bool,
  saving: PropTypes.bool,
  searchResults: PropTypes.array,
  handleOnClose: PropTypes.func,
  handleOnSave: PropTypes.func,
  loadSuggestion: PropTypes.func
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

export default connect(mapStateToProps, mapDispatchToProps)(NewLanguageModal)
