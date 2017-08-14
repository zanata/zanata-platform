/* global jest describe it expect */
jest.disableAutomock()

import React from 'react'
import ReactDOMServer from 'react-dom/server'
import DraggableVersionPanels, {Item, DragHandle, tooltipSort} from '.'
import {Button, ListGroup, ListGroupItem, OverlayTrigger} from 'react-bootstrap'
import {Icon, LockIcon} from '../../components'

const callback = function (e) {}

describe('DraggableVersionPanels', () => {
  it('can render a draggable Item', () => {
    const version = {
      projectSlug: 'meikai1',
      version: {
        id: 'ver1',
        status: 'ACTIVE'
      }
    }
    const actual = ReactDOMServer.renderToStaticMarkup(
      <Item key={'meikai1:ver1'} index={0}
        value={version} removeVersion={callback} />
    )
    const expected = ReactDOMServer.renderToStaticMarkup(
      <ListGroupItem className='v' >
        <DragHandle />
        {'ver1'} <span className='text-muted'> {'meikai1'}
        </span> <LockIcon status={'ACTIVE'} />
        {" "}
        <Button bsSize='xsmall' className='close rm-version-btn'
          onClick={callback}>
          <Icon name='cross' className='n2 crossicon' title='remove version' />
        </Button>
      </ListGroupItem>
    )
    expect(actual).toEqual(expected)
  })
  it('can render DraggableVersionPanels', () => {
    const someVersions = [{
      projectSlug: 'meikai1',
      version: {
        id: 'ver1',
        status: 'ACTIVE'
      }
    },
      {
        projectSlug: 'meikai2',
        version: {
          id: 'ver2',
          status: 'ACTIVE'
        }
      }]
    const actual = ReactDOMServer.renderToStaticMarkup(
      <DraggableVersionPanels
        selectedVersions={someVersions}
        onDraggableMoveEnd={callback}
        removeVersion={callback} />
    )
    const expected = ReactDOMServer.renderToStaticMarkup(
      <ListGroup>
        <div>
          <span className="vmerge-adjtitle vmerge-title">
          Adjust priority of selected versions
          </span><br />
          <span className="text-muted vmerge-adjsub">(best first)</span>
          <OverlayTrigger placement='top' overlay={tooltipSort}>
            <Icon name='info' className='s0 info-icon' />
          </OverlayTrigger>
          <Item key={'meikai1:ver1'} index={0}
            value={someVersions[0]} removeVersion={callback} />
          <Item key={'meikai2:ver2'} index={1}
            value={someVersions[1]} removeVersion={callback} />
        </div>
      </ListGroup>
    )
    expect(actual).toEqual(expected)
  })
  it('returns an empty span if there are no selectedVersions', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <DraggableVersionPanels
        selectedVersions={[]}
        onDraggableMoveEnd={callback}
        removeVersion={callback} />
    )
    expect(actual).toEqual('')
  })
})
