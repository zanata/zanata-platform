/**
 * Modal to display the details for a group of suggestion matches.
 */

import React, { Component } from 'react'
import { Modal } from 'zanata-ui'
import { PanelGroup } from 'react-bootstrap'
import LocalProjectDetailPanel from './LocalProjectDetailPanel'
import ImportedTMDetailPanel from './ImportedTMDetailPanel'

const sampleProps = {
  source: 'The Source Text',
  target: 'The Translated Text'
}

const sampleData = [
  {
    'type': 'LOCAL_PROJECT',
    'textFlowId': 6,
    'contentState': 'Translated',
    'projectId': 'test',
    'projectName': 'Test Projacked',
    'version': '1',
    'documentName': 'document.txt',
    'documentPath': 'path/to/file/',
    'resId': '9cef980fa92eaf64699cf1a07a279313',
    'lastModifiedDate': '2016-11-10T00:51:21.000Z',
    'lastModifiedBy': 'admin'
  },
  {
    'type': 'LOCAL_PROJECT',
    'textFlowId': 1,
    'contentState': 'Approved',
    'projectId': 'test',
    'projectName': 'Test Projacked',
    'version': '1',
    'documentName': 'document.txt',
    'documentPath': '',
    'resId': '9cef980fa92eaf64699cf1a07a279313',
    'lastModifiedDate': '2016-11-23T23:55:19.000Z',
    'lastModifiedBy': 'admin'
  },
  {
    'type': 'LOCAL_PROJECT',
    'textFlowId': 7,
    'contentState': 'Translated',
    'projectId': 'test',
    'projectName': 'Test Projacked',
    'version': '1',
    'documentName': 'document.txt',
    'documentPath':
      'really/long/path/to/file/that/should/be/shortened/in/the/heading',
    'resId': '9cef980fa92eaf64699cf1a07a279313',
    'lastModifiedDate': '2016-11-23T23:55:19.000Z',
    'lastModifiedBy': 'admin',
    'sourceComment': 'This is a source comment right here.',
    'targetComment': 'This one is a target comment.'
  },
  {
    'type': 'IMPORTED_TM',
    'transMemoryUnitId': 1,
    'transMemorySlug': 'some-generic-tm',
    'transUnitId': 'test:1:document.txt:9cef980fa92eaf64699cf1a07a279313',
    'lastChanged': '2016-12-05T04:01:08.000Z'
  },
  {
    'type': 'IMPORTED_TM',
    'transMemoryUnitId': 2,
    'transMemorySlug': 'some-generic-tm',
    'transUnitId':
      'test:1:path/to/file/document.txt:9cef980fa92eaf64699cf1a07a279313',
    'lastChanged': '2016-12-05T04:01:08.000Z'
  }
]

class SuggestionDetailsModal extends Component {
  constructor () {
    super()
    this.state = {
      // FIXME make it false
      // FIXME use props instead
      show: true
    }
  }

  hideModal () {
    this.setState({show: false})
  }

  render () {
    // FIXME use real data
    const { source, target } = sampleProps
    const matchDetails = sampleData

    // FIXME better variable name
    const panels = matchDetails.map((matchDetail, index) => {
      const props = {
        source, target, matchDetail, key: index, eventKey: index
      }
      switch (matchDetail.type) {
        case 'LOCAL_PROJECT':
          return <LocalProjectDetailPanel {...props} />
        case 'IMPORTED_TM':
          return <ImportedTMDetailPanel {...props} />
        default:
          console.error('Unrecognised suggestion match type', matchDetail.type)
      }
    })

    return (
      <div>
        {/* FIXME use the "X more" match detail link to do this instead
                  or maybe just use a click on any of those detail items */}
        <Modal
          show={this.state.show}
          onHide={::this.hideModal}>
          <Modal.Header>
            <Modal.Title><small><span className="pull-left">
            Translation Memory Details</span></small></Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <ul className="list-inline">
              <li className="diff">{source}</li>
              <li>{target}{/* <Label bsStyle="sucess">Translated
              </Label>*/}</li>
            </ul>
            <PanelGroup defaultActiveKey={0} accordion>
              {panels}
            </PanelGroup>
          </Modal.Body>
          <Modal.Footer>
            {/*
            <p>Last modified on 30/04/16 13:15 by <a href="">sdickers</a></p>
            */}
          </Modal.Footer>
        </Modal>
      </div>)
  }
}

// TODO make this a connected component, it can pull out the right suggestion
//      detail based on an on/off state and the appropriate id.

export default SuggestionDetailsModal
