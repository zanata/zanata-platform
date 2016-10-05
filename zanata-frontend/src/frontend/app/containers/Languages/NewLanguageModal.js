import React, {PropTypes, Component} from 'react'
import { connect } from 'react-redux'
import {
  Row,
  ButtonLink,
  ButtonRound,
  LoaderText,
  Modal
} from 'zanata-ui'

import {
  handleNewLanguageDisplay
} from '../../actions/languages'

class NewLanguageModal extends Component {

  resetFields () {

  }

  handleCancel () {
    this.resetFields()
    this.props.handleOnClose()
  }

  /* eslint-disable react/jsx-no-bind, react/jsx-boolean-value */
  render () {
    const {
      show,
      saving
    } = this.props

    return (
      <Modal
        show={show}
        onHide={() => this.handleCancel()} rootClose >
        <Modal.Header closeButton>
          <Modal.Title>New language</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          Elit est explicabo ipsum eaque dolorem blanditiis.
        </Modal.Body>
        <Modal.Footer>
          <Row theme={{ base: {j: 'Jc(c)'} }}>
            <ButtonLink
              atomic={{m: 'Mend(r1)'}}
              disabled={saving}
              onClick={() => this.handleCancel()}>
              Cancel
            </ButtonLink>
            <ButtonRound
              type='primary'
              disabled={saving}>
              <LoaderText loading={saving} loadingText='Saving'>
                Save
              </LoaderText>
            </ButtonRound>
          </Row>
        </Modal.Footer>
      </Modal>
    )
  }
  /* eslint-enable react/jsx-no-bind, react/jsx-boolean-value */
}

NewLanguageModal.propTypes = {
  show: PropTypes.bool,
  saving: PropTypes.bool,
  handleOnClose: PropTypes.func
}

const mapStateToProps = (state) => {
  const {
    show,
    saving
  } = state.languages.newLanguage
  return {
    show,
    saving
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    handleOnClose: () => {
      dispatch(handleNewLanguageDisplay(false))
    }
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(NewLanguageModal)
