import { handleActions } from 'redux-actions'
import { isEmpty, cloneDeep, forEach, size } from 'lodash'
import {
  GLOSSARY_PERMISSION_REQUEST,
  GLOSSARY_PERMISSION_SUCCESS,
  GLOSSARY_PERMISSION_FAILURE,
  GLOSSARY_UPDATE_LOCALE,
  GLOSSARY_UPDATE_FILTER,
  GLOSSARY_INIT_STATE_FROM_URL,
  GLOSSARY_TERMS_INVALIDATE,
  GLOSSARY_TERMS_REQUEST,
  GLOSSARY_TERMS_SUCCESS,
  GLOSSARY_TERMS_FAILURE,
  GLOSSARY_STATS_REQUEST,
  GLOSSARY_STATS_SUCCESS,
  GLOSSARY_STATS_FAILURE,
  GLOSSARY_SELECT_TERM,
  GLOSSARY_UPDATE_FIELD,
  GLOSSARY_RESET_TERM,
  GLOSSARY_UPDATE_REQUEST,
  GLOSSARY_UPDATE_SUCCESS,
  GLOSSARY_UPDATE_FAILURE,
  GLOSSARY_DELETE_REQUEST,
  GLOSSARY_DELETE_SUCCESS,
  GLOSSARY_DELETE_FAILURE,
  GLOSSARY_UPLOAD_REQUEST,
  GLOSSARY_UPLOAD_SUCCESS,
  GLOSSARY_UPLOAD_FAILURE,
  GLOSSARY_UPDATE_IMPORT_FILE,
  GLOSSARY_UPDATE_IMPORT_FILE_LOCALE,
  GLOSSARY_TOGGLE_IMPORT_DISPLAY,
  GLOSSARY_TOGGLE_EXPORT_DISPLAY,
  GLOSSARY_UPDATE_EXPORT_TYPE,
  GLOSSARY_UPDATE_SORT,
  GLOSSARY_TOGGLE_NEW_ENTRY_DISPLAY,
  GLOSSARY_TOGGLE_DELETE_ALL_ENTRIES_DISPLAY,
  GLOSSARY_CREATE_REQUEST,
  GLOSSARY_CREATE_SUCCESS,
  GLOSSARY_CREATE_FAILURE,
  GLOSSARY_DELETE_ALL_REQUEST,
  GLOSSARY_DELETE_ALL_SUCCESS,
  GLOSSARY_DELETE_ALL_FAILURE,
  GLOSSARY_EXPORT_REQUEST,
  GLOSSARY_EXPORT_SUCCESS,
  GLOSSARY_EXPORT_FAILURE,
  GLOSSARY_GET_QUALIFIED_NAME_REQUEST,
  GLOSSARY_GET_QUALIFIED_NAME_SUCCESS,
  GLOSSARY_GET_QUALIFIED_NAME_FAILURE,
  PROJECT_GET_DETAILS_REQUEST,
  PROJECT_GET_DETAILS_SUCCESS,
  PROJECT_GET_DETAILS_FAILURE,
  FILE_TYPES
} from '../actions/glossary-actions'
import {
  CLEAR_MESSAGE,
  SEVERITY,
  DEFAULT_LOCALE
} from '../actions/common-actions'
import GlossaryHelper from '../utils/GlossaryHelper'

const ERROR_MSG = 'We are unable to get glossary information from server. ' +
  'Please refresh this page and try again.'

const PROJECT_ERROR_MSG = 'We are unable to get project information ' +
  'from server. Please refresh this page and try again.'

