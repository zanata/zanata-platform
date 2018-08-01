/* eslint-disable */
import { connect } from 'react-redux'
import { IntlProvider } from 'react-intl'
import { formats } from '../../config/intl'

// This function will map the current redux state to the props for the component that it is "connected" to.
// When the state of the redux store changes, this function will be called, if the props that come out of
// this function are different, then the component that is wrapped is re-rendered.
// @ts-ignore
function mapStateToProps (state) {
  return {
      locale: state.headerData.selectedI18nLocale,
      key: state.headerData.selectedI18nLocale,
      messages: state.headerData.localeMessages,
      formats: formats
    }
}
// @ts-ignore
export default connect(mapStateToProps)(IntlProvider)
