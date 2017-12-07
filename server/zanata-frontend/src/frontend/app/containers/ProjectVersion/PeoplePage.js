import React, { Component } from 'react'
import PropTypes from "prop-types";
import { Button, InputGroup, FormGroup, FormControl, Pagination
} from 'react-bootstrap'
import { Icon } from '../../components'


class PeoplePage extends Component {

  render() {

    return (
        <div className='flexTab wideView'>
          <h2>People</h2>
          <div>
            <Button bsStyle='primary' id='btn-people-add-new'>
              <Icon name='plus' className='n1 plusicon' title='plus' />&nbsp;
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
            <div className='pageCount col-xs-7 col-sm-8
              col-md-12'>
              <Pagination
               prev
               next
               bsSize='medium'
               items={10}/>
            </div>
          </div>

        </div>
    )
  }
}

export default PeoplePage
