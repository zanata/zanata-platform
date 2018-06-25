import { Locale } from '../utils/prop-types-util';

/* tslint:disable interface-over-type-literal */

// TODO eliminate all anys
// TODO eliminate optionality where appropriate


export interface TopLevelState {
  routing?: any
  explore?: any
  glossary?: any
  common?: any
  profile?: any
  languages?: any
  projectVersion: ProjectVersionState
  tmx?: any
  admin?: any
}

// TODO verify types
export type ProjectVersionState = {
  MTMerge: {
    showMTMerge: boolean
    processStatus: any
    queryStatus: any
  }
  TMMerge: {
    show: boolean
    triggered: boolean
    processStatus: any
    queryStatus: any
    projectVersions: any[]
  }
  projectResultsTimestamp?: Date
  locales?: Locale[]
  fetchingProject: boolean
  fetchingLocale: boolean
  notification: any
}
