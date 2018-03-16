// @ts-nocheck
import React from 'react'
import { action } from '@storybook/addon-actions'
import { storiesOf } from '@storybook/react'
import GlossaryTerm from '.'
import { Table } from 'react-bootstrap'

const copyGlossaryTerm = action('copyGlossaryTerm')
const showDetails = action('showDetails')

/*
 * See .storybook/README.md for info on the component storybook.
 */
storiesOf('GlossaryTerm', module)
  .add('simple term on its own', () => (
    <Table>
      <tbody>
        <GlossaryTerm key={0} index={0}
          copyGlossaryTerm={copyGlossaryTerm}
          showDetails={showDetails}
          term={{
            source: 'Ambulance',
            target: 'Krankenwagen'
          }} />
      </tbody>
    </Table>
  ))
  .add('in a table', () => (
    <Table>
      <thead>
        <tr>
          <th>Source</th>
          <th>Translation</th>
          <th></th>
          <th className="align-right hide-md">Details</th>
        </tr>
      </thead>
      <tbody>
        <GlossaryTerm key={0} index={0}
          copyGlossaryTerm={copyGlossaryTerm}
          showDetails={showDetails}
          term={{
            source: 'Ambulance',
            target: 'Krankenwagen'
          }} />
        <GlossaryTerm key={1} index={1}
          copyGlossaryTerm={copyGlossaryTerm}
          showDetails={showDetails}
          term={{
            source: 'Hospital',
            target: 'Krankenhaus'
          }} />
        <GlossaryTerm key={2} index={2}
          copyGlossaryTerm={copyGlossaryTerm}
          showDetails={showDetails}
          term={{
            source: 'Doctor',
            target: 'Arzt'
          }} />
      </tbody>
    </Table>
  ))
  .add('Without translations', () => (
    <Table>
      <thead>
        <tr>
          <th>Source</th>
          <th>Translation</th>
          <th></th>
          <th className="align-right hide-md">Details</th>
        </tr>
      </thead>
      <tbody>
        <GlossaryTerm key={0} index={0}
          copyGlossaryTerm={copyGlossaryTerm}
          showDetails={showDetails}
          term={{
            source: 'Ambulance',
            target: ''
          }} />
        <GlossaryTerm key={1} index={1}
          copyGlossaryTerm={copyGlossaryTerm}
          showDetails={showDetails}
          term={{
            source: 'Hospital',
            target: ''
          }} />
        <GlossaryTerm key={2} index={2}
          copyGlossaryTerm={copyGlossaryTerm}
          showDetails={showDetails}
          term={{
            source: 'Doctor',
            target: ''
          }} />
      </tbody>
    </Table>
  ))
