import React from 'react'
import { merge } from 'lodash'
import PureRenderMixin from 'react-addons-pure-render-mixin'
import Actions from '../../actions/userMatrix'
import dateUtil from '../../utils/DateHelper'
import {
  Base,
  Button
} from '../../components'

const classes = {
  root: {
    bd: 'Bdw(rq) Bdc(t) Bds(s)',
    bgc: 'Bgc(cc)',
    bgcp: 'Bgcp(pb)',
    ov: 'Ov(h)',
    p: 'P(0)',
    pos: 'Pos(r)',
    ta: 'Ta(c)',
    va: 'Va(t)'
  },
  calButton: {
    base: {
      d: 'D(b)',
      bgc: 'Bgc(#fff.7)',
      h: 'H(100%)',
      w: 'W(100%)',
      disabled: {
        op: '',
        bgc: 'Bgc(#fff.85):di'
      }
    },
    active: {
      bgc: 'Bgc(t)',
      c: 'C(#fff)'
    }
  },
  calDate: {
    fz: 'Fz(msn1)',
    p: 'Py(re)'
  },
  calInfo: {
    bgc: 'Bgc(#fff.4)',
    fw: 'Fw(600)',
    p: 'Py(re)'
  }
}
/**
 * Clickable date and word count component for daily statistics
 */
var DayMatrix = React.createClass({
  mixins: [PureRenderMixin],
  propTypes: {
    dateLabel: React.PropTypes.string,
    date: React.PropTypes.string,
    wordCount: React.PropTypes.number,
    selectedDay: React.PropTypes.string
  },

  handleDayClick: function (event) {
    var dayChosen = this.props.date
    if (this.props.selectedDay === dayChosen) {
      // click the same day again will cancel selection
      Actions.clearSelectedDay()
    } else {
      Actions.onDaySelected(dayChosen)
    }
  },

  render: function () {
    const {
      date,
      dateLabel,
      selectedDay,
      wordCount
    } = this.props
    // Note: this will make this component impure. But it will only become
    // impure when you render it between midnight, e.g. two re-render attempt
    // happen across two days with same props, which I think it's ok.
    const dateIsInFuture = date ? dateUtil.isInFuture(date) : false

    const buttonTheme = {
      base: merge({},
        classes.calButton.base,
        date === selectedDay && classes.calButton.active
      )
    }

    return (
      <Base tagName='td' theme={classes.root}>
        {date
        ? (<Button onClick={this.handleDayClick}
            disabled={dateIsInFuture || !date}
            theme={buttonTheme}
            title={wordCount + ' words'}>
            <Base atomic={classes.calDate}>
              {date ? dateLabel : '\u00a0'}
            </Base>
            <Base atomic={classes.calInfo}>
              {dateIsInFuture ? '\u00a0' : wordCount}
            </Base>
          </Button>)
        : <Base atomic={{bgc: 'Bgc(#fff.85)', stretchedBox: 'StretchedBox'}} />}
      </Base>
    )
  }
})

export default DayMatrix
