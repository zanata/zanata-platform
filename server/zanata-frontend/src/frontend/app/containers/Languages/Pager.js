import React, {PropTypes} from 'react'
const Pager = ({
  currentPage,
  totalPage,
  handlePageChanged,
  ...props
}) => {
  /* eslint-disable react/jsx-no-bind */
  return (
    <nav>
      {totalPage < 2
        ? <ul className='pagination pull-right'>
          <li className='disabled'><a href='#'>{currentPage}</a></li>
        </ul>
        : <ul className='pagination pull-right'>
          <li>
            <span>
              <span aria-hidden='true'>« prev</span>
            </span>
          </li>
          <li>
            <span>1
              <span className='sr-only'></span>
            </span>
          </li>
          <li className='disabled'>
            <a href='#'>...
              <span className='sr-only'></span>
            </a>
          </li>
          <li>
            <a href='#'>7
              <span className='sr-only'></span>
            </a>
          </li>
          <li>
            <a href='#'>8
              <span className='sr-only'></span>
            </a>
          </li>
          <li className='active'>
            <a href='#'>{currentPage}
              <span className='sr-only'></span>
            </a>
          </li>
          <li>
            <a href='#'>10
              <span className='sr-only'></span>
            </a>
          </li>
          <li>
            <a href='#'>11
              <span className='sr-only'></span>
            </a>
          </li>
          <li className='disabled'>
            <a href='#'>...
              <span className='sr-only'></span>
            </a>
          </li>
          <li>
            <a href='#'>{totalPage}
              <span className='sr-only'></span>
            </a>
          </li>
          <li>
            <a href='#' aria-label='Next'>
              <span aria-hidden='true'>next »</span>
            </a>
          </li>
        </ul>
      }
    </nav>
  )
  /* eslint-disable react/jsx-no-bind */
}

Pager.propTypes = {
  currentPage: PropTypes.number.isRequired,
  totalPage: PropTypes.number.isRequired,
  handlePageChanged: PropTypes.func.isRequired
}

export default Pager
