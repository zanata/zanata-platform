import cx from 'classnames'
import Icon from '../Icon'
import IconButton from '../IconButton'
import React, { PropTypes } from 'react'

/**
 * Styled text input that displays result count.
 */
const SuggestionSearchInput = React.createClass({

  propTypes: {
    text: PropTypes.string.isRequired,
    onTextChange: PropTypes.func.isRequired,
    loading: PropTypes.bool.isRequired,
    resultCount: PropTypes.number,
    hasSearch: PropTypes.bool.isRequired,
    clearSearch: PropTypes.func.isRequired
  },

  clearSearch: function () {
    this.props.clearSearch()
    this.focusInput()
  },

  getDefaultProps: () => {
    return {
      focused: false
    }
  },

  getInitialState: () => {
    return {
      // FIXME one other component is interested in this state
      //       just deal with that when I get to it
      focused: false
    }
  },

  onFocus: function () {
    this.setState({
      focused: true
    })
  },

  onBlur: function () {
    this.setState({
      focused: false
    })
  },

  focusInput: function () {
    // TODO different approach for React 0.14

    // may not need to actually set focused=true, mainly using for
    // callback, which gets around issues with the component not being
    // properly in the DOM yet
    this.setState({
      focused: true
    }, () => {
      this.refs.input.focus()
    })
  },

  componentDidMount: function () {
    this.focusInput()
  },

  loadingResultsElement: function () {
    return (
      <span onClick={this.focusInput}
        className="Editor-suggestionsSearchLoader">
        {/* TODO proper loader */}
        Loading…
      </span>
    )
  },

  resultCountElement: function () {
    return (
      <span onClick={this.focusInput}
        className="Editor-suggestionsSearchResults">
        {this.props.resultCount} results
      </span>
    )
  },

  resultsElement: function () {
    if (!this.props.loading && !this.props.hasSearch) {
      return undefined
    }

    const innerElement = this.props.loading
      ? this.loadingResultsElement()
      : this.resultCountElement()

    return (
      <span className="InputGroup-addon">
        {innerElement}
      </span>
    )
  },

  clearButtonElement: function () {
    if (!this.props.hasSearch) {
      return undefined
    }
    // FIXME need to not use Icon--sm style for this one
    //       maybe use a size property with default of small
    return (
      <span className="InputGroup-addon">
        <IconButton icon="cross"
          title="Clear search"
          iconClass="Icon--xsm"
          onClick={this.clearSearch} />
      </span>
    )
  },

  render: function () {
    return (
      <div className={cx('InputGroup InputGroup--outlined InputGroup--rounded',
                         { 'is-focused': this.state.focused })}>
        <span className="InputGroup-addon"
          onClick={this.focusInput}>
          <Icon name="search"
            title="Search suggestions"
            className="Icon--xsm" />
        </span>
        <input ref="input"
          type="search"
          placeholder="Search suggestions…"
          maxLength="1000"
          value={this.props.text}
          onChange={this.props.onTextChange}
          className="InputGroup-input u-sizeLineHeight-1_1-4" />
        {this.resultsElement()}
        {this.clearButtonElement()}
      </div>
    )
  }
})

export default SuggestionSearchInput
