import { DeepReadonly } from '../../utils/DeepReadonly'
import { RouterState } from 'react-router-redux'
import {
  ENTER_SAVES_IMMEDIATELY,
  SYNTAX_HIGHLIGTING,
  SUGGESTIONS_DIFF,
  KEY_SUGGESTIONS_VISIBLE,
  HTML_XML,
  NEW_LINE,
  TAB,
  JAVA_VARIABLES,
  XML_ENTITY,
  PRINTF_VARIABLES,
  PRINTF_XSI_EXTENSION } from './settings-reducer'
import { GLOSSARY_TAB, ACTIVITY_TAB} from './ui-reducer'
import { LocaleId } from '../../utils/prop-types-util'

/* tslint:disable interface-over-type-literal */

// TODO reverse engineer nested types (anys) by looking at rootReducer in index.js
export type EditorState = Readonly<{
  activity: ActivityState
  context: EditorContextState
  dropdown: DropdownState
  glossary: EditorGlossaryState
  headerData: HeaderDataState
  phrases: PhrasesState
  routing: RouterState
  settings: EditorSettingsState
  suggestions: SuggestionsState
  review: ReviewState
  ui: EditorUIState
}>

export type EditorContextState = DeepReadonly<{
  sourceLocale: {
    localeId: string
    name: string
    isRTL: boolean
  }
}>

export type HeaderDataState = DeepReadonly<{
  user: {
    name: string
    gravatarUrl: string
    dashboardUrl: string
  }
  context: {
    projectVersion: {
      project: {
        slug: string
        name: string
      }
      version: string
      url: string
      docs: any[]
      locales: any
    }
    selectedDoc: {
      counts: {
        total: number
        approved: number
        rejected: number
        translated: number
        needswork: number
        untranslated: number,
        mt: number
      }
      id: string
    }
    selectedLocale: string
  }
  permissions: {
    reviewer: boolean
    translator: boolean
  }
  localeMessages?: any,
  selectedI18nLocale: string
}>

export type DropdownState = DeepReadonly<{
  openDropdownKey?: any
  docsKey: string
  localeKey: string
  uiLocaleKey: string
}>

export type EditorGlossaryState = Readonly<{
  searchText: string
  searching: boolean
  // searchText -> results array [each result seems to have sourceIdList]
  results: Map<string, any>
  // FIXME should have a timestamp per set of results in the map
  resultsTimestamp: Date
  details: {
    show: boolean
    /* Which glossary result to show detail for. Only valid when show is true */
    resultIndex: number
    /* Are details currently being fetched? */
    fetching: boolean
    /* detail keyed by id in results[x].sourceIdList */
    byId: any
  }
}>

export type PhrasesState = {
    fetchingList: boolean
    fetchingFilteredList: boolean
    filteredListTimestamp: Date
    fetchingDetail: boolean
    saveAsMode: boolean
    // expected shape: { [docId1]: [{ id, resId, status }, ...], [docId2]: [...] }
    inDoc: any
    inDocFiltered: any
    // expected shape: { [phraseId1]: phrase-object, [phraseId2]: ..., ...}
    detail: any
    notification: any
    selectedPhraseId: any
    /* Cursor/selection position within the currently editing translation, used
     * for inserting terms from glossary etc. */
    selectedTextRange: {
      start: number
      end: number
    },
    paging: {
      countPerPage: number
      pageIndex: number
    },
    filter: PhraseFilterState
}

export type PhraseFilterState = {
  showAdvanced: boolean
  advanced: {
    searchString: string
    resId: string
    lastModifiedByUser: string
    changedBefore: string
    changedAfter: string
    sourceComment: string
    transComment: string
    msgContext: string
  },
  status: PhraseFilterStatus
}

export type PhraseFilterStatus = {
  all: boolean
  approved: boolean
  rejected: boolean
  translated: boolean
  needswork: boolean
  untranslated: boolean,
  mt: boolean
}

export type Setting = {
  value: any
  saving: boolean
  error: any
}

export type EditorSettingsState = {
  // state for all settings being requested on app load
  fetching: boolean

  // error when attempt to load user settings fails
  error: any

  // state for individual settings
  settings: {
    [ENTER_SAVES_IMMEDIATELY]: Setting
    [SYNTAX_HIGHLIGTING]: Setting
    [SUGGESTIONS_DIFF]: Setting
    [KEY_SUGGESTIONS_VISIBLE]: Setting
    // Validator options disabled by default
    [HTML_XML]: Setting
    [NEW_LINE]: Setting
    [TAB]: Setting
    [JAVA_VARIABLES]: Setting
    [XML_ENTITY]: Setting
    [PRINTF_VARIABLES]: Setting
    [PRINTF_XSI_EXTENSION]: Setting
  }
}

export type SuggestionsState = {
  searchType: string
  showDetailModalForIndex: any
  textSearch: {
    loading: boolean
    searchStrings: any[]
    suggestions: any[]
    timestamp: number
  }

  // searchStrings is just the source text that was used for the lookup
  // usually won't change, but could if new source is uploaded
  // { phraseId: { loading, searchStrings, suggestions, timestamp } }
  searchByPhrase: any

  search: {
    input: {
      text: string
      focused: boolean
    }
  }
}

export type ReviewState = {
  notification: any
  showReviewModal: boolean
  criteria: any[]
}

export type ActivityState = {
  transHistory: {
    historyItems: any
    latest: any
    reviewComments: any
  }
}

export type EditorUIState = {
  panels: {
    navHeader: {
      visible: boolean
    }
    sidebar: {
      visible: boolean
      selectedTab: typeof GLOSSARY_TAB | typeof ACTIVITY_TAB
    }
    suggestions: {
      heightPercent: number
    }
    keyShortcuts: {
      visible: boolean
    }
  }
  appLocaleData: any
  uiLocales: any
  selectedUiLocale: LocaleId
  showSettings: boolean
  gettextCatalog: {
    getString: (key: string) => string
  }
}
