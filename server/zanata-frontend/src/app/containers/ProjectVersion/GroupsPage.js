// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import * as PropTypes from "prop-types"
import { InputGroup, FormGroup, FormControl, Pagination, Table
} from 'react-bootstrap'
import { Icon } from '../../components'

class GroupsPage extends Component {

  render() {
    return (
      /* eslint-disable max-len */
        <div className='flexTab wideView'>
          <h2>Groups</h2>
          <div className='toolbar'>
            <FormGroup className='searchBox'>
              <InputGroup>
                <FormControl type='text'
                 value='Search groups'/>
                <InputGroup.Addon>
                  <Icon name='search'
                   className='s1'
                   title='search' />
                </InputGroup.Addon>
              </InputGroup>
            </FormGroup>
            <div className='sortItems'>
              <FormControl componentClass='select'
                id='sort-options'>
                return
                <option>Last active</option>
                <option>Alphabetical</option>
              </FormControl>
            </div>
            <div className='showItems u-pullRight'>
              <span>Show</span>
              <FormControl componentClass='select'
                id='page-size-options'>
                return
                <option>10</option>
                <option>25</option>
                <option>50</option>
                <option>100</option>
              </FormControl>
            </div>
            <div className='pageCount col-xs-7 col-sm-8 col-md-12'>
              <Pagination
               prev
               next
               bsSize='medium'
               items={1}/>
            </div>
          </div>
          <Table striped hover>
            <thead>
            <tr>
              <th>Name</th>
              <th>Members</th>
              <th>Last active</th>
            </tr>
            </thead>
            <tbody>
            <tr>
              <td>Zanata fanatics</td>
              <td>320</td>
              <td>3 days ago</td>
            </tr>
            <tr>
              <td>Japanese translator</td>
              <td>17</td>
              <td>5 days ago</td>
            </tr>
            <tr>
              <td>Word nerds</td>
              <td>58</td>
              <td>12 days ago</td>
            </tr>
            </tbody>
          </Table>
        </div>
        /* eslint-enable max-len */
    )
  }
}

export default GroupsPage
