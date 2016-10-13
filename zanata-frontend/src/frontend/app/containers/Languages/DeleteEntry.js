import React, { Component, PropTypes } from 'react'
import ReactDOM from 'react-dom'
import {Button} from 'react-bootstrap'
import {
  ButtonLink,
  ButtonRound,
  Tooltip,
  Overlay
} from 'zanata-ui'

class DeleteEntry extends Component {

  handleDeleteEntry (localeId) {
    this.props.handleDeleteEntry(localeId)
    setTimeout(() => {
      this.props.handleDeleteEntryDisplay(false)
    }, 200)
  }

  render () {
    const {
      locale,
      show,
      handleDeleteEntryDisplay,
      handleDeleteEntry
    } = this.props
    /* eslint-disable react/jsx-no-bind */
    return (
      <div className='D(ib)'>
        <Overlay
          placement='top'
          target={() => ReactDOM.findDOMNode(this)}
          rootClose
          show={show}
          onHide={() => handleDeleteEntryDisplay(false)}>
          <Tooltip id='delete-glossary' title='Delete language'>
            <p>
              Are you sure you want to delete
              <span className='Fw(b)'> {locale.displayName}</span>?
            </p>
            <div className='Mt(rq)'>
              <ButtonLink
                atomic={{m: 'Mend(rh)'}}
                onClick={() => handleDeleteEntryDisplay(false)}>
                Cancel
              </ButtonLink>
              <ButtonRound type='danger' size='n1'
                onClick={() => {
                  handleDeleteEntry(locale.localeId)
                  handleDeleteEntryDisplay(false)
                }}>
                Delete
              </ButtonRound>
            </div>
          </Tooltip>
        </Overlay>

        <Button bsSize='small'
          onClick={() => handleDeleteEntryDisplay(true)}>
          <i className='fa fa-times Mend(ee)'></i>Delete
        </Button>
      </div>
    )
    /* eslint-enable react/jsx-no-bind */
  }
}

DeleteEntry.propTypes = {
  locale: React.PropTypes.object,
  show: React.PropTypes.bool,
  handleDeleteEntryDisplay: PropTypes.func.isRequired,
  handleDeleteEntry: React.PropTypes.func.isRequired
}

export default DeleteEntry
