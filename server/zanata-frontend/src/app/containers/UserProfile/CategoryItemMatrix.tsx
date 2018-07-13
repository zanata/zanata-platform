import * as PropTypes from 'prop-types'
import React from 'react'
import { isEmpty, isUndefined } from 'lodash'

const CategoryItemMatrix: React.SFC<{itemTitle: string, itemName: string, wordCount: number}> = ({
  itemTitle,
  itemName,
  wordCount,
}) => {
  const title = isEmpty(itemTitle) || isUndefined(itemTitle) ||
    itemTitle === 'null' ? '' : itemTitle
  const name = isEmpty(itemName) || isUndefined(itemName) || itemName === 'null'
    ? 'N/A' : itemName
  return (
    <tr>
      <td className='l--pad-left-0 l--pad-v-0 w--1'>
        {title} <span className='f6 txt-muted'>({name})</span>
      </td>
      <td className='txt--align-right l--pad-right-0 l--pad-v-0 txt--nowrap fr'>
        {wordCount} <span className='l--pad-left-quarter f6 txt-muted'>
        words</span>
      </td>
    </tr>
  )
}

CategoryItemMatrix.propTypes = {
  itemTitle: PropTypes.string,
  itemName: PropTypes.string,
  wordCount: PropTypes.number
}

export default CategoryItemMatrix
