import React from 'react'
import { storiesOf } from '@kadira/storybook'
import { EditableText } from '../'

storiesOf('EditableText', module)
    .add('editing', () => (
        <EditableText
            className='editable textInput text-state-classes'
            maxLength={255}
            editable={true}
            editing={true}
            placeholder='Add a description…'
            emptyReadOnlyText='No description'>
          Test text
        </EditableText>
    ))
    .add('not editing', () => (
        <EditableText
            className='editable textInput text-state-classes'
            maxLength={255}
            editable={true}
            editing={false}
            placeholder='Add a description…'
            emptyReadOnlyText='No description'>
          Test text
        </EditableText>
    ))
