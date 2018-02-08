import * as PropTypes from 'prop-types'
import React from 'react'
import { isEmpty, isUndefined } from 'lodash'

const CategoryItemMatrix: React.SFC<{itemTitle, itemName, wordCount, props?}> = ({
  itemTitle,
  itemName,
  wordCount,
  // @ts-ignore: unused?
  ...props
}) => {
  const title = isEmpty(itemTitle) || isUndefined(itemTitle) ||
    itemTitle === 'null' ? '' : itemTitle
  const name = isEmpty(itemName) || isUndefined(itemName) || itemName === 'null'
    ? 'N/A' : itemName
  return (
    <tr>
      <td className='l--pad-left-0 l--pad-v-0 w--1'>
        {title} <span className='u-textUnderstated'>({name})</span>
      </td>
      <td className='txt--align-right l--pad-right-0 l--pad-v-0 txt--nowrap'>
        {wordCount} <span className='l--pad-left-quarter u-textUnderstated'>
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
