import React, { PropTypes } from 'react'
import { merge } from 'lodash'
import { Row } from './'

const classes = {
  base: {
    bd: 'Bdb(bd1) Bdc(light)',
    h: 'H(r2)'
  },
  highlight: {
    trs: 'Trs(aeo)',
    hover: {
      bgc: 'Bgc(light):h'
    }
  },
  selected: {
    bgc: 'Bgc(light)',
    custom: 'row--selected'
  }
}
/**
 * Styled table row component (based on Row) which used with TableCell.
 */
const TableRow = ({
  children,
  className,
  selected,
  highlight = false,
  theme = {},
  ...props
}) => {
  const themed = merge({},
    classes,
    theme
  )
  const themedState = {
    base: merge({},
      themed.base,
      highlight && classes.highlight,
      selected && classes.selected,
      className && { classes: className }
    )
  }
  return (
    <Row
      {...props}
      theme={themedState}>
      {children}
    </Row>
  )
}

TableRow.propType = {
  children: PropTypes.node,
  highlight: PropTypes.bool,
  selected: PropTypes.bool
}

export default TableRow
