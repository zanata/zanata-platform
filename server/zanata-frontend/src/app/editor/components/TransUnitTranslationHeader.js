import React from 'react'
import * as PropTypes from 'prop-types'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import TransUnitLocaleHeading from './TransUnitLocaleHeading'
import { hasTranslationChanged } from '../utils/phrase-util'

/**
 * Heading that displays locale name and ID
 */
class TransUnitTranslationHeader extends React.Component {
  static propTypes = {
    phrase: PropTypes.object.isRequired,
    cancelEdit: PropTypes.func.isRequired,
    undoEdit: PropTypes.func.isRequired,
    translationLocale: PropTypes.shape({
      id: PropTypes.string.isRequired,
      name: PropTypes.string.isRequired
    }).isRequired
  }

  // TODO this is duplicated between source header and translation header,
  //      de-duplicate it
  buttonClass =
    'Link Link--neutral u-sizeHeight-1_1-2 u-sizeWidth-1 u-textCenter'

  closeButtonElement = () => {
    return (
      <li className="u-sm-hidden">
        <Button size="large"
          icon="close"
          className={this.buttonClass}
          title="Cancel edit"
          onClick={this.props.cancelEdit} />
      </li>
    )
  }

  undoButtonElement = () => {
    return (
      <li>
        <Button size="large"
          icon="rollback"
          className={this.buttonClass}
          title="Undo edit"
          onClick={this.props.undoEdit} />
      </li>
    )
  }

  render () {
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
}

export default TransUnitTranslationHeader
