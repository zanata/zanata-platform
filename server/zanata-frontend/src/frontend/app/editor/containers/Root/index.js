import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { connect } from 'react-redux'
import MainContent from '../MainContent'
import ParamPropDispatcher from '../ParamPropDispatcher'
import EditorHeader from '../EditorHeader'
import KeyShortcutCheatSheet from '../KeyShortcutCheatSheet'
import KeyShortcutDispatcher from '../KeyShortcutDispatcher'
import SuggestionsPanel from '../SuggestionsPanel'
import { setSidebarVisibility } from '../../actions'
import { fetchUiLocales } from '../../actions/header-actions'
import { saveSuggestionPanelHeight } from '../../actions/suggestions-actions'
import SplitPane from 'react-split-pane'
import { Icons } from '../../../components'
import Sidebar from '../Sidebar'

/**
 * Top level of Zanata view hierarchy.
 */
class Root extends Component {
  static propTypes = {
    percentHeight: PropTypes.number.isRequired,
    showSidebar: PropTypes.bool.isRequired,
    showSuggestion: PropTypes.bool,
    requestUiLocales: PropTypes.func.isRequired,
    setSidebarVisible: PropTypes.func.isRequired,
    saveSuggestionPanelHeight: PropTypes.func.isRequired
  }

  componentDidMount () {
    this.props.requestUiLocales()
    window.addEventListener('resize', this.onWindowResize)
  }

  componentWillUnmount () {
    window.removeEventListener('resize', this.onWindowResize)
  }

  // TODO could debounce this
  onWindowResize = (e) => {
    // Reach in and override the dragged pixel size of the resizer component.
    // This is a workaround, needed because once the resizer is dragged, only
    // the size prop will override the dragged size, but specifying size prop
    // stops drag resize from working.
    if (this.refs && this.refs.suggestionResizer) {
      const pixelHeight = this.props.showSuggestion
        ? this.props.percentHeight * window.innerHeight
        : 0
      this.refs.suggestionResizer.setState({
        draggedSize: pixelHeight
      })
      // trigger a re-render so the new panel size is shown properly
      this.forceUpdate()
    }
  }

  resizeFinished = () => {
    // draggedSize is defined as soon as any drag-resize happens,
    // so no need to save the height if it has not been set
    if (this.refs && this.refs.suggestionResizer &&
      this.refs.suggestionResizer.state.draggedSize &&
      this.props.showSuggestion) {
      const panelSize = this.refs.suggestionResizer.state.draggedSize
      this.props.saveSuggestionPanelHeight(panelSize)
    }
  }

  render () {
    const pixelHeight = this.props.showSuggestion
      ? this.props.percentHeight * window.innerHeight
      : 0

    // TODO adjust scrollbar width on div like Angular template editor.html
    return (
      <ParamPropDispatcher {...this.props}>
        <KeyShortcutDispatcher className="Editor is-suggestions-active">
          <Icons />
          <EditorHeader />
          <Sidebar open={this.props.showSidebar}
            setSidebarVisible={this.props.setSidebarVisible}>
            <SplitPane ref="suggestionResizer"
              split="horizontal"
              defaultSize={pixelHeight}
              primary="second"
              onDragFinished={this.resizeFinished}>
              <MainContent />
              {this.props.showSuggestion && <SuggestionsPanel />}
            </SplitPane>
          </Sidebar>
          <KeyShortcutCheatSheet />
        </KeyShortcutDispatcher>
      </ParamPropDispatcher>
    )
  }
}

function mapStateToProps ({ ui }) {
  return {
    percentHeight: ui.panels.suggestions.heightPercent,
    showSidebar: ui.panels.sidebar.visible,
    showSuggestion: ui.panels.suggestions.visible
  }
}

function mapDispatchToProps (dispatch) {
  return {
    setSidebarVisible: (visible) => {
      dispatch(setSidebarVisibility(visible))
    },
    saveSuggestionPanelHeight: (pixelHeight) => {
      const percent = pixelHeight / window.innerHeight
      dispatch(saveSuggestionPanelHeight(percent))
    },
    requestUiLocales: () => {
      dispatch(fetchUiLocales())
    }
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(Root)
