import cx from 'classnames'
import { Icon } from '../../../components'
import KeyCombinations from '../../components/KeyCombinations'
import { chain, each, map } from 'lodash'
import { connect } from 'react-redux'
import React from 'react'
import PropTypes from 'prop-types'
import { SHORTCUTS } from '../../actions/key-shortcuts-actions'
import { toggleKeyboardShortcutsModal } from '../../actions/header-actions'

/**
 * Modal showing a summary of the available key shortcuts.
 */
class KeyShortcutCheatSheet extends React.Component {
  static propTypes = {
    show: PropTypes.bool.isRequired,
    onClose: PropTypes.func.isRequired,
    className: PropTypes.string
  }

  /**
   * Convert a shortcut with sequence keys to an array of simple shortcuts.
   * Non-sequence shortcuts are returned unchanged.
   *
   * Map this then use flatten to get a flat list of normal and sequence keys.
   */
  expandSequences = (shortcut) => {
    const { sequenceKeys } = shortcut.keyConfig
    if (sequenceKeys) {
      const prefix = shortcut.keyConfig.keys[0] + ' '
      const shortcuts = [shortcut]
      each(sequenceKeys, (seqKey) => {
        const keys = map(seqKey.keyConfig.keys, (key) => {
          return prefix + key
        })
        const keyConfig = { ...seqKey.keyConfig, keys }
        shortcuts.push({ ...seqKey, keyConfig })
      })
      return shortcuts
    }
    return shortcut
  }

  renderShortcut = (shortcut) => {
    const { keys } = shortcut.keyConfig
    return (
      <li className="Grid" key={keys.join()}>
        <div className="Grid-cell u-size1of2 u-sPR-1-4 u-sPV-1-4 u-textRight">
          <KeyCombinations keys={keys} />
        </div>
        <div className="Grid-cell u-size1of2 u-sPL-1-4 u-sPV-1-4">
          {shortcut.description}
        </div>
      </li>
    )
  }

  render () {
    const { onClose, show } = this.props
    const className = cx(this.props.className, 'Modal', {
      'is-active': show
    })

    // TODO string-i18n
    return (
      <div className={className}>
        <div className="Modal-dialog">
          <div className="Modal-header">
            <h2 className="Modal-title">Keyboard Shortcuts</h2>
            <button className="Modal-close Link Link--neutral"
              onClick={onClose}>
              <Icon name="cross" title="Close" className="s1" />
            </button>
          </div>
          <div className="Modal-content u-sP-1">
            <ul>
              {chain(SHORTCUTS).values()
                .map(this.expandSequences).flatten()
                .map(this.renderShortcut)
                .value()}
            </ul>
          </div>
        </div>
      </div>
    )
  }
}

function mapStateToProps (state) {
  return {
    show: state.ui.panels.keyShortcuts.visible
  }
}

function mapDispatchToProps (dispatch) {
  return {
    onClose: () => {
      dispatch(toggleKeyboardShortcutsModal())
    }
  }
}

export default connect(mapStateToProps, mapDispatchToProps
  )(KeyShortcutCheatSheet)
