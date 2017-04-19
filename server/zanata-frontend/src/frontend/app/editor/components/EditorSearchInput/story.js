import React from 'react'
import { storiesOf } from '@kadira/storybook'
import { action, decorateAction } from '@kadira/storybook-addon-actions'
import RealEditorSearchInput from '.'

// Simple wrapper component to handle text updates for the story
// Uses the component name so it shows accurately how to use it
class EditorSearchInput extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      text: props.text
    }
    this.updateText = ::this.updateText
  }

  updateText (text) {
    // ensure the action is visible in the logger
    this.props.updateText(text)
    this.setState({ text })
  }

  render () {
    return (
      <RealEditorSearchInput
        toggleDisplay={this.props.toggleDisplay}
        text={this.state.text}
        updateText={this.updateText}
      />
    )
  }
}

storiesOf('EditorSearchInput', module)
  .add('empty', () => (
    <EditorSearchInput
      text=""
      toggleDisplay={action('toggleDisplay')}
      updateText={action('updateText')}
    />
  ))
  .add('simple search', () => (
    <EditorSearchInput
      toggleDisplay={action('toggleDisplay')}
      text="text: hello there"
      updateText={action('updateText')}
    />
  ))
