import React, {PropTypes, Component} from 'react'
import { connect } from 'react-redux'
import { isEmpty, debounce } from 'lodash'
import { Modal, Icon } from 'zanata-ui'
import Autosuggest from 'react-autosuggest'

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
      const details = isEmpty(displayName) ? {
        ...this.state.details,
        localeId: query.replace('_', '-')
      } : this.state.details
      this.props.handleOnSave(details)
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
    return selectedLocale
  }

  renderSuggestion (suggestion) {
    return (
      <span>
        <span className='Fw(400)'>{suggestion.displayName}</span>
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
        ...suggestionValue,
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

    return (
      <Modal
        show={show}
        onHide={() => this.handleCancel()} rootClose>
        <Modal.Header closeButton>
          <Modal.Title>Add a new language</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <div className='bootstrap'>
            <FormGroup validationState={!validFields ? 'error' : undefined}>
              <ControlLabel>Language</ControlLabel>
              <Autosuggest
                suggestions={searchResults}
                onSuggestionSelected={this.onSuggestionSelected}
                getSuggestionValue={this.getSuggestionValue}
                onSuggestionsFetchRequested={loadSuggestion}
                onSuggestionsClearRequested={this.onSuggestionsClearRequested}
                renderSuggestion={this.renderSuggestion}
                inputProps={inputProps}
                />
            </FormGroup>
            <FormGroup validationState={!validFields ? 'error' : undefined}>
              <ControlLabel>Name</ControlLabel>
              <FormControl type='text'
                maxLength={100}
                onChange={(e) => this.updateField('displayName', e)}
                placeholder='Display name'
                value={details.displayName} />
              <FormControl.Feedback />
            </FormGroup>
            <FormGroup>
              <ControlLabel>Native Name</ControlLabel>
              <FormControl type='text'
                maxLength={100}
                onChange={(e) => this.updateField('nativeName', e)}
                placeholder='Native name'
                value={details.nativeName} />
            </FormGroup>
            <FormGroup>
              <strong className='Mend(eq)'>Language Code</strong>
              <span className='C(muted)'>
                {details.localeId || 'None'}
              </span>
            </FormGroup>
            <FormGroup>
              <ControlLabel>Plural forms</ControlLabel>
              <a href='http://docs.translatehouse.org/projects/localization-guide/en/latest/l10n/pluralforms.html?id=l10n/pluralforms' // eslint-disable-line max-len
                target='_blank'>
                <Icon name='info'
                  atomic={{m: 'Mstart(re) Va(sub)'}}
                  title='Help' />
              </a>
              <FormControl
                type='text'
                maxLength={255}
                onChange={(e) => this.updateField('pluralForms', e)}
                placeholder='Plural forms'
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
              disabled={saving ||
                (isEmpty(details.localeId) && isEmpty(query))}
              bsStyle='primary'
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
