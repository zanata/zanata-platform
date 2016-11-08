import React, { PropTypes } from 'react'
import {
  ContentStates,
  ContentStateStyles
} from '../../constants/Options'
import {
  ButtonLink,
  Base
} from 'zanata-ui'

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
const ContentStateFilter = ({
  selectedContentState,
  handleFilterChanged,
  ...props
}) => {
  const optionItems = ContentStates.map(function (option, index) {
    const states = {
      active: selectedContentState === option
    }
    /* eslint-disable react/jsx-no-bind */
    return (
      <ButtonLink key={option}
        states={states}
        theme={classes.button}
        type={ContentStateStyles[index]}
        onClick={() => handleFilterChanged(option)}>
        {option}
      </ButtonLink>
    )
    /* eslint-enable react/jsx-no-bind */
  })
  return (
    <Base atomic={classes.root}>
      {optionItems}
    </Base>
  )
}

ContentStateFilter.propTypes = {
  selectedContentState: PropTypes.oneOf(ContentStates).isRequired,
  handleFilterChanged: PropTypes.func
}

export default ContentStateFilter
