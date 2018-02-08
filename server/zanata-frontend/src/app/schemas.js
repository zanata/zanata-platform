import { Schema, arrayOf } from 'normalizr'

// Read more about Normalizr: https://github.com/gaearon/normalizr

export const GLOSSARY_TERM = new Schema('glossaryTerms')
export const GLOSSARY_TERM_ARRAY = arrayOf(GLOSSARY_TERM)

export const PROJECT = new Schema('Project')
export const PROJECT_ARRAY = arrayOf(PROJECT)

export const GROUP = new Schema('Group')
export const GROUP_ARRAY = arrayOf(GROUP)

export const LANGUAGE_TEAM = new Schema('LanguageTeam')
export const LANGUAGE_TEAM_ARRAY = arrayOf(LANGUAGE_TEAM)

export const PERSON = new Schema('Person')
export const PERSON_ARRAY = arrayOf(PERSON)

const SEARCH_TYPES = {
  Project: PROJECT,
  LanguageTeam: LANGUAGE_TEAM,
  Person: PERSON,
  Group: GROUP
}

export const SEARCH_RESULTS = arrayOf(SEARCH_TYPES, { schemaAttribute: 'type' })
