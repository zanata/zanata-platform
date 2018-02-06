import React from 'react'
import { storiesOf } from '@storybook/react'
import { EditableText } from '../'
import { Well, Table } from 'react-bootstrap'
import * as PropTypes from "prop-types";

storiesOf('EditableText', module)
    .add('editing', () => (
        <span>
          <h2><img src="https://upload.wikimedia.org/wikipedia/commons/4/49/Zanata-Logo.svg" width="42px" /> EditableText</h2>
          <Well bsSize="large">Used on Glossary page and modals for editable glossary terms.</Well>
          <EditableText
            className='editable textInput textState'
            maxLength={255}
            editable={true}
            editing={true}
            placeholder='Add a description…'
            emptyReadOnlyText='No description'>
          Test text
        </EditableText>
          <hr />
            <h3>Props</h3>
          <Table striped bordered condensed hover>
            <thead>
            <tr>
              <th>Name</th>
              <th>Type</th>
              <th>Default</th>
              <th>Description</th>
            </tr>
            </thead>
            <tbody>
            <tr>
              <td>children</td>
              <td>string</td>
              <td></td>
              <td>String value for this text field</td>
            </tr>
            <tr>
              <td>editable</td>
              <td>boolean</td>
              <td>false</td>
              <td>Toggle whether the text field is in editable or not.
              </td>
            </tr>
            <tr>
              <td>editing</td>
              <td>boolean</td>
              <td>false</td>
              <td>Toggle whether the text field is in editing mode or not.</td>
            </tr>
            <tr>
              <td>placeholder</td>
              <td>string</td>
              <td></td>
              <td>
                Field placeholder text
              </td>
            </tr>
            <tr>
              <td>emptyReadOnlyText</td>
              <td>string</td>
              <td></td>
              <td>String to display if it is editable and children is empty and there is not placeholder
              </td>
            </tr>
            <tr>
              <td>title</td>
              <td>string</td>
              <td></td>
              <td>Field tooltip
              </td>
            </tr>
            </tbody>
          </Table>
        </span>
    ))
    .add('not editing', () => (
        <span>
        <h2><img src="https://upload.wikimedia.org/wikipedia/commons/4/49/Zanata-Logo.svg" width="42px" /> EditableText - disabled</h2>
        <EditableText
            className='editable textInput textState'
            maxLength={255}
            editable={true}
            editing={false}
            placeholder='Add a description…'
            emptyReadOnlyText='No description'>
          Test text
        </EditableText>
        </span>
    ))
