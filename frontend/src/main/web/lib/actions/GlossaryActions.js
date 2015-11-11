import Dispatcher from '../dispatchers/GlossaryDispatcher';
import {GlossaryActionTypes} from '../constants/ActionTypes';

var Actions = {
  NO_INFO_MESSAGE: 'No information available',

  changeTransLocale: function(selectedLocale) {
    Dispatcher.handleViewAction({
      actionType: GlossaryActionTypes.TRANS_LOCALE_SELECTED,
      data: selectedLocale
    });
  },
  saveGlossary: function(srcLocaleId, term, pos, description) {
    Dispatcher.handleViewAction({
      actionType: GlossaryActionTypes.INSERT_GLOSSARY,
      data: {
        srcLocaleId: srcLocaleId,
        term: term,
        pos: pos,
        description: description
      }
    });
  },
  updateGlossary: function(id) {
    Dispatcher.handleViewAction({
      actionType: GlossaryActionTypes.UPDATE_GLOSSARY,
      data: id
    });
  },
  deleteGlossary: function(id) {
    Dispatcher.handleViewAction({
      actionType: GlossaryActionTypes.DELETE_GLOSSARY,
      data: {
       id: id
      }
    });
  },
  updateFilter: function(filter) {
    Dispatcher.handleViewAction({
        actionType: GlossaryActionTypes.UPDATE_FILTER,
        data: filter
    });
  },
  updateSortOrder: function(field, ascending) {
    Dispatcher.handleViewAction({
      actionType: GlossaryActionTypes.UPDATE_SORT_ORDER,
      data: {
        field:field,
        ascending: ascending
      }
    });
  },
  uploadFile: function(uploadFile, srcLocale) {
    Dispatcher.handleViewAction({
      actionType: GlossaryActionTypes.UPLOAD_FILE,
      data: {
        uploadFile: uploadFile,
        srcLocale: srcLocale
      }
    });
  },
  loadGlossary: function(index) {
    Dispatcher.handleViewAction({
        actionType: GlossaryActionTypes.LOAD_GLOSSARY,
        data: index
    });
  },
  updateEntryField: function (id, field, value) {
    Dispatcher.handleViewAction({
          actionType: GlossaryActionTypes.UPDATE_ENTRY_FIELD,
          data: {
            id: id,
            field: field,
            value: value
          }
    });
  },
  updateComment: function (id, value) {
    Dispatcher.handleViewAction({
      actionType: GlossaryActionTypes.UPDATE_COMMENT,
      data: {
        id: id,
        value: value
      }
    });
  },
  updateFocusedRow: function (id, rowIndex) {
    Dispatcher.handleViewAction({
      actionType: GlossaryActionTypes.UPDATE_FOCUSED_ROW,
      data: {
        rowIndex: rowIndex,
        id: id
      }
    });
  },
  resetEntry: function(id) {
    Dispatcher.handleViewAction({
        actionType: GlossaryActionTypes.RESET_ENTRY,
        data: id
      });
  },
  clearMessage: function() {
    Dispatcher.handleViewAction({
      actionType: GlossaryActionTypes.CLEAR_MESSAGE
    });
  }
};

export default Actions;
