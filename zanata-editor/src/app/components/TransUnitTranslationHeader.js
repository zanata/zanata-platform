import React, { PropTypes } from 'react'
import IconButton from './IconButton'
import TransUnitLocaleHeading from './TransUnitLocaleHeading'
import { hasTranslationChanged } from '../utils/phrase'

/**
 * Heading that displays locale name and ID
 */
const TransUnitTranslationHeader = React.createClass({

  propTypes: {
    phrase: PropTypes.object.isRequired,
    cancelEdit: PropTypes.func.isRequired,
    undoEdit: PropTypes.func.isRequired,
    translationLocale: PropTypes.shape({
      id: PropTypes.string.isRequired,
      name: PropTypes.string.isRequired
    }).isRequired
  },

  // TODO this is duplicated between source header and translation header,
  //      de-duplicate it
  buttonClass:
    'Link Link--neutral u-sizeHeight-1_1-2 u-sizeWidth-1 u-textCenter',

  closeButtonElement: function () {
    return (
      <li className="u-sm-hidden">
        <IconButton
          icon="cross"
          className={this.buttonClass}
          title="Cancel edit"
          onClick={this.props.cancelEdit} />
      </li>
    )
  },

  undoButtonElement: function () {
    return (
      <li>
        <IconButton
          icon="undo"
          className={this.buttonClass}
          title="Undo edit"
          onClick={this.props.undoEdit} />
      </li>
    )
  },

  render: function () {
    const displayUndo = hasTranslationChanged(this.props.phrase)
    const button = displayUndo
      ? this.undoButtonElement()
      : this.closeButtonElement()

    return (
      <div
        className="TransUnit-panelHeader TransUnit-panelHeader--translation">

        <TransUnitLocaleHeading
          {...this.props.translationLocale} />

        <ul className="u-floatRight u-listHorizontal">
          {button}
        </ul>
      </div>
    )
  }
})

export default TransUnitTranslationHeader
