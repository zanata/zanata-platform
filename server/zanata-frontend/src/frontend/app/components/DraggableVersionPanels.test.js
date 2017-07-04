jest.disableAutomock()

import React from 'react'
import ReactDOMServer from 'react-dom/server'
import DraggableVersionPanels from './DraggableVersionPanels'

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
    const svgIcon = `<use xlink:href="#Icon-cross" />`
    const expected = ReactDOMServer.renderToStaticMarkup(
      <div className="list-group">
        <div>
          <span className="vmerge-adjtitle vmerge-title">
          Adjust priority of selected versions
          </span><br />
          <span className="text-muted vmerge-adjsub">(best first)</span>
          <div className="pre-scrollable"><span className="v list-group-item">
            <button type="button" className="btn-link-sort btn btn-link">
              <i className="fa fa-sort"></i>
            </button>ver1 <span className="text-muted"> meikai1
            </span> <span></span> <button type="button"
              className="close rm-version-btn btn btn-xs btn-default">
              <span className="n2 crossicon" title="remove version">
                <svg dangerouslySetInnerHTML={{ __html: svgIcon }}
                  style={{ fill: 'currentColor' }} />
              </span>
            </button></span><span className="v list-group-item">
              <button type="button" className="btn-link-sort btn btn-link">
                <i className="fa fa-sort"></i>
              </button>ver2 <span className="text-muted"> meikai2
              </span> <span></span> <button type="button"
                className="close rm-version-btn btn btn-xs btn-default">
                <span className="n2 crossicon" title="remove version">
                  <svg dangerouslySetInnerHTML={{ __html: svgIcon }}
                    style={{ fill: 'currentColor' }} />
                </span>
              </button>
            </span>
          </div>
        </div>
      </div>
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
