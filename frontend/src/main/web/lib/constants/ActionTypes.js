import keymirror from 'keymirror';

var UserMatrixActionTypes = keymirror({
  DATE_RANGE_UPDATE: null,
  CONTENT_STATE_UPDATE: null,
  DAY_SELECTED: null
});

var GlossaryActionTypes = keymirror({
  TRANS_LOCALE_SELECTED: null,
  INSERT_GLOSSARY: null,
  UPDATE_GLOSSARY: null,
  DELETE_GLOSSARY: null,
  UPDATE_FILTER: null,
  LOAD_GLOSSARY: null,
  UPDATE_SORT_ORDER: null,
  UPLOAD_FILE: null,
  UPDATE_ENTRY_FIELD: null,
  UPDATE_COMMENT: null,
  UPDATE_FOCUSED_ROW: null,
  RESET_ENTRY: null,
  CLEAR_MESSAGE: null
});

export { UserMatrixActionTypes, GlossaryActionTypes }
