import React from 'react'
import { Component } from 'react'
import * as PropTypes from "prop-types";
import { Button, InputGroup, FormGroup, FormControl, Pagination, Table
} from 'react-bootstrap'
import { Icon } from '../../components'


class PeoplePage extends Component {

  render() {

    return (
      /* eslint-disable max-len */
        <div className='flexTab wideView'>
          <h2>People</h2>
          <div>
            <Button bsStyle='primary' id='btn-people-add-new'>
              <Icon name='plus' className='n1' parentClassName='plusicon'
                  title='plus'/>&nbsp;
              Add someone
            </Button>
          </div>
          <div className='toolbar'>
            <FormGroup className='searchBox'>
              <InputGroup>
                <FormControl type='text'
                 value='Search people'/>
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
               items={10}/>
            </div>
          </div>
          <Table striped hover>
            <thead>
            <tr>
              <th>Name</th>
              <th>Role</th>
              <th>&nbsp;</th>
            </tr>
            </thead>
            <tbody>
            <tr>
              <td>Admin</td>
              <td>Maintainer</td>
              <td><Button bsStyle='default'>
                <Icon name='settings' className='s0' />Manage permissions
              </Button></td>
            </tr>
            <tr>
              <td>Tux</td>
              <td>Reviewer</td>
              <td><Button bsStyle='default'>
                <Icon name='settings' className='s0' />Manage permissions
              </Button></td>
            </tr>
            <tr>
              <td>Alix</td>
              <td>Translator</td>
              <td><Button bsStyle='default'>
                <Icon name='settings' className='s0' />Manage permissions
              </Button></td>
            </tr>
            </tbody>
          </Table>
        </div>
        /* eslint-enable max-len */
    )
  }
}

export default PeoplePage
