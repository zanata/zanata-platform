import React, { PureRenderMixin } from 'react/addons';
import Configs from '../constants/Configs';
import GlossaryStore from '../stores/GlossaryStore';
import Actions from '../actions/GlossaryActions';
import { Button, Input, Icons, Icon, Loader, Select } from 'zanata-ui'
import DataTable from './glossary/DataTable'
import NewEntryModal from './glossary/NewEntryModal'
import ImportModal from './glossary/ImportModal'
import MessageModal from './MessageModal'
import _ from 'lodash';
import StringUtils from '../utils/StringUtils'

/**
 * Main view for glossary page
 */
var SystemGlossary = React.createClass({
  mixins: [PureRenderMixin],

  filterTimeout: null,

  _init: function() {
    return GlossaryStore.init();
  },

  getInitialState: function() {
    return this._init();
  },

  componentDidMount: function() {
    GlossaryStore.addChangeListener(this._onChange);
  },

  componentWillUnmount: function() {
    if(this.filterTimeout !== null) {
      clearTimeout(this.filterTimeout);
    }
    GlossaryStore.removeChangeListener(this._onChange);
  },

  _onChange: function() {
    this.setState(this._init());
  },

  _onTranslationLocaleChange: function(localeId) {
    this.setState({loadingEntries: true});
    Actions.changeTransLocale(localeId)
  },

  _handleFilterKeyDown: function(event) {
    if(event.key === 'Enter') {
      Actions.updateFilter(this.state.filter);
    }
  },

  _handleFilterReset: function (event) {
    this.setState({filter: ''})
    Actions.updateFilter('')
  },

  _handleFilterChange: function(event) {
    this.setState({filter: event.target.value});

    if(this.filterTimeout) {
      clearTimeout(this.filterTimeout);
    }
    this.filterTimeout = setTimeout(() => {
      Actions.updateFilter(this.state.filter)
    }, 500);
  },

  render: function() {
    const srcLocale = this.state.srcLocale;
    var count = 0,
      selectedTransLocale = this.state.selectedTransLocale,
      uploadSection,
      newEntrySection,
      messageModal;

    if(this.state.notification) {
      messageModal = <MessageModal value={this.state.notification}/>;
    }

    if(!_.isUndefined(srcLocale) && !_.isNull(srcLocale)) {
      count = this.state.srcLocale.numberOfTerms;
    }

    var allowNewEntry = this.state.canAddNewEntry && !_.isUndefined(srcLocale) && !_.isNull(srcLocale);

    if(allowNewEntry) {
      uploadSection = (<ImportModal srcLocale={this.state.srcLocale} transLocales={this.state.locales}/>);
      newEntrySection = (<NewEntryModal className='ml1/2' srcLocale={this.state.srcLocale}/>);
    }
    var loader;

    if(this.state.loadingEntries) {
      loader = (<Loader className='csec ml1/2' size={3}/>);
    }

    return (
      <div>
        <Icons />
        <div className='dfx aic mb2'>
          <div className='fxauto dfx aic'>
            <h1 className='fz2 dib csec whsnw tove ovh'>System Glossary</h1>
            <Icon name='chevron-right' className='mh1/2 csec50' size='s1'/>
            <Select
              name='language-selection'
              placeholder='Select a language…'
              className='wmi8'
              value={this.state.selectedTransLocale}
              options={this.state.localeOptions}
              onChange={this._onTranslationLocaleChange}/>
            {loader}
          </div>
          <div className='dfx aic'>
            {messageModal}
            {uploadSection}
            {newEntrySection}
          </div>
        </div>
        <div className='dfx aic mb1/2'>
          <div className='fxauto'>
            <div className='maw16'>
              <Input value={this.state.filter}
                label='Search Glossary'
                hideLabel
                icon='search'
                border='outline'
                resetButton
                loading={this.state.loadingEntries && this.state.filter}
                onReset={this._handleFilterReset}
                placeholder='Search Glossary'
                id="search"
                onKeyDown={this._handleFilterKeyDown}
                onChange={this._handleFilterChange}/>
            </div>
          </div>
          <div className='dfx aic'>
            <Icon name='glossary' className='csec50 mr1/4' />
            <span className='csec'>{count}</span>
          </div>
        </div>
        <div>
          <DataTable
            glossaryData={this.state.glossary}
            glossaryIds={this.state.glossaryIds}
            focusedRow={this.state.focusedRow}
            hoveredRow={this.state.hoveredRow}
            totalCount={this.state.totalCount}
            canAddNewEntry={this.state.canAddNewEntry}
            canUpdateEntry={this.state.canUpdateEntry}
            user={Configs.user}
            srcLocale={srcLocale}
            locales={this.state.locales}
            filter={this.state.filter}
            allowNewEntry={allowNewEntry}
            loading={this.state.loadingEntries}
            selectedTransLocale={selectedTransLocale}/>
        </div>
      </div>
    );
  }
});

export default SystemGlossary;
