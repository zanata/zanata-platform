import React, { PropTypes } from 'react'
import IconButton from './IconButton'
import TransUnitLocaleHeading from './TransUnitLocaleHeading'
import { hasTranslationChanged } from '../utils/phrase'

/**
 * Header for the source of the selected phrase
 */
const TransUnitSourceHeader = React.createClass({

  propTypes: {
    phrase: PropTypes.object.isRequired,
    cancelEdit: PropTypes.func.isRequired,
    copyFromSource: PropTypes.func.isRequired,
    sourceLocale: PropTypes.shape({
      id: PropTypes.string.isRequired,
      name: PropTypes.string.isRequired
    }).isRequired
  },

  componentWillMount: function () {
    this.copyFromSource = this.props.copyFromSource.bind(undefined, 0)
  },

  render: function () {
    // TODO remove duplication of this between source and translation headers
    const buttonClass =
      'Link Link--neutral u-sizeHeight-1_1-2 u-sizeWidth-1 u-textCenter'

    const copyButtonItem = this.props.phrase.plural
      ? undefined
      : (
      <li>
        <IconButton icon="copy"
          title={'Copy ' + this.props.sourceLocale.name +
                     ' (' + this.props.sourceLocale.id + ')'}
          onClick={this.copyFromSource}
          className={buttonClass} />
      </li>)

    const closeButtonItem = hasTranslationChanged(this.props.phrase)
      ? undefined
      : (
      <li className="u-gtemd-hidden">
        <IconButton
          icon="cross"
          title="Cancel edit"
          onClick={this.props.cancelEdit}
          className={buttonClass} />
      </li>
      )

    return (
      <div className="TransUnit-panelHeader TransUnit-panelHeader--source">
        <TransUnitLocaleHeading {...this.props.sourceLocale} />
        <ul className="u-floatRight u-listHorizontal">
          {/* <li ng-show="appCtrl.PRODUCTION">
            <button class="Link Link--neutral u-sizeHeight-1_1-2
                           u-sizeWidth-1 u-textCenter"
              title="{{::'Do not translate'|translate}}">
              <icon name="block"
                class="Icon--sm"
                title="{{::'Do not translate'|translate}}"></icon>
          </li> */}
          {copyButtonItem}
          {closeButtonItem}
        </ul>
      </div>
    )
  }
})

export default TransUnitSourceHeader
