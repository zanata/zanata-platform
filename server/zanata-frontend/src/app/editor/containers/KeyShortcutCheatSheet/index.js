const cx /* TS: import cx */ = require('classnames')
import { Icon } from '../../../components'
import KeyCombinations from '../../components/KeyCombinations'
import { chain, each, map, values } from 'lodash'
import { connect } from 'react-redux'
import * as React from 'react'
import * as PropTypes from 'prop-types'
import { getShortcuts, getKeyShortcutsVisible } from '../../reducers'
import { toggleKeyboardShortcutsModal } from '../../actions/header-actions'

const KEY_SHORTCUT_TYPE = PropTypes.shape({
  description: PropTypes.string,
  keyConfig: PropTypes.shape({
    keys: PropTypes.array.isRequired,
    // array of KEY_SHORTCUT_TYPE, but not worth the workaround to define it
    sequenceKeys: PropTypes.array
  }).isRequired
})

/**
 * Modal showing a summary of the available key shortcuts.
 */
class KeyShortcutCheatSheet extends React.Component {
  static propTypes = {
    show: PropTypes.bool.isRequired,
    // could specify each shortcut key, but that is overkill here.
    shortcuts: PropTypes.arrayOf(KEY_SHORTCUT_TYPE).isRequired,
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
    const { onClose, shortcuts, show } = this.props
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
              {chain(shortcuts)
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
    show: getKeyShortcutsVisible(state),
    shortcuts: values(getShortcuts(state))
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
