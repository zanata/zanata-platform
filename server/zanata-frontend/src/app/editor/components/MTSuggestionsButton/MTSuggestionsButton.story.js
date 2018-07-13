// @ts-nocheck
/* eslint-disable */
import React from 'react'
import MTSuggestionsButton from './index.tsx'
import { storiesOf } from '@storybook/react';

/*
 * See .storybook/README.md for info on the component storybook.
 */
// @ts-ignore any
storiesOf('MTSuggestionsButton', module)
    .add('Google translations', () =>(
        <MTSuggestionsButton backendId='Google' />)
    )
    .add('Microsoft translations', () =>(
        <MTSuggestionsButton backendId='Ms' />)
    )
