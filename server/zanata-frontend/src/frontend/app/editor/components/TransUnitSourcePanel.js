import React from 'react'
import PropTypes from 'prop-types'
import TransUnitSourceHeader from './TransUnitSourceHeader'
import { LoaderText } from '../../components'
import IconButton from './IconButton'

/**
 * Panel for the source of the selected phrase
 */
class TransUnitSourcePanel extends React.Component {
  static propTypes = {
    phrase: PropTypes.object.isRequired,
    selected: PropTypes.bool.isRequired,
    cancelEdit: PropTypes.func.isRequired,
    copyFromSource: PropTypes.func.isRequired,
    sourceLocale: PropTypes.shape({
      id: PropTypes.string.isRequired,
      name: PropTypes.string.isRequired
    }).isRequired
  }

  render () {
    const isPlural = this.props.phrase.plural

    const header = this.props.selected
      ? <TransUnitSourceHeader {...this.props} />
      : undefined

    const isLoading = !this.props.phrase.sources

    const sources = isLoading
      ? <span className="u-textMeta">
        <LoaderText loading />
      </span>
      : this.props.phrase.sources.map(
        (source, index) => {
          // TODO make this translatable
          const headerLabel = index === 0
            ? 'Singular Form'
            : 'Plural Form'

          const copySource = this.props.copyFromSource.bind(undefined, index)

          const copyButton = this.props.selected
          /* eslint-disable max-len */
            ? <ul className="u-floatRight u-listHorizontal">
              <li>
                <IconButton
                  icon="copy"
                  title={'Copy ' + this.props.sourceLocale.name +
                    ' (' + this.props.sourceLocale.id + ')'}
                  onClick={copySource}
                  className="u-floatRight Link Link--neutral u-sizeHeight-1 u-sizeWidth-1 u-textCenter" />
              </li>
            </ul>
            /* eslint-enable max-len */
            : undefined

          const itemHeader = isPlural
            ? <div className="TransUnit-itemHeader">
              <span className="u-textMeta">
                {headerLabel}
              </span>
              {copyButton}
            </div>
            : undefined

          return (
            <div className="TransUnit-item" key={index}>
              {itemHeader}
              <pre className="TransUnit-text">{source}</pre>
            </div>
          )
        })

    // empty, but this is what is output in the Angular version
    const footer = this.props.selected
      ? (
      /* eslint-disable max-len */
      <div className="TransUnit-panelFooter TransUnit-panelFooter--source u-sm-hidden">
        <div className="u-sizeHeight-1_1-2">
          {/*
          <button ng-show="appCtrl.PRODUCTION"
            class="Link Link--neutral u-sizeHeight-1_1-2"
            title="{{::'Details'|translate}}">
            <icon name="info"
              title="{{::'Details'|translate}}"
              class="u-sizeWidth-1_1-2"></icon>
          </button>
          */}
        </div>
      </div>
      /* eslint-enable max-len */
      )
      : undefined

    return (
      <div className="TransUnit-panel TransUnit-source">
        {header}
        {sources}
        {footer}
      </div>
    )
  }
}

export default TransUnitSourcePanel
