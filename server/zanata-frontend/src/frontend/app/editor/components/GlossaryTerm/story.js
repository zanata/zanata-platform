import React from 'react'
import { storiesOf, action } from '@kadira/storybook'
import GlossaryTerm from '.'
import { Table } from 'react-bootstrap'
import { Icons } from 'zanata-ui'

const copyGlossaryTerm = action('copyGlossaryTerm')

/*
 * See .storybook/README.md for info on the component storybook.
 */
storiesOf('GlossaryTerm', module)
  .addDecorator((story) => (
    <div>
      <Icons />
      {story()}
    </div>
  ))
    .add('simple term on its own', () => (
      <Table>
        <tbody>
          <GlossaryTerm key={0} index={0} copyGlossaryTerm={copyGlossaryTerm}
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
            <th>Target</th>
            <th></th>
            <th className="align-right hide-md">Details</th>
          </tr>
        </thead>
        <tbody>
          <GlossaryTerm key={0} index={0} copyGlossaryTerm={copyGlossaryTerm}
            term={{
              source: 'Ambulance',
              target: 'Krankenwagen'
            }} />
          <GlossaryTerm key={1} index={1} copyGlossaryTerm={copyGlossaryTerm}
            term={{
              source: 'Hospital',
              target: 'Krankenhaus'
            }} />
          <GlossaryTerm key={2} index={2} copyGlossaryTerm={copyGlossaryTerm}
            term={{
              source: 'Doctor',
              target: 'Arzt'
            }} />
        </tbody>
      </Table>
    ))
