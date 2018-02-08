/* global jest describe expect it */

import React from 'react'
import * as ReactDOMServer from 'react-dom/server'
import * as TestUtils from 'react-dom/test-utils'
import { mount } from 'enzyme'
import { Pager } from '.'
import { Icon } from '../../../components'
import mockGettextCatalog from '../../../../__mocks__/mockAngularGettext'

const callback = () => {}

describe('PagerTest', () => {
  it('Pager markup', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(<Pager
      firstPage={callback}
      previousPage={callback}
      nextPage={callback}
      lastPage={callback}
      pageNumber={7}
      pageCount={11}
      gettextCatalog={mockGettextCatalog} />)

    const expected = ReactDOMServer.renderToStaticMarkup(
      <ul className='u-listHorizontal u-textCenter'>
        <li>
          <a className='Link--neutral u-sizeHeight-1_1-2 u-textNoSelect'
            title='First page'>
            <Icon name='previous' title='First page' className="s2" />
          </a>
        </li>
        <li>
          <a className='Link--neutral u-sizeHeight-1_1-2 u-textNoSelect'
            title='Previous page'>
            <Icon name='chevron-left' title='Previous page' className="s2" />
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
            <Icon name='chevron-right' title='Next page' className="s2" />
          </a>
        </li>
        <li>
          <a className='Link--neutral u-sizeHeight-1_1-2 u-textNoSelect'
            title='Last page'>
            <Icon name='next' title='Last page' className="s2" />
          </a>
        </li>
      </ul>
    )
    expect(actual).toEqual(expected)
  })

  it('Pager events', () => {
    const goFirst = jest.fn()
    const goPrev = jest.fn()
    const goNext = jest.fn()
    const goLast = jest.fn()

    const d20 = mount(
      <Pager
        firstPage={goFirst}
        previousPage={goPrev}
        nextPage={goNext}
        lastPage={goLast}
        pageNumber={2}
        pageCount={20}
        gettextCatalog={mockGettextCatalog} />
    )

    // click events are expected on the <a> tags
    d20.find('a').at(0).simulate('click')
    expect(goFirst).toHaveBeenCalled()

    expect(goPrev).not.toHaveBeenCalled()
    d20.find('a').at(1).simulate('click')
    expect(goPrev).toHaveBeenCalled()

    expect(goNext).not.toHaveBeenCalled()
    d20.find('a').at(2).simulate('click')
    expect(goNext).toHaveBeenCalled()

    expect(goLast).not.toHaveBeenCalled()
    d20.find('a').at(3).simulate('click')
    expect(goLast).toHaveBeenCalled()
  })
})
