import DashboardLink from '../components/DashboardLink'
import DocsDropdown from '../components/DocsDropdown'
import { Icon, Row } from 'zanata-ui'
import LanguagesDropdown from '../components/LanguagesDropdown'
import ProjectVersionLink from '../components/ProjectVersionLink'
/* Disabled UI locale changes until zanata-spa is internationalised
import UiLanguageDropdown from '../components/UiLanguageDropdown'
*/
import React, { PropTypes } from 'react'
import { connect } from 'react-redux'
import { toggleDropdown } from '../actions'
import { changeUiLocale } from '../actions/headerActions'

const { any, arrayOf, func, object, shape, string } = PropTypes

/**
 * Hideable navigation header across the top of the app.
 */
const NavHeader = React.createClass({

  propTypes: {
    actions: shape({
      changeUiLocale: func.isRequired,
      toggleDropdown: func.isRequired
    }).isRequired,

    data: shape({
      user: shape({
        name: string,
        gravatarUrl: string,
        dashboardUrl: string.isRequired
      }),
      context: shape({
        projectVersion: shape({
          project: shape({
            slug: string.isRequired,
            name: string
          }).isRequired,
          version: string.isRequired,
          url: string,
          docs: arrayOf(string).isRequired,
          locales: object.isRequired
        }).isRequired,
        selectedLocale: string.isRequired
      }).isRequired
    }).isRequired,

    dropdown: shape({
      openDropdownKey: any,
      docsKey: any.isRequired,
      localeKey: any.isRequired,
      uiLocaleKey: any.isRequired
    }).isRequired,

    ui: shape({
      // locale id for selected locale
      selectedUiLocale: string,
      // localeId -> { id, name }
      uiLocales: object.isRequired
    }).isRequired
  },

  render: function () {
    const props = this.props
    const ctx = props.data.context
    const dropdowns = props.dropdown

    const docsDropdownProps = {
      context: ctx,
      isOpen: dropdowns.openDropdownKey === dropdowns.docsKey,
      toggleDropdown: props.actions.toggleDropdown(dropdowns.docsKey)
    }

    const langsDropdownProps = {
      context: ctx,
      isOpen: dropdowns.openDropdownKey === dropdowns.localeKey,
      toggleDropdown: props.actions.toggleDropdown(dropdowns.localeKey)
    }

    /* Disabled UI locale changes until zanata-spa is internationalised
    const uiLangDropdownProps = {
      changeUiLocale: props.actions.changeUiLocale,
      selectedUiLocale: props.ui.selectedUiLocale,
      uiLocales: props.ui.uiLocales,
      isOpen: dropdowns.openDropdownKey === dropdowns.uiLocaleKey,
      toggleDropdown: props.actions.toggleDropdown(dropdowns.uiLocaleKey)
    }*/

    return (
      <nav role="navigation"
        className="Editor-mainNav u-posRelative u-textCenter">
        <div className="u-posAbsoluteLeft">
          <Row>
            <ProjectVersionLink {...ctx.projectVersion} />
            <div className="u-inlineBlock u-sMH-1-4 u-textInvert
                          u-textMuted u-sm-hidden">
              <Icon name="chevron-right" size="1" />
            </div>
            <span className="Editor-docsDropdown">
              <DocsDropdown {...docsDropdownProps} />
            </span>
            <span className="u-sMH-1-4"></span>
            <LanguagesDropdown {...langsDropdownProps} />
          </Row>
        </div>

        <ul className="u-listHorizontal u-posAbsoluteRight u-sMR-1-2">
          {/* Disabled UI locale changes until zanata-spa is internationalised
            <li>
            <UiLanguageDropdown {...uiLangDropdownProps}/>
          </li>*/
          }
          {/* A couple of items from the Angular template that were not used
          <li ng-show="appCtrl.PRODUCTION">
            <button class="Link--invert Header-item u-sizeWidth-1_1-2"
              title="{{'More'|translate}}"><icon name="ellipsis"/><span
              class="u-hiddenVisually" translate>More</span></button>
          </li>
          <li ng-show="appCtrl.PRODUCTION">
            <button class="Link--invert Header-item u-sizeWidth-1_1-2"
              title="{{'Notifications'|translate}}">
              <icon name="notification" title="{{'Notifications'|translate}}"/>
            </button>
          </li>
          */}
          <li>
            <DashboardLink {...this.props.data.user} />
          </li>
        </ul>
      </nav>
    )
  }
})

function mapStateToProps (state) {
  const { dropdown, headerData, ui } = state
  return {
    data: headerData,
    dropdown,
    ui
  }
}

function mapDispatchToProps (dispatch) {
  return {
    actions: {
      toggleDropdown: (key) => {
        return () => dispatch(toggleDropdown(key))
      },
      changeUiLocale: (key) => {
        return () => dispatch(changeUiLocale(key))
      }
    }
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(NavHeader)
