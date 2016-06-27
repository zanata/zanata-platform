import React, { PropTypes } from 'react'

const CategoryItemMatrix = ({
  itemTitle,
  itemName,
  wordCount,
  ...props
}) => {
  return (
    <tr>
      <td className='l--pad-left-0 l--pad-v-0 w--1'>
        {itemTitle} <span className='txt--understated'>({itemName})</span>
      </td>
      <td className='txt--align-right l--pad-right-0 l--pad-v-0 txt--nowrap'>
        {wordCount}
        <span className='l--pad-left-quarter txt--understated'>words</span>
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
