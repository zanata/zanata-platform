import React from 'react'
import { storiesOf } from '@kadira/storybook'
import { action, decorateAction } from '@kadira/storybook-addon-actions'
import { Icons } from 'zanata-ui'

import GlossarySearchInput from '.'

// Showing two different approaches to modifying the arguments to an action
// function before they are logged in the storybook:

// For a single argument, this just wraps the action function with something
// that takes an argument in and digs a value out of it. It would work with
// multiple arguments too.
// Just pass the function object as a prop
const onTextChange = event => action('onTextChange')(event.target.value)
// e.g. onFoo = (arg1, arg2, ...) => action('onFoo')(arg1.foo.bar, arg2.x, ...)

// decorateAction gets an array of arguments passed in and expects an array of
// arguments returned. It is a pipeline for rewriting the arguments.
// Call the function in the prop, same as you would action('onDoStuff')
const eventTarget = decorateAction([
  args => [args[0].target.value]
])

/*
 * See .storybook/README.md for info on the component storybook.
 */
storiesOf('GlossarySearchInput', module)
  .addDecorator((story) => (
    <div>
      <Icons />
      {story()}
    </div>
  ))
    .add('empty', () => (
        <GlossarySearchInput
          text=""
          onTextChange={onTextChange} />
    ))
    .add('with text', () => {
      return (
        <GlossarySearchInput
          text={'some text'}
          onTextChange={eventTarget('onTextChange')}/>
      )
    })
