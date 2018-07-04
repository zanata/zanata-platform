import React from 'react'
import * as PropTypes from 'prop-types'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import TransUnitLocaleHeading from './TransUnitLocaleHeading'
import { hasTranslationChanged } from '../utils/phrase-util'
import Tooltip from 'antd/lib/tooltip'

/**
 * Header for the source of the selected phrase
 */
class TransUnitSourceHeader extends React.Component {
  static propTypes = {
    phrase: PropTypes.object.isRequired,
    cancelEdit: PropTypes.func.isRequired,
    copyFromSource: PropTypes.func.isRequired,
    sourceLocale: PropTypes.shape({
      id: PropTypes.string.isRequired,
      name: PropTypes.string.isRequired
    }).isRequired
  }

  componentWillMount () {
    this.copyFromSource = this.props.copyFromSource.bind(undefined, 0)
  }

  render () {
    // TODO remove duplication of this between source and translation headers
    const buttonClass =
      'btn-link Link Link--neutral u-sizeHeight-1_1-2 u-sizeWidth-1' +
        ' tc'

    const copyButtonItem = this.props.phrase.plural
      ? undefined
      : (
      <li>
        <Tooltip title={'Copy ' + this.props.sourceLocale.name +
              ' (' + this.props.sourceLocale.id + ')'}>
          <Button size="large" icon="copy"
            onClick={this.copyFromSource}
            className={buttonClass} />
        </Tooltip>
      </li>)

    const closeButtonItem = hasTranslationChanged(this.props.phrase)
      ? undefined
      : (
      <li className="u-gtemd-hidden">
        <Tooltip title="Cancel edit">
          <Button
            icon="close"
            onClick={this.props.cancelEdit}
            className={buttonClass} />
        </Tooltip>
      </li>
      )

    return (
      <div className="TransUnit-panelHeader TransUnit-panelHeader--source">
        <TransUnitLocaleHeading {...this.props.sourceLocale} />
        <ul className="fr u-listHorizontal">
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
}

export default TransUnitSourceHeader
