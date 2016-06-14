import React, {PureRenderMixin} from 'react/addons';
import {Icon, Button, Input, Modal, Select} from 'zanata-ui';
import Actions from '../../actions/GlossaryActions';
import StringUtils from '../../utils/StringUtils'
import _ from 'lodash';
import GlossaryStore from '../../stores/GlossaryStore';

var ImportModal = React.createClass({
  propTypes: {
    className: React.PropTypes.string,
    transLocales: React.PropTypes.object,
    srcLocale: React.PropTypes.shape({
      locale: React.PropTypes.shape({
        localeId: React.PropTypes.string.isRequired,
        displayName: React.PropTypes.string.isRequired,
        alias: React.PropTypes.string.isRequired
      }).isRequired,
      numberOfTerms: React.PropTypes.number.isRequired
    })
  },

  mixins: [PureRenderMixin],

  getInitialState: function() {
    return this._getState();
  },

  _getState: function() {
    return GlossaryStore.getUploadFileState();
  },

  componentDidMount: function() {
    GlossaryStore.addChangeListener(this._onChange);
  },

  componentWillUnmount: function() {
    GlossaryStore.removeChangeListener(this._onChange);
  },

  _onChange: function() {
    this.setState(this._getState());
  },

  _showModal: function () {
    this.setState({show: true})
  },

  _closeModal: function () {
    this.setState(this.getInitialState());
  },

  _onFileChange: function(e) {
    var file = e.target.files[0];
    this.setState({file: file});
  },

  _uploadFile: function() {
    this.setState({status: 0});
    Actions.uploadFile(this.state, this.props.srcLocale.locale.localeId);
  },

  _getUploadFileExtension: function () {
    var extension = '';
    if(this.state.file) {
      extension = this.state.file.name.split(".").pop();
    }
    return extension;
  },

  _isSupportedFile: function (extension) {
    return extension === 'po' || extension === 'csv';
  },

  _onTransLocaleChange: function (localeId) {
    this.setState({transLocale : localeId});
  },

  render: function () {
    var transLanguageDropdown,
      messageSection,
      fileExtension = this._getUploadFileExtension(),
      disableUpload = true,
      isUploading = this.state.status !== -1;

    if(this._isSupportedFile(fileExtension)) {
      if(fileExtension === 'po') {
        var localeOptions = [];
        _.forEach(this.props.transLocales, function(locale, localeId) {
          localeOptions.push({
            value: localeId,
            label: locale.locale.displayName
          });
        });

        if(isUploading) {
          transLanguageDropdown = (<span className='csec fz2'>{this.state.transLocale.label}</span>);
        } else {
          transLanguageDropdown = (<Select
            name='glossary-import-language-selection'
            className='w16 mb1'
            placeholder='Select a translation language…'
            value={this.state.transLocale}
            options={localeOptions}
            onChange={this._onTransLocaleChange}
          />);
        }

        if(!StringUtils.isEmptyOrNull(this.state.transLocale)) {
          disableUpload = false;
        }
      } else {
        disableUpload = false;
      }
    } else if(this.state.file) {
      messageSection = <div className='cdanger mv1/4'>File {this.state.file.name} is not supported.</div>
    }

    return (
      <div className={this.props.className}>
        <Button className='whsnw tove ovh' onClick={this._showModal} link>
          <Icon name='import' className='mr1/4' /><span>Import Glossary</span>
        </Button>
        <Modal show={this.state.show} onHide={this._closeModal}>
          <Modal.Header>
            <Modal.Title>Import Glossary</Modal.Title>
          </Modal.Header>
          <Modal.Body className='tal' scrollable={false}>
            <input type="file" onChange={this._onFileChange} ref="file" multiple={false} disabled={isUploading} className="mb1" />
            {messageSection}
            {transLanguageDropdown}
            <p>
              CSV and PO files are supported. <strong>The source language should be in {this.props.srcLocale.locale.displayName}</strong>.
              For more details on how to prepare glossary files, see our <a
              href="http://docs.zanata.org/en/release/user-guide/glossary/upload-glossaries/"
              className="cpri" target="_blank">glossary import documentation</a>.
            </p>
          </Modal.Body>
          <Modal.Footer>
            <Button className='mr1' disabled={isUploading} link onClick={this._closeModal}>Cancel</Button>
            <Button kind='primary' disabled={disableUpload} onClick={this._uploadFile} loading={isUploading}>Import</Button>
          </Modal.Footer>
        </Modal>
      </div>)
  }
});

export default ImportModal;
