import React from 'react'
import { storiesOf } from '@storybook/react'
import { EditableText } from '../'

storiesOf('EditableText', module)
    .add('editing', () => (
        <span>
          <h2><img src="https://upload.wikimedia.org/wikipedia/commons/4/49/Zanata-Logo.svg" width="42px" /> EditableText</h2>
        <EditableText
            className='editable textInput textState'
            maxLength={255}
            editable={true}
            editing={true}
            placeholder='Add a description…'
            emptyReadOnlyText='No description'>
          Test text
        </EditableText>
        </span>
    ))
    .add('not editing', () => (
        <EditableText
            className='editable textInput textState'
            maxLength={255}
            editable={true}
            editing={false}
            placeholder='Add a description…'
            emptyReadOnlyText='No description'>
          Test text
        </EditableText>
    ))
