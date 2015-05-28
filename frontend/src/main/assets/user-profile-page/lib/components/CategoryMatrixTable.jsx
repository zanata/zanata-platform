import React from 'react';
import _ from 'lodash';
import CategoryItemMatrix from './CategoryItemMatrix';

var CategoryMatrixTable = React.createClass(
  {
    render: function() {
      var categoryMatrix = {},
          rows = [],
          // TODO validate category is one of the key in matrix object
          categoryField = this.props.category,
          categoryFieldTitle = this.props.categoryTitle
        ;

      this.props.matrixData.forEach(function(matrix) {
        var field = matrix[categoryField];
        if (categoryMatrix[field] && categoryMatrix[field]['wordCount']) {
          categoryMatrix[field]['wordCount'] += matrix['wordCount'];
        } else {
          categoryMatrix[field] = {
            wordCount: matrix['wordCount'],
            title: matrix[categoryFieldTitle]
          };
        }
      });

      _.forOwn(categoryMatrix, function(value, key) {
        rows.push(<CategoryItemMatrix key={key} itemName={key} itemTitle={value['title']} wordCount={value['wordCount']} />)
      });

      return (
        <div>
          <h3 className='zeta txt--uppercase txt--understated'>{this.props.categoryName}</h3>
          <table className='l--push-bottom-half'>
            <tbody>
            {rows}
            </tbody>
          </table>
        </div>
      );
    }
  }
);

export default CategoryMatrixTable;
