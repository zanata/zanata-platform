jest.disableAutomock()

import React from 'react'
import ReactDOMServer from 'react-dom/server'
import DraggableVersionPanels from '.'
import {Button, ListGroup, ListGroupItem} from 'react-bootstrap'
import {Icon, LockIcon} from '../../components'

describe('DraggableVersionPanelsTest', () => {
  it('can render DraggableVersionPanels', () => {
    const clickFun = function (e) {}
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
        onDraggableMoveEnd={clickFun}
        removeVersion={clickFun} />
    )
    const expected = ReactDOMServer.renderToStaticMarkup(
      <ListGroup>
        <div>
          <span className="vmerge-adjtitle vmerge-title">
          Adjust priority of selected versions
          </span><br />
          <span className="text-muted vmerge-adjsub">(best first)</span>
          <div className="pre-scrollable">
            <ListGroupItem className='v' >
              <button type="button" className="btn-link-sort btn btn-link">
                <i className="fa fa-sort"></i>
              </button>
              ver1 <span className="text-muted"> meikai1
              </span> <LockIcon status={'ACTIVE'} />
              {" "}
              <Button bsSize='xsmall' className='close rm-version-btn'
                onClick={clickFun}>
                <Icon name='cross' className='n2 crossicon'
                  title='remove version' />
              </Button>
            </ListGroupItem>
            <ListGroupItem className='v' >
              <button type="button" className="btn-link-sort btn btn-link">
                <i className="fa fa-sort"></i>
              </button>
              ver2 <span className="text-muted"> meikai2
              </span> <LockIcon status={'ACTIVE'} />
              {" "}
              <Button bsSize='xsmall' className='close rm-version-btn'
                onClick={clickFun}>
                <Icon name='cross' className='n2 crossicon'
                  title='remove version' />
              </Button>
            </ListGroupItem>
          </div>
        </div>
      </ListGroup>
    )
    expect(actual).toEqual(expected)
  })
  it('returns an empty span if there are no selectedVersions', () => {
    const clickFun = function (e) {}
    const actual = ReactDOMServer.renderToStaticMarkup(
      <DraggableVersionPanels
        selectedVersions={[]}
        onDraggableMoveEnd={clickFun}
        removeVersion={clickFun} />
    )
    const expected = ReactDOMServer.renderToStaticMarkup(
      <span></span>
    )
    expect(actual).toEqual(expected)
  })
})
