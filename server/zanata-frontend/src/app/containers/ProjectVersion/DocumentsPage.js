// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import * as PropTypes from "prop-types";
import { InputGroup, FormGroup, FormControl, Pagination, Table,
  ProgressBar
} from 'react-bootstrap'
import { Icon } from '../../components'

class DocumentsPage extends Component {

  render() {
    return (
      /* eslint-disable max-len */
        <div className='flexTab wideView'>
          <h2>Documents</h2>
          <div className='toolbar'>
            <FormGroup className='searchBox'>
              <InputGroup>
                <FormControl type='text'
                 value='Search documents'/>
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
                <option>Last modified</option>
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
              <th>Translated</th>
              <th>Last modified</th>
            </tr>
            </thead>
            <tbody>
            <tr>
              <td><a href="">MobyDick.txt</a></td>
              <td>100%</td>
              <td>3 days ago</td>
            </tr>
            <tr>
              <td colSpan='3' className='progRow'>
                <ProgressBar>
                <ProgressBar className='progress-bar-success'
                             now={100} key={1} />
              </ProgressBar></td>
            </tr>
            <tr>
              <td><a href="">A-really-long-filename-to-test.txt</a></td>
              <td>33%</td>
              <td>5 days ago</td>
            </tr>
            <tr>
              <td colSpan='3' className='progRow'><ProgressBar>
                <ProgressBar className='progress-bar-success'
                             now={33} key={1} />
                <ProgressBar className='progress-bar-warning'
                             now={7} key={2} />
                <ProgressBar className='progress-bar-danger'
                             now={3} key={3} />
                <ProgressBar className='progress-bar-info'
                             now={10} key={4} />
              </ProgressBar>
              </td>
            </tr>
            <tr>
              <td><a href="">Day Of The Triffids.txt</a></td>
              <td>10%</td>
              <td>12 days ago</td>
            </tr>
            <tr>
              <td colSpan='3' className='progRow'>
              <ProgressBar>
                <ProgressBar className='progress-bar-success'
                             now={10} key={1} />
                <ProgressBar className='progress-bar-warning'
                             now={7} key={2} />
                <ProgressBar className='progress-bar-danger'
                             now={33} key={3} />
              </ProgressBar></td>
            </tr>
            </tbody>
          </Table>
        </div>
        /* eslint-enable max-len */
    )
  }
}

export default DocumentsPage
