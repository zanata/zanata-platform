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

  _closeModal: function() {
    Actions.clearMessage();
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
    var detailsSection;

    if(!StringUtils.isEmptyOrNull(this.props.value.DETAILS)) {
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
          {detailsSection}
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
