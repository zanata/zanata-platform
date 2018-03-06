import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import {
  Button,
  Popover,
  Overlay
} from 'react-bootstrap'
import * as ReactDOM from 'react-dom'
import { Icon } from '../../components'

class DeleteEntry extends Component {
  static propTypes = {
    locale: PropTypes.object,
    show: PropTypes.bool,
    handleDeleteEntryDisplay: PropTypes.func.isRequired,
    handleDeleteEntry: PropTypes.func.isRequired
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
      <div className='u-blockInline bstrapReact'>
        <Button bsSize='small'
          onClick={() => handleDeleteEntryDisplay(true)}>
          <Icon name='cross' className='n2' parentClassName='iconCross'
            title='cross' />
          Delete
        </Button>
        <Overlay show={show} placement='top'
          target={() => ReactDOM.findDOMNode(this)}>
          <Popover id='popover-contained' title='Delete language'>
            <p>Are you sure you want to delete&nbsp;
              <strong>{locale.displayName}</strong>?&nbsp;
            </p>
            <span className='button-spacing'>
              <Button bsStyle='default' bsSize='small'
                onClick={() => handleDeleteEntryDisplay(false)}>
                Cancel
              </Button>
              <Button bsStyle='danger' bsSize='small' type='button'
                onClick={() => {
                  handleDeleteEntry(locale.localeId)
                  handleDeleteEntryDisplay(false)
                }}>
                Delete
              </Button>
            </span>
          </Popover>
        </Overlay>
      </div>
    )
    /* eslint-enable react/jsx-no-bind */
  }
}

export default DeleteEntry
