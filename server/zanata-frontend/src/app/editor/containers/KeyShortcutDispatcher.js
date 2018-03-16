// @ts-nocheck
import Combokeys from 'combokeys'
import globalBind from 'combokeys/plugins/global-bind'
import { setSaveAsMode } from '../actions/key-shortcuts-actions'
import { getShortcuts } from '../reducers'
import React from 'react'
import * as PropTypes from 'prop-types'
import { connect } from 'react-redux'
import { map } from 'lodash'

/**
 * Wraps a div around the content that can observe for key shortcut combinations
 * on bubbled events.
 */
class KeyShortcutDispatcher extends React.Component {
  static propTypes = {
    className: PropTypes.string,
    cancelSaveAs: PropTypes.func.isRequired,
    shortcutInfoList: PropTypes.arrayOf(PropTypes.shape({
      keyConfig: PropTypes.shape({
        sequenceKeys: PropTypes.arrayOf(PropTypes.shape({
          // Note: these are nested ShortcutInfos, but specifying infinite
          //       recursion with PropTypes is too much bother (i.e. I don't
          //       think it is possible). Flow can probably handle it. - damason
          keyConfig: PropTypes.object,
          handler: PropTypes.func.isRequired
        })),
        keys: PropTypes.arrayOf(PropTypes.string).isRequired,
        eventType: PropTypes.string
      }),
      handler: PropTypes.func.isRequired
    })),
    children: PropTypes.node
  }

  combokeys = undefined

  /**
   * Extend a handler to also register the next keys in the sequence.
   *
   * This is hard-coded to assume a save-as sequence. Need to fix that
   * before using for anything else.
   */
  makeSequenceHandler = (handler, sequenceKeys) => {
    return (event) => {
      handler(event)

      // handler is swapped out directly so that [Esc] will cancel the sequence
      const originalEscHandler = this.combokeys.directMap['esc:undefined']

      // clear all the sequence key handlers and call an action to clear the
      // save-as mode state in the application
      const endSaveAsMode = () => {
        this.props.cancelSaveAs()
        sequenceKeys.forEach(({ keyConfig }) => {
          this.deleteKeys(keyConfig)
        })
        this.enableKeysFor({ keys: ['esc'] }, originalEscHandler)
      }
      sequenceKeys.forEach(({ keyConfig, handler }) => {
        this.enableKeysFor(keyConfig, (event) => {
          endSaveAsMode()
          handler(event)
        })
      })
      this.enableKeysFor({ keys: ['esc'] }, endSaveAsMode)
    }
  }

  enableKeysFor = ({ keys, eventType }, handler) => {
    if (!Array.isArray(keys)) {
      throw new Error(
        'keyConfig does not contain a "keys" value that is an array')
    }
    // Note: eventType may be undefined
    this.combokeys.bindGlobal(keys, handler, eventType)
  }

  deleteKeys = ({ keys, eventType }) => {
    this.combokeys.unbind(keys, eventType)
  }

  bindShortcuts = (shortcuts) => {
    shortcuts.forEach(shortcutInfo => {
      const { keyConfig, handler } = shortcutInfo
      const sequenceKeys = keyConfig.sequenceKeys
      if (sequenceKeys) {
        this.enableKeysFor(keyConfig,
          this.makeSequenceHandler(handler, sequenceKeys))
      } else {
        this.enableKeysFor(keyConfig, handler)
      }
    })
  }

  componentDidMount () {
    const elem = this.shortcutContainer
    this.combokeys = globalBind(new Combokeys(elem))
    if (elem) {
      this.bindShortcuts(this.props.shortcutInfoList)
    } else {
      console.error('No shortcut container element is bound for this ' +
                    'KeyShortcutDispatcher')
    }
  }

  /* Ensure bindings are updated whenever the shortcuts change. */
  componentWillReceiveProps (newProps) {
    if (this.props.shortcutInfoList !== newProps.shortcutInfoList) {
      // reset removes all the bindings
      this.combokeys.reset()
      this.bindShortcuts(newProps.shortcutInfoList)
    }
  }

  componentWillUnmount () {
    if (this.combokeys) {
      this.combokeys.detach()
    }
  }

  setShortcutContainer = (ref) => {
    this.shortcutContainer = ref
  }

  render () {
    // tabIndex is to make it focusable.
    return (
      <div tabIndex={0}
        className={this.props.className}
        ref={this.setShortcutContainer}>
        {this.props.children}
      </div>
    )
  }
}

const mapStateToProps = state => ({
  shortcuts: getShortcuts(state)
})

const mapDispatchToProps = dispatch => {
  return {
    cancelSaveAs: () => { dispatch(setSaveAsMode(false)) },
    addHandlersRecursively
  }

  function addHandlersRecursively (shortcut) {
    const { eventActionCreator } = shortcut
    const unhandledSequenceKeys = shortcut.keyConfig.sequenceKeys
    const sequenceKeys = unhandledSequenceKeys
      ? map(unhandledSequenceKeys, addHandlersRecursively)
      : undefined
    const keyConfig = { ...shortcut.keyConfig, sequenceKeys }
    const handler = (event) => {
      return dispatch(eventActionCreator(event))
    }
    return {
      keyConfig,
      handler
    }
  }
}

const mergeProps =
  ({ shortcuts }, { addHandlersRecursively, ...dispatchProps }, ownProps) => ({
    ...ownProps,
    ...dispatchProps,
    shortcutInfoList: map(shortcuts, addHandlersRecursively)
  })

export default connect(mapStateToProps, mapDispatchToProps, mergeProps
  )(KeyShortcutDispatcher)
