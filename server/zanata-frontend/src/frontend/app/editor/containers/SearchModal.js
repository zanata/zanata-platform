import React, { PropTypes } from 'react'
import { Modal } from '../../components'
import { Button, ButtonGroup } from 'react-bootstrap'
import { connect } from 'react-redux'
import { toggleAdvanceSearchModal } from '../actions/search'

const SearchModal = React.createClass({
  propTypes: {
    showPanel: PropTypes.bool,
    toggleAdvanceSearchModal: PropTypes.func
  },

  render: function () {
    console.log('this.props.showPanel', this.props.showPanel)
    return (
      <Modal show={this.props.showPanel}
        onHide={this.props.toggleAdvanceSearchModal}>
        <Modal.Header>
          <Modal.Title>
            Search title
          </Modal.Title>
        </Modal.Header>
        <Modal.Body>
          body here
        </Modal.Body>
        <Modal.Footer>
          <ButtonGroup className='pull-right'>
            <Button bsStyle='link'>
              Cancel
            </Button>
            <Button bsStyle='primary' type='button'>
              Search
            </Button>
          </ButtonGroup>
        </Modal.Footer>
      </Modal>
    )
  }
})

function mapStateToProps (state) {
  return {
    ...state.search,
    showPanel: state.ui.panels.search.visible
  }
}

function mapDispatchToProps (dispatch) {
  return {
    toggleAdvanceSearchModal: () => dispatch(toggleAdvanceSearchModal())
  }
}
export default connect(mapStateToProps, mapDispatchToProps)(SearchModal)
