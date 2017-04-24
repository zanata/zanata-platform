import React from 'react'
import { storiesOf } from '@kadira/storybook'
import { action, decorateAction } from '@kadira/storybook-addon-actions'
import RealEditorSearchInput from '.'

// Simple wrapper component to handle text updates for the story
// Uses the component name so it shows accurately how to use it
class EditorSearchInput extends React.Component {
  constructor(props) {
    super(props)
    this.state = props.search
    this.updateSearch = ::this.updateSearch
  }

  updateSearch (search) {
    // ensure the action is visible in the logger
    this.props.updateSearch(search)
    this.setState(search)
  }

  render () {
    return (
      <RealEditorSearchInput
        search={this.state}
        updateSearch={this.updateSearch}
      />
    )
  }
}

const blankSearch = {
  text: '',
  resourceId: '',
  lastModifiedBy: '',
  lastModifiedBefore: '',
  lastModifiedAfter: '',
  sourceComment: '',
  translationComment: '',
  msgctxt: ''
}

storiesOf('EditorSearchInput', module)
  .add('empty', () => (
    <EditorSearchInput
      search={blankSearch}
      updateSearch={action('updateSearch')}
    />
  ))
  .add('text search', () => (
    <EditorSearchInput
      search={{
        ...blankSearch,
        text: 'hello there'
      }}
      updateSearch={action('updateSearch')}
    />
  ))
