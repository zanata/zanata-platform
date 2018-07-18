import React from 'react'
import * as PropTypes from 'prop-types'
import { forOwn } from 'lodash'
import CategoryItemMatrix from './CategoryItemMatrix'

/** @type
    { React.StatelessComponent<{
        key: 'locales'|'projects',
        matrixData: any[],
        category: string,
        categoryTitle: string,
        categoryName: string}>
    } */
const CategoryMatrixTable = ({
  matrixData,
  category,
  categoryTitle,
  categoryName,
  // @ts-ignore: unused?
  key // TODO what's this for?
}) => {
  let categoryMatrix = {}
  /** @type {any[]} */
  let rows = []

  matrixData.forEach(function (matrix) {
    const field = matrix[category]
    if (categoryMatrix[field] && categoryMatrix[field]['wordCount']) {
      categoryMatrix[field]['wordCount'] += matrix['wordCount']
    } else {
      categoryMatrix[field] = {
        wordCount: matrix['wordCount'],
        title: matrix[categoryTitle]
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
    <React.Fragment>
      <h3 className='txt-muted ttu f6'>
        {categoryName}
      </h3>
      <table>
        <tbody>
        {rows}
        </tbody>
      </table>
    </React.Fragment>
  )
}

CategoryMatrixTable.propTypes = {
  matrixData: PropTypes.array,
  category: PropTypes.string,
  categoryTitle: PropTypes.string,
  categoryName: PropTypes.string,
  key: PropTypes.string
}

export default CategoryMatrixTable
