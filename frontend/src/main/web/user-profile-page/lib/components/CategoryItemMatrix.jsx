import React from 'react';

var CategoryItemMatrix = React.createClass(
  {
    render: function() {
      return (
        <tr>
          <td className='l--pad-left-0 l--pad-v-0 w--1'>
            {this.props.itemTitle} <span className='txt--understated'>({this.props.itemName})</span>
          </td>
          <td className='txt--align-right l--pad-right-0 l--pad-v-0 txt--nowrap' >{this.props.wordCount} <span className='txt--understated'>words</span></td>
        </tr>
      )
    }
  }
);

export default CategoryItemMatrix;
