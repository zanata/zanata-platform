import React, { PropTypes } from 'react'
import { forOwn } from 'lodash'
import CategoryItemMatrix from './CategoryItemMatrix'

const CategoryMatrixTable = ({
  categoryField,
  categoryFieldTitle,
  matrixData,
  categoryName,
  ...props
}) => {
  let categoryMatrix = {}
  let rows = []

  matrixData.forEach(function (matrix) {
    const field = matrix[categoryField]
    if (categoryMatrix[field] && categoryMatrix[field]['wordCount']) {
      categoryMatrix[field]['wordCount'] += matrix['wordCount']
    } else {
      categoryMatrix[field] = {
        wordCount: matrix['wordCount'],
        title: matrix[categoryFieldTitle]
      }
    }
  })
  forOwn(categoryMatrix, function (value, key) {
    rows.push(<CategoryItemMatrix key={key}
      itemName={key}
      itemTitle={value['title']}
      wordCount={value['wordCount']} />)
  })

  return (
    <div>
      <h3 className='zeta txt--uppercase txt--understated'>
        {categoryName}
      </h3>
      <table className='l--push-bottom-half'>
        <tbody>
        {rows}
        </tbody>
      </table>
    </div>
  )
}

CategoryMatrixTable.propTypes = {
  categoryField: PropTypes.string,
  categoryFieldTitle: PropTypes.string,
  matrixData: PropTypes.array,
  categoryName: PropTypes.string
}

export default CategoryMatrixTable
