import React from 'react'
import PropTypes from 'prop-types'
import { storiesOf } from '@storybook/react'
import { action } from '@storybook/addon-actions'
import { EditorSearchInput as RealEditorSearchInput } from '.'

// Simple wrapper component to handle text updates for the story
// Uses the component name so it shows accurately how to use it
class EditorSearchInput extends React.Component {
  static propTypes = {
    search: PropTypes.object.isRequired,
    showAdvanced: PropTypes.bool.isRequired,
    toggleAdvanced: PropTypes.func.isRequired,
    updateSearch: PropTypes.func.isRequired
  }

  constructor (props) {
    super(props)
    this.state = {
      search: props.search,
      showAdvanced: props.showAdvanced
    }
  }

  toggleAdvanced = () => {
    // ensure the action is visible in the logger
    this.props.toggleAdvanced()
    this.setState({
      showAdvanced: !this.state.showAdvanced
    })
  }

  updateSearch = (search) => {
    // ensure the action is visible in the logger
    this.props.updateSearch(search)
    this.setState({
      search: {
        ...this.state.search,
        ...search
      }
    })
  }

  render () {
    return (
      <RealEditorSearchInput
        showAdvanced={this.state.showAdvanced}
        search={this.state.search}
        updateSearch={this.updateSearch}
        toggleAdvanced={this.toggleAdvanced}
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
      showAdvanced={false}
      toggleAdvanced={action('toggleAdvanced')}
    />
  ))
  .add('simple search', () => (
    <EditorSearchInput
      search={{
        ...blankSearch,
        text: 'it was the'
      }}
      updateSearch={action('updateSearch')}
      showAdvanced={false}
      toggleAdvanced={action('toggleAdvanced')}
    />
  ))
  .add('advanced panel open', () => (
    <EditorSearchInput
      search={{
        ...blankSearch,
        text: 'it was the worst'
      }}
      updateSearch={action('updateSearch')}
      showAdvanced
      toggleAdvanced={action('toggleAdvanced')}
    />
  ))
  .add('advanced search', () => (
    <EditorSearchInput
      search={{
        text: 'it was the worst of',
        resourceId: 'para-0001',
        lastModifiedBy: 'cdickens',
        lastModifiedBefore: '1859-12-31',
        lastModifiedAfter: '1859-01-01',
        sourceComment: 'England and France',
        translationComment: 'blurst of times?! You stupid monkey!',
        msgctxt: 'chapter01.txt'
      }}
      updateSearch={action('updateSearch')}
      showAdvanced
      toggleAdvanced={action('toggleAdvanced')}
    />
  ))
