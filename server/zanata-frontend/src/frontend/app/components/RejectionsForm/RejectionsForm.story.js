import React from 'react'
import { storiesOf, action } from '@storybook/react'
import { Button } from 'react-bootstrap'
import { Icon, TextInput } from '../../components'
import RejectionsForm from '.'

storiesOf('RejectionsForm', module)
  .add('not editable', () => (
      <RejectionsForm
          editable={false}
          criteriaPlaceholder='Format'
          subcatPlaceholder='More text'
          priority='Critical' textState='text-danger' />
   ))
   .add('editable', () => (
        <RejectionsForm
            className='active'
            editable={true}
            criteriaPlaceholder='Format'
            subcatPlaceholder='More text'
            priority='Major' textState='text-warning' />
    ))
   .add('Admin screen', () => (
      <div className='container'>
        <h1>Reject translations settings</h1>
        <RejectionsForm
            className='active'
            editable={true}
            criteriaPlaceholder='Format'
            subcatPlaceholder='More text'
            priority='Critical' textState='text-danger' />
        <Button bsStyle='primary' className='btn-left'>
          <Icon name='plus' className='s1' /> Add review criteria
        </Button>
        <Button className='active' bsStyle='info'>
          <Icon name='refresh' className='s1' /> Revert to default
        </Button>
      </div>
  ))

