/* global jest describe expect it */

import React from 'react'
import * as ReactDOMServer from 'react-dom/server'
import Adapter from 'enzyme-adapter-react-16'
import Enzyme, { mount } from 'enzyme'
import Pager from '.'
import { Icon } from '../../../components'
import { IntlProvider } from 'react-intl'

Enzyme.configure({ adapter: new Adapter() });

// tslint:disable-next-line:no-empty
const callback = () => {}

describe('PagerTest', () => {
  it('Pager markup', () => {
    // Construct a new `IntlProvider` instance by passing `props` and
    // `context` as React would, then call `getChildContext()` to get the
    // React Intl API, complete with the `format*()` functions.
    // see: https://github.com/yahoo/react-intl/wiki/Testing-with-React-Intl#relativedate-advanced-uses-injectintl

    const actual = ReactDOMServer.renderToStaticMarkup(
        <IntlProvider locale={'en'}>
          <Pager
            intl={undefined}
            firstPage={callback}
            previousPage={callback}
            nextPage={callback}
            lastPage={callback}
            pageNumber={7}
            pageCount={11}
            />
        </IntlProvider>)

    const expected = ReactDOMServer.renderToStaticMarkup(
      <ul className='u-listHorizontal tc'>
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
          <span className='txt-neutral'>
            <option>7 of 11</option>
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
      <IntlProvider locale={'en'}>
        <Pager
          intl={undefined}
          firstPage={goFirst}
          previousPage={goPrev}
          nextPage={goNext}
          lastPage={goLast}
          pageNumber={2}
          pageCount={20}/>
      </IntlProvider>
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
