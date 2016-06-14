import React from 'react'
import {Icon, Button, Modal} from 'zanata-ui';
import Actions from '../actions/GlossaryActions';
import StringUtils from '../utils/StringUtils'

var MessageModal = React.createClass({
  propTypes: {
    value: React.PropTypes.shape({
      SEVERITY: React.PropTypes.string.isRequired,
      SUBJECT: React.PropTypes.string.isRequired,
      MESSAGE: React.PropTypes.string,
      DETAILS: React.PropTypes.string
    }).isRequired
  },

  getInitialState: function() {
    return {
      showDetails: false
    };
  },

  _closeModal: function() {
    this.setState({showDetails: !this.state.showDetails})
    Actions.clearMessage();
  },

  _toggleDetails: function() {
    const showDetails = !this.state.showDetails
    this.setState({showDetails: showDetails})
  },

  _getSeverityClass: function (severity) {
    switch(severity) {
      case 'warn':
        return 'cwarning';
      case 'error':
        return 'cdanger';
      default:
        return 'chighlight';
    }
  },

  render: function () {
    const severityClass = this._getSeverityClass(this.props.value.SEVERITY);
    const hasDetails = !StringUtils.isEmptyOrNull(this.props.value.DETAILS);
    const detailsButtonText = this.state.showDetails ? 'Hide details' : 'Show details'
    var detailsSection;

    if(hasDetails) {
      detailsSection = (
        <div className='bdrs1/4 bxsh1 p1/2 fzn1'>
          {this.props.value.DETAILS}
        </div>
      );
    }

    return (
      <Modal show={true} onHide={this._closeModal}>
        <Modal.Header>
          <Modal.Title>Notification</Modal.Title>
        </Modal.Header>
        <Modal.Body className='tal'>
          <div className={severityClass + ' fz2'}>{this.props.value.SUBJECT}</div>
          <div className='mv1/2'>
            {this.props.value.MESSAGE}
          </div>
          {hasDetails &&
            <Button className='whsnw tove ovh' onClick={this._toggleDetails} link>
              {detailsButtonText}
            </Button>
          }
          {this.state.showDetails && detailsSection}
        </Modal.Body>
        <Modal.Footer>
          <Button
            kind='primary'
            onClick={this._closeModal}>
            Clear message
          </Button>
        </Modal.Footer>
      </Modal>
    );
  }
});

export default MessageModal;
