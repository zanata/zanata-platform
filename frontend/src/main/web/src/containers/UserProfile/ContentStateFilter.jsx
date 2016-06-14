import React from 'react'
import Actions from '../../actions/userMatrix'
import {
  ContentStates,
  ContentStateStyles
} from '../../constants/Options'
import {
  ButtonLink,
  Base
} from '../../components'
import PureRenderMixin from 'react-addons-pure-render-mixin'

const classes = {
  root: {
    m: 'Mb(rh)'
  },
  button: {
    base: {
      fz: 'Fz(msn1) Fz(ms0)--lg',
      bdrs: 'Bdrs(rnd)',
      m: 'Mend(eq) Mend(eh)--lg',
      p: 'Px(eh) Px(e3q)--lg Py(eq)'
    },
    states: {
      active: {
        c: 'C(#fff)',
        hover: {
          filter: ''
        },
        focus: {
          filter: ''
        },
        active: {
          filter: ''
        }
      }
    },
    types: {
      plain: {
        states: {
          active: {
            bgc: 'Bgc(dark)'
          }
        }
      },
      primary: {
        states: {
          active: {
            bgc: 'Bgc(pri)'
          }
        }
      },
      success: {
        states: {
          active: {
            bgc: 'Bgc(success)'
          }
        }
      },
      unsure: {
        states: {
          active: {
            bgc: 'Bgc(unsure)'
          }
        }
      }
    }
  }
}
/**
 * Component to filter statistics on content state
 * (approved, translated, need work)
 */
var ContentStateFilter = React.createClass({
  mixins: [PureRenderMixin],
  propTypes: {
    selectedContentState: React.PropTypes.oneOf(ContentStates).isRequired
  },
  onFilterOptionClicked: function (option, event) {
    if (this.props.selectedContentState !== option) {
      Actions.changeContentState(option)
    }
  },
  render: function () {
    const contentStateFilter = this
    const selected = this.props.selectedContentState
    const clickHandler = this.onFilterOptionClicked
    const optionItems = ContentStates.map(function (option, index) {
      const states = {
        active: selected === option
      }
      return (
        <ButtonLink key={option}
          states={states}
          theme={classes.button}
          type={ContentStateStyles[index]}
          onClick={clickHandler.bind(contentStateFilter, option)}>
          {option}
        </ButtonLink>
      )
    })
    return (
      <Base atomic={classes.root}>
        {optionItems}
      </Base>
    )
  }
})

export default ContentStateFilter
