jest.disableAutomock()

import React from 'react'
import ReactDOMServer from 'react-dom/server'
import TestUtils from 'react-addons-test-utils'
import Pager from '../../app/editor/components/Pager'
import { Icon } from 'zanata-ui'
import mockGettextCatalog from '../mock/mockAngularGettext'

describe('PagerTest', () => {
  it('Pager markup', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(<Pager
      actions={{
        firstPage: () => {},
        previousPage: () => {},
        nextPage: () => {},
        lastPage: () => {}
      }}
      pageNumber={7}
      pageCount={11}
      gettextCatalog={mockGettextCatalog}/>)

    const expected = ReactDOMServer.renderToStaticMarkup(
      <ul className='u-listHorizontal u-textCenter'>
        <li>
          <a className='Link--neutral u-sizeHeight-1_1-2 u-textNoSelect'
             title='First page'>
            <Icon name='previous' title='First page' size="2" />
          </a>
        </li>
        <li>
          <a className='Link--neutral u-sizeHeight-1_1-2 u-textNoSelect'
             title='Previous page'>
            <Icon name='chevron-left' title='Previous page' size="2" />
          </a>
        </li>
        <li className='u-sizeHeight-1 u-sPH-1-4'>
          <span className='u-textNeutral'>
            7 of 11
          </span>
        </li>
        <li>
          <a className='Link--neutral u-sizeHeight-1_1-2 u-textNoSelect'
             title='Next page'>
            <Icon name='chevron-right' title='Next page' size="2" />
          </a>
        </li>
        <li>
          <a className='Link--neutral u-sizeHeight-1_1-2 u-textNoSelect'
             title='Last page'>
            <Icon name='next' title='Last page' size="2" />
          </a>
        </li>
      </ul>
    )
    expect(actual).toEqual(expected)
  })

  it('Pager events', () => {
    const blank = 'no event'
    let event

    const d20 = TestUtils.renderIntoDocument(
      <Pager
        actions={{
          firstPage: () => event = 'critical fumble',
          previousPage: () => event = 'fumble',
          nextPage: () => event = 'success',
          lastPage: () => event = 'critical hit'
        }}
        pageNumber={2}
        pageCount={20}
        gettextCatalog={mockGettextCatalog}/>
    )
    // click events are expected on the <a> tags
    const [ first, prev, next, last ] =
      TestUtils.scryRenderedDOMComponentsWithTag(d20, 'a')

    event = blank
    TestUtils.Simulate.click(first)
    expect(event).toEqual('critical fumble',
      'first-page button should trigger given event')

    event = blank
    TestUtils.Simulate.click(prev)
    expect(event).toEqual('fumble',
      'previous-page button should trigger given event')

    event = blank
    TestUtils.Simulate.click(next)
    expect(event).toEqual('success',
      'next-page button should trigger given event')

    event = blank
    TestUtils.Simulate.click(last)
    expect(event).toEqual('critical hit',
      'last-page button should trigger given event')
  })
})
