import React from 'react'
import { storiesOf, action } from '@kadira/storybook'
import { EditableText } from '../'

storiesOf('EditableText', module)
    .add('default', () => (
        <EditableText
            className='editable textInput text-state-classes'
            maxLength={255}
            placeholder='Add a description…'
            emptyReadOnlyText='No description'>
          Test text
        </EditableText>
    ))

