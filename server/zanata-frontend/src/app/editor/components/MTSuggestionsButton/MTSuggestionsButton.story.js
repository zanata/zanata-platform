import React from 'react'
import MTSuggestionsButton from '.'
import { storiesOf } from '@storybook/react';

/*
 * See .storybook/README.md for info on the component storybook.
 */
// @ts-ignore any
storiesOf('MTSuggestionsButton', module)
    .add('default', () =>(
        <MTSuggestionsButton />)
    )
