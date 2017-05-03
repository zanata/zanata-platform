jest.disableAutomock()

import React from 'react'
import ReactDOMServer from 'react-dom/server'
import SuggestionContents from '../../app/editor/components/SuggestionContents'
import TextDiff from '../../app/editor/components/TextDiff'

describe('SuggestionContentTest', () => {
  it('SuggestionContent markup (singular)', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <SuggestionContents
        plural={false}
        contents={['As thick as you are, pay attention!']}/>
    )

    const expected = ReactDOMServer.renderToStaticMarkup(
      <div>
        <div className="TransUnit-item">
          <div className="TransUnit-itemHeader" />
          <div className="TransUnit-text TransUnit-text--tight">
            As thick as you are, pay attention!
          </div>
        </div>
      </div>
    )
    expect(actual).toEqual(expected)
  })

  it('SuggestionContent markup (plural)', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <SuggestionContents
        plural={true}
        contents={[
          'As thick as you are, pay attention!',
          'Even you can be caught unawares'
        ]} />
    )

    const expected = ReactDOMServer.renderToStaticMarkup(
      <div>
        <div className="TransUnit-item">
          <div className="TransUnit-itemHeader">
            <span className="u-textMeta">Singular Form</span>
          </div>
          <div className="TransUnit-text">
            As thick as you are, pay attention!
          </div>
        </div>
        <div className="TransUnit-item">
          <div className="TransUnit-itemHeader">
            <span className="u-textMeta">Plural Form</span>
          </div>
          <div className="TransUnit-text">
            Even you can be caught unawares
          </div>
        </div>
      </div>
    )
    expect(actual).toEqual(expected)
  })

  it('SuggestionContent markup (with diff)', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <SuggestionContents
        plural={false}
        contents={['As thick as you are, pay attention!']}
        compareTo={['As slick as you are, play attrition?']}/>
    )

    const expected = ReactDOMServer.renderToStaticMarkup(
      <div>
        <div className="TransUnit-item">
          <div className="TransUnit-itemHeader" />
          <TextDiff
            className="TransUnit-text TransUnit-text--tight Difference"
            text1="As slick as you are, play attrition?"
            text2="As thick as you are, pay attention!"/>
        </div>
      </div>
    )
    expect(actual).toEqual(expected)
  })

  it('SuggestionContent markup (plural with diff)', () => {
    const actual = ReactDOMServer.renderToStaticMarkup(
      <SuggestionContents
        plural={true}
        contents={[
          'As thick as you are, pay attention!',
          'Even you can be caught unawares'
        ]}
        compareTo={['As slick as you are, play attrition?']}/>
    )

    const expected = ReactDOMServer.renderToStaticMarkup(
      <div>
        <div className="TransUnit-item">
          <div className="TransUnit-itemHeader">
            <span className="u-textMeta">Singular Form</span>
          </div>
          <TextDiff
            className="TransUnit-text Difference"
            text1="As slick as you are, play attrition?"
            text2="As thick as you are, pay attention!"/>
        </div>
        <div className="TransUnit-item">
          <div className="TransUnit-itemHeader">
            <span className="u-textMeta">Plural Form</span>
          </div>
          <TextDiff
            className="TransUnit-text Difference"
            text1=""
            text2="Even you can be caught unawares"/>
        </div>
      </div>
    )
    expect(actual).toEqual(expected)
  })
})
