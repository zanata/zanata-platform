import React, {PropTypes} from 'react'
import {Pagination} from 'react-bootstrap'
const Pager = React.createClass({
  propTypes: {
    activePage: PropTypes.number.isRequired,
    totalPage: PropTypes.number.isRequired,
    handleSelect: PropTypes.func.isRequired
  },
  handleSelect (eventKey) {
    this.setState({
      activePage: eventKey
    })
  },

  render () {
    return <div>
      <Pagination
        bsSize='medium'
        items={this.props.totalPage}
        activePage={this.props.activePage}
        onSelect={this.handleSelect} />
    </div>
  }
})

export default Pager
