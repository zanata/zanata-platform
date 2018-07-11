import { connect } from 'react-redux'
import GlossaryTermModal from './component'
import { map } from 'lodash'
import { showGlossaryDetails } from '../../actions/glossary-actions'
import { EditorState } from '../../reducers/state'

function mapStateToProps (state: EditorState) {
  const { context, glossary, headerData } = state
  const { details, results, searchText } = glossary
  const { byId, resultIndex, show } = details
  const term = results.get(searchText)[resultIndex]

  // undefined items are kept, that just indicates the detail has not been
  // returned from the API yet.
  const detailItems = term && term.sourceIdList
    // @ts-ignore
    ? map(term.sourceIdList, id => byId[id]) : []

  return {
    show,
    sourceLocale: context.sourceLocale.localeId,
    targetLocale: headerData.context.selectedLocale,
    term,
    details: detailItems
  }
}

// @ts-ignore any
function mapDispatchToProps (dispatch) {
  return {
    // @ts-ignore
    close: () => dispatch(showGlossaryDetails(false))
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(GlossaryTermModal)