// @ts-ignore
const glossary = handleActions({
  [CLEAR_MESSAGE]: (state, _action) => {
    return {
      ...state,
      notification: undefined
    }
  },
  [GLOSSARY_INIT_STATE_FROM_URL]: (state, action) => {
    // @ts-ignore
    const query = action.payload.query
    // @ts-ignore
    const projectSlug = action.payload.projectSlug
    return {
      ...state,
      src: query.src || DEFAULT_LOCALE.localeId,
      locale: query.locale || '',
      filter: query.filter || '',
      sort: GlossaryHelper.convertSortToObject(query.sort),
      projectSlug: projectSlug
    }
  },
  [GLOSSARY_PERMISSION_REQUEST]: (state, _action) => ({
    ...state,
    permission: {
      canAddNewEntry: false,
      canUpdateEntry: false,
      canDeleteEntry: false,
      canDownload: false
    }
  }),
  [GLOSSARY_PERMISSION_SUCCESS]: (state, action) => {
    if (action.error) {
      return {
        ...state,
        notification: {
          severity: SEVERITY.ERROR,
          message: ERROR_MSG
        }
      }
    } else {
      return {
        ...state,
        permission: {
          // @ts-ignore payload
          canAddNewEntry: action.payload.insertGlossary,
          // @ts-ignore payload
          canUpdateEntry: action.payload.updateGlossary,
          // @ts-ignore payload
          canDeleteEntry: action.payload.deleteGlossary,
          // @ts-ignore payload
          canDownload: action.payload.downloadGlossary
        }
      }
    }
  },
  [PROJECT_GET_DETAILS_REQUEST]: (state, _action) => ({
    ...state,
    project: undefined
  }),
  [PROJECT_GET_DETAILS_SUCCESS]: (state, action) => {
    if (action.error) {
      return {
        ...state,
        project: undefined,
        termsLoading: false,
        notification: {
          severity: SEVERITY.ERROR,
          message: PROJECT_ERROR_MSG
        }
      }
    } else {
      return {
        ...state,
        project: action.payload
      }
    }
  },
  [PROJECT_GET_DETAILS_FAILURE]: (state, _action) => ({
    ...state,
    project: undefined,
    termsLoading: false,
    notification: {
      severity: SEVERITY.ERROR,
      message: PROJECT_ERROR_MSG
    }
  }),
  [GLOSSARY_PERMISSION_FAILURE]: (state, _action) => ({
    ...state,
    permission: {
      canAddNewEntry: false,
      canUpdateEntry: false,
      canDeleteEntry: false,
      canDownload: false
    },
    notification: {
      severity: SEVERITY.ERROR,
      message: ERROR_MSG
    }
  }),
  [GLOSSARY_UPDATE_LOCALE]: (state, action) => ({
    ...state,
    selectedTerm: {},
    locale: action.payload
  }),
  [GLOSSARY_UPDATE_FILTER]: (state, action) => ({
    ...state,
    filter: action.payload
  }),
  [GLOSSARY_UPDATE_SORT]: (state, action) => {
    return {
      ...state,
      sort: action.payload
    }
  },
  [GLOSSARY_UPLOAD_REQUEST]: (state, action) => {
    if (action.error) {
      return {
        ...state,
        notification: {
          severity: SEVERITY.ERROR,
          message:
            'We were unable to import your file. ' +
            'Please refresh this page and try again.'
        }
      }
    } else {
      let importFile = state.importFile
      // @ts-ignore FIXME this looks like a state bug. immutability-helper?
      importFile.status = 0
      return {
        ...state,
        importFile: importFile
      }
    }
  },
  [GLOSSARY_UPLOAD_SUCCESS]: (state, action) => ({
    ...state,
    importFile: {
      show: false,
      status: -1,
      file: null,
      transLocale: null
    },
    notification: {
      severity: SEVERITY.INFO,
      message: 'File imported successfully',
      // @ts-ignore payload
      description: size(action.payload.glossaryEntries) + ' terms imported.'
    }
  }),
  [GLOSSARY_UPLOAD_FAILURE]: (state, _action) => ({
    ...state,
    importFile: {
      show: false,
      status: -1,
      file: null,
      transLocale: null
    },
    notification: {
      severity: SEVERITY.ERROR,
      message:
        'We were unable to import your file. ' +
        'Please refresh this page and try again.'
    }
  }),
  [GLOSSARY_UPDATE_IMPORT_FILE]: (state, action) => {
    return {
      ...state,
      importFile: {
        ...state.importFile,
        file: action.payload
      }
    }
  },
  [GLOSSARY_UPDATE_IMPORT_FILE_LOCALE]: (state, action) => {
    return {
      ...state,
      importFile: {
        ...state.importFile,
        transLocale: action.payload
      }
    }
  },
  [GLOSSARY_TOGGLE_IMPORT_DISPLAY]: (state, action) => {
    return {
      ...state,
      importFile: {
        ...state.importFile,
        show: action.payload
      }
    }
  },
  [GLOSSARY_TOGGLE_EXPORT_DISPLAY]: (state, action) => {
    return {
      ...state,
      exportFile: {
        ...state.exportFile,
        show: action.payload
      }
    }
  },
  [GLOSSARY_UPDATE_EXPORT_TYPE]: (state, action) => {
    return {
      ...state,
      exportFile: {
        ...state.exportFile,
        type: action.payload
      }
    }
  },
  [GLOSSARY_EXPORT_REQUEST]: (state, _action) => {
    return {
      ...state,
      exportFile: {
        ...state.exportFile,
        status: 1
      }
    }
  },
  [GLOSSARY_EXPORT_SUCCESS]: (state, _action) => {
    return {
      ...state,
      exportFile: {
        ...state.exportFile,
        status: -1,
        show: false
      }
    }
  },
  [GLOSSARY_EXPORT_FAILURE]: (state, _action) => {
    return {
      ...state,
      exportFile: {
        ...state.exportFile,
        status: -1,
        show: false
      },
      notification: {
        severity: SEVERITY.ERROR,
        message: 'We are unable to export glossary entries from server. ' +
        'Please refresh this page and try again.'
      }
    }
  },
  [GLOSSARY_TOGGLE_NEW_ENTRY_DISPLAY]: (state, action) => {
    return {
      ...state,
      newEntry: {
        ...state.newEntry,
        show: action.payload
      }
    }
  },
  [GLOSSARY_TOGGLE_DELETE_ALL_ENTRIES_DISPLAY]: (state, action) => {
    return {
      ...state,
      deleteAll: {
        ...state.deleteAll,
        show: action.payload
      }
    }
  },
  [GLOSSARY_RESET_TERM]: (state, action) => {
    return {
      ...state,
      // @ts-ignore
      selectedTerm: cloneDeep(state.terms[action.payload])
    }
  },
  [GLOSSARY_UPDATE_FIELD]: (state, action) => {
    let newSelectedTerm = cloneDeep(state.selectedTerm)
    // @ts-ignore payload
    switch (action.payload.field) {
      case 'src':
        // @ts-ignore payload
        newSelectedTerm.srcTerm.content = action.payload.value
        break
      case 'locale':
        if (newSelectedTerm.transTerm) {
          // @ts-ignore payload
          newSelectedTerm.transTerm.content = action.payload.value
        } else {
          newSelectedTerm.transTerm =
            GlossaryHelper.generateEmptyTerm(state.locale)
          // @ts-ignore payload
          newSelectedTerm.transTerm.content = action.payload.value
        }
        if (isEmpty(newSelectedTerm.transTerm.content)) {
          newSelectedTerm.transTerm.comment = null
        }
        break
      case 'pos':
        // @ts-ignore payload
        newSelectedTerm.pos = action.payload.value
        break
      case 'description':
        // @ts-ignore payload
        newSelectedTerm.description = action.payload.value
        break
      case 'comment':
        if (newSelectedTerm.transTerm) {
          // @ts-ignore payload
          newSelectedTerm.transTerm.comment = action.payload.value
        } else {
          console.error('comment not allow for empty translation')
        }
        break
      default: console.error('Not a valid field')
    }
    newSelectedTerm.status = GlossaryHelper.getEntryStatus(
      newSelectedTerm, state.terms[newSelectedTerm.id])
    return {
      ...state,
      selectedTerm: newSelectedTerm
    }
  },
  [GLOSSARY_STATS_REQUEST]: (state, action) => {
    if (action.error) {
      return {
        ...state,
        notification: {
          severity: SEVERITY.ERROR,
          message: ERROR_MSG
        }
      }
    } else {
      return {
        ...state,
        statsError: false,
        statsLoading: true
      }
    }
  },
  [GLOSSARY_STATS_SUCCESS]: (state, action) => {
    // @ts-ignore
    const transLocales = isEmpty(action.payload.transLocale)
      ? []
      // @ts-ignore any and payload
      : action.payload.transLocale.map(result => ({
        value: result.locale.localeId,
        label: result.locale.displayName,
        count: result.numberOfTerms
      }))
    return ({
      ...state,
      stats: {
        // @ts-ignore
        srcLocale: action.payload.srcLocale,
        transLocales: transLocales
      },
      statsLoading: false
    })
  },
  [GLOSSARY_STATS_FAILURE]: (state, action) => ({
    ...state,
    statsError: true,
    statsErrorMessage: action.payload,
    statsLoading: false,
    notification: {
      severity: SEVERITY.ERROR,
      message: ERROR_MSG
    }
  }),
  [GLOSSARY_TERMS_INVALIDATE]: (state, _action) => ({
    ...state,
    termsDidInvalidate: true
  }),
  [GLOSSARY_DELETE_REQUEST]: (state, action) => {
    if (action.error) {
      return {
        ...state,
        notification: {
          severity: SEVERITY.ERROR,
          message:
          'We were unable to delete the glossary term. ' +
          'Please refresh this page and try again.'
        }
      }
    }

    const entryId = action.payload
    return {
      ...state,
      deleting: {
        ...state.deleting,
        // @ts-ignore possible bug?
        [entryId]: entryId
      }
    }
  },
  [GLOSSARY_DELETE_SUCCESS]: (state, action) => {
    let deleting = cloneDeep(state.deleting)
    // @ts-ignore
    const entryId = action.payload.id
    delete deleting[entryId]
    let terms = cloneDeep(state.terms)
    delete terms[entryId]

    return {
      ...state,
      terms: terms,
      deleting: deleting
    }
  },
  [GLOSSARY_DELETE_FAILURE]: (state, _action) => ({
    ...state,
    notification: {
      severity: SEVERITY.ERROR,
      message:
        'We were unable to delete the glossary term. ' +
        'Please refresh this page and try again.'
    }
  }),

  [GLOSSARY_UPDATE_REQUEST]: (state, action) => {
    if (action.error) {
      return {
        ...state,
        notification: {
          severity: SEVERITY.ERROR,
          message:
          'We were unable to update the glossary term. ' +
          'Please refresh this page and try again.'
        }
      }
    } else {
      let saving = cloneDeep(state.saving)
      // @ts-ignore
      const entryId = action.payload.id
      saving[entryId] = cloneDeep(action.payload)
      return {
        ...state,
        saving: saving
      }
    }
  },
  [GLOSSARY_UPDATE_SUCCESS]: (state, action) => {
    let saving = cloneDeep(state.saving)
    let selectedTerm = state.selectedTerm
    let terms = cloneDeep(state.terms)
    // @ts-ignore
    forEach(action.payload.glossaryEntries, (rawEntry) => {
      const entry = GlossaryHelper.generateEntry(rawEntry, state.locale)
      terms[rawEntry.id] = entry

      if (selectedTerm && selectedTerm.id === rawEntry.id) {
        selectedTerm = cloneDeep(entry)
      }
      delete saving[entry.id]
    })

    return {
      ...state,
      saving: saving,
      terms: terms,
      selectedTerm: selectedTerm
    }
  },
  [GLOSSARY_UPDATE_FAILURE]: (state, _action) => {
    return {
      ...state,
      notification: {
        severity: SEVERITY.ERROR,
        message:
          'We were unable to update the glossary term. ' +
          'Please refresh this page and try again.'
      }
    }
  },
  [GLOSSARY_CREATE_REQUEST]: (state, action) => {
    if (action.error) {
      return {
        ...state,
        notification: {
          severity: SEVERITY.ERROR,
          message:
          'We were unable save glossary entry. ' +
          'Please refresh this page and try again.'
        }
      }
    } else {
      const newEntry = state.newEntry
      return {
        ...state,
        newEntry: {
          ...newEntry,
          isSaving: true
        }
      }
    }
  },
  [GLOSSARY_CREATE_SUCCESS]: (state, action) => {
    const newEntry = state.newEntry
    // @ts-ignore payload
    return (action.payload.warnings.length > 0)
      ? {
        ...state,
        newEntry: {
          ...newEntry,
          isSaving: false,
          entry: GlossaryHelper.generateEmptyEntry(state.src),
          show: false
        },
        notification: {
          severity: SEVERITY.ERROR,
          message:
            'We were unable to save the glossary entry.',
          // @ts-ignore
          description: action.payload.warnings,
          duration: null
        }
      }
      : {
        ...state,
        newEntry: {
          ...newEntry,
          isSaving: false,
          entry: GlossaryHelper.generateEmptyEntry(state.src),
          show: false
        },
        notification: {
          severity: SEVERITY.INFO,
          message: 'Glossary term created.',
          duration: 3.5
        }
      }
  },
  [GLOSSARY_CREATE_FAILURE]: (state, action) => {
    const newEntry = state.newEntry
    return {
      ...state,
      newEntry: {
        ...newEntry,
        isSaving: false,
        entry: GlossaryHelper.generateEmptyEntry(state.src),
        show: false
      },
      notification: {
        severity: SEVERITY.ERROR,
        message:
        'We were unable to save the glossary entry.',
        // @ts-ignore
        description: action.payload.response,
        duration: null
      }
    }
  },
  [GLOSSARY_TERMS_REQUEST]: (state, action) => {
    if (action.error) {
      return state
    } else {
      return {
        ...state,
        termsError: action.error,
        termsErrorMessage: action.payload,
        termsLoading: true
      }
    }
  },
  [GLOSSARY_TERMS_SUCCESS]: (state, action) => {
    let terms = {}
    // @ts-ignore payload
    forEach(action.payload.entities.glossaryTerms, (entry) => {
      terms[entry.id] = GlossaryHelper.generateEntry(entry, state.locale)
    })

    return {
      ...state,
      termsLoading: false,
      // @ts-ignore
      termsLastUpdated: action.meta.receivedAt,
      terms,
      // @ts-ignore
      termIds: action.payload.result.results,
      // @ts-ignore
      termCount: action.payload.result.totalCount
    }
  },
  [GLOSSARY_TERMS_FAILURE]: (state, action) => ({
    ...state,
    termsError: action.error,
    termsErrorMessage: action.payload,
    termsLoading: false
  }),
  [GLOSSARY_SELECT_TERM]: (state, action) => {
    // @ts-ignore
    let selectedTerm = cloneDeep(state.terms[action.payload])
    return {
      ...state,
      selectedTerm: selectedTerm
    }
  },
  [GLOSSARY_DELETE_ALL_REQUEST]: (state, _action) => {
    const deleteAll = state.deleteAll
    return {
      ...state,
      deleteAll: {
        ...deleteAll,
        isDeleting: true
      }
    }
  },
  [GLOSSARY_DELETE_ALL_SUCCESS]: (state, _action) => {
    const deleteAll = state.deleteAll
    return {
      ...state,
      deleteAll: {
        ...deleteAll,
        show: false,
        isDeleting: false
      }
    }
  },
  [GLOSSARY_DELETE_ALL_FAILURE]: (state, _action) => {
    const deleteAll = state.deleteAll
    return {
      ...state,
      deleteAll: {
        ...deleteAll,
        isDeleting: false
      },
      notification: {
        severity: SEVERITY.ERROR,
        message:
        'We were unable to delete glossary entries. ' +
        'Please refresh this page and try again.'
      }
    }
  },
  [GLOSSARY_GET_QUALIFIED_NAME_REQUEST]: (state, action) => {
    if (action.error) {
      return {
        ...state,
        notification: {
          severity: SEVERITY.ERROR,
          message: ERROR_MSG
        }
      }
    } else {
      return state
    }
  },
  [GLOSSARY_GET_QUALIFIED_NAME_SUCCESS]: (state, action) => {
    if (action.error) {
      return {
        ...state,
        notification: {
          severity: SEVERITY.ERROR,
          message: ERROR_MSG
        }
      }
    } else {
      return {
        ...state,
        qualifiedName: action.payload
      }
    }
  },
  [GLOSSARY_GET_QUALIFIED_NAME_FAILURE]: (state, _action) => {
    return {
      ...state,
      notification: {
        severity: SEVERITY.ERROR,
        message: ERROR_MSG
      }
    }
  }
},
// default state
  /** @type {import('./state').GlossaryState} */
  ({
    src: DEFAULT_LOCALE.localeId,
    locale: '',
    filter: '',
    sort: {
      src_content: true
    },
    selectedTerm: {},
    permission: {
      canAddNewEntry: false,
      canUpdateEntry: false,
      canDeleteEntry: false,
      canDownload: false
    },
    terms: {},
    termIds: [],
    termCount: 0,
    termsError: false,
    termsLoading: true,
    termsDidInvalidate: false,
    stats: {
      srcLocale: {},
      transLocales: []
    },
    saving: {},
    deleting: {},
    importFile: {
      show: false,
      status: -1,
      file: null,
      transLocale: null
    },
    exportFile: {
      show: false,
      type: {value: FILE_TYPES[0], label: FILE_TYPES[0]},
      status: -1,
      types: [
        {value: FILE_TYPES[0], label: FILE_TYPES[0]},
        {value: FILE_TYPES[1], label: FILE_TYPES[1]}
      ]
    },
    newEntry: {
      show: false,
      isSaving: false,
      entry: GlossaryHelper.generateEmptyEntry(DEFAULT_LOCALE.localeId)
    },
    deleteAll: {
      show: false,
      isDeleting: false
    },
    statsError: false,
    statsLoading: false,
    notification: undefined,
    result: undefined,
    entities: undefined,
    warnings: undefined,
    id: undefined,
    srcLocale: undefined,
    transLocale: undefined,
    value: undefined,
    field: undefined,
    deleteGlossary: undefined,
    downloadGlossary: undefined,
    insertGlossary: undefined,
    projectSlug: undefined,
    query: undefined,
    updateGlossary: undefined,
    glossaryEntries: undefined,
    response: undefined,
  }))

export default glossary
