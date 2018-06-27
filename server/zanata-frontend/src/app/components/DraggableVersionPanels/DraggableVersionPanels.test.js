/* global jest describe it expect */

import React from 'react'
import * as ReactDOMServer from 'react-dom/server'
import DraggableVersionPanels, {Item, DragHandle, tooltipSort} from '.'
import {LockIcon, Icon} from '../../components'
import Button from 'antd/lib/button'
import Tooltip from 'antd/lib/tooltip'
import Layout from 'antd/lib/layout'

// @ts-ignore any
const callback = function (_e) {}

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
      // @ts-ignore
      <Item key={'meikai1:ver1'} index={0}
        value={version} removeVersion={callback} />
    )
    const expected = ReactDOMServer.renderToStaticMarkup(
      <li className="v list-group-item" >
        <DragHandle />
        <span className='ml2'>
          {'ver1'}
        </span>
        <span className='txt-muted ml1'>
          {'meikai1'}
        </span> <LockIcon status={'ACTIVE'} />
        {" "}
        <Button
          className='close btn-xs'
          aria-label='button'
          onClick={callback} icon='close' />
      </li>
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
        // @ts-ignore
        selectedVersions={someVersions}
        onDraggableMoveEnd={callback}
        removeVersion={callback} />
    )
    const expected = ReactDOMServer.renderToStaticMarkup(
      <span>
        <div>
        <Layout className="d-inh">
        Adjust priority of selected versions
        <br />
        <span className="txt-muted">(best first)</span>
        <Tooltip placement='top' title={tooltipSort}>
          <a className="btn-xs btn-link">
            <Icon name="info" className="s0" />
          </a>
        </Tooltip>
        <Item
          dispatch={callback}
          key={'meikai1:ver1'}
          // @ts-ignore
          index={0}
          value={someVersions[0]} removeVersion={callback} />
        <Item
          dispatch={callback}
          key={'meikai2:ver2'}
          // @ts-ignore
          index={1}
          value={someVersions[1]} removeVersion={callback} />
        </Layout>
      </div>
      </span>
    )
    expect(actual).toEqual(expected)
  })
  it('returns an descriptive span if there are no selectedVersions', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <DraggableVersionPanels
        selectedVersions={[]}
        onDraggableMoveEnd={callback}
        removeVersion={callback} />
    )
    const expected = ReactDOMServer.renderToStaticMarkup(
      <p className="no-v tc txt-muted">
        Please select versions to sort<br />
        <Icon name="version" className="s8" />
      </p>
    )
    expect(actual).toEqual(expected)
  })
})
