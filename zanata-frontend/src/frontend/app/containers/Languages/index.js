import React, {PropTypes, Component} from 'react'
import { connect } from 'react-redux'
import {Button, InputGroup, FormGroup, FormControl} from 'react-bootstrap'
import Helmet from 'react-helmet'
import {Page, ScrollView, View, LoaderText} from 'zanata-ui'

import {
  initialLoad,
  handleDelete,
  pageSizeOption,
  sortOption
} from '../../actions/languages'

const contentViewContainerTheme = {
  base: {
    w: 'W(100%)'
  }
}

const loadingContainerTheme = {
  base: {
    ai: 'Ai(c)',
    flxg: 'Flxg(1)',
    jc: 'Jc(c)',
    w: 'W(100%)'
  }
}

class Languages extends Component {
  componentDidMount () {
    this.props.handleInitLoad()
  }

  render () {
    const {
      results,
      permission,
      loading
    } = this.props

    return (
      <Page>
        <Helmet title='Languages' />
        <ScrollView>
          <View theme={contentViewContainerTheme}>
            <div className='row clearfix' id='admin-lang'>
              <div className='row-height'>
                <div className='contentx clearfix center-block'>
                  <h2>
                    Languages {!loading &&
                      <span className='badge'>{results.length}</span>}
                  </h2>
                  {permission.canAddLocale &&
                    <Button className='btn-primary'>
                      <i className='fa fa-plus'></i> Add new language
                    </Button>
                  }
                  {loading
                      ? <View theme={loadingContainerTheme}>
                        <LoaderText theme={{ base: { fz: 'Fz(ms1)' } }}
                          size='2' loading />
                      </View>
                      : (<div className='left-form toolbar col-xs-12'>
                        <div className='search-box col-xs-6 col-sm-8 col-md-6
                         col-lg-7'>
                          <FormGroup>
                            <InputGroup>
                              <FormControl type='text' />
                              <InputGroup.Button>
                                <Button>
                                  <i className='fa fa-search'></i>&nbsp;
                                </Button>
                              </InputGroup.Button>
                            </InputGroup>
                          </FormGroup>
                        </div>
                        <div className='sort-items col-xs-6 col-sm-4 col-md-4
                          col-lg-3'>
                          <FormControl componentClass='select'
                            className='pull-right' id='sort-options'>
                            {sortOption.map(function (value, i) {
                              return <option key={i} value={value}>
                                {value}</option>
                            })}
                          </FormControl>
                        </div>
                        <div className='show-items col-xs-5 col-sm-3 col-md-2
                          col-lg-2'>
                          <span>Show</span>
                          <FormControl inline componentClass='select'
                            id='page-size-options'>
                            {pageSizeOption.map(function (value, i) {
                              return <option key={i} value={value}>
                                {value}</option>
                            })}
                          </FormControl>
                        </div>
                        <div className='page-count pull-right col-xs-7 col-sm-8
                          col-md-12'>
                          <nav>
                            <ul className='pagination pull-right'>
                              <li>
                                <span>
                                  <span aria-hidden='true'>« prev</span>
                                </span>
                              </li>
                              <li>
                                <span>1
                                  <span className='sr-only'>(current)</span>
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
                                <a href='#'>9
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
                                <a href='#'>36
                                  <span className='sr-only'></span>
                                </a>
                              </li>
                              <li>
                                <a href='#' aria-label='Next'>
                                  <span aria-hidden='true'>next »</span>
                                </a>
                              </li>
                            </ul>
                          </nav>
                        </div>
                      </div>)
                  }
                  {!loading &&
                    <div className='left-form tablepadding col-xs-12'>
                      <table className='table'>
                        <thead>
                          <tr className='hidden'>
                            <th>Language</th>
                            <th>&nbsp;</th>
                            <th>&nbsp;</th>
                          </tr>
                        </thead>
                        <tbody>
                        {results.map(function (value, i) {
                          return (
                            <tr key={i}>
                              <td>
                                <a href=''>
                                  <span className='Pend(rq)'>
                                    {value.displayName}
                                  </span>
                                  {value.enabledByDefault &&
                                    <span className='greentext badge'>
                                      DEFAULT
                                    </span>
                                  }
                                  {!value.enabled &&
                                    <span className='dis badge'>
                                      DISABLED
                                    </span>
                                  }
                                </a>
                                <br />
                                <span className='langcode'>
                                  {value.localeId} [{value.nativeName}]
                                </span>
                              </td>
                              <td>
                                <span>
                                  <i className='fa fa-user'></i>
                                  {value.membersCount}
                                </span>
                              </td>
                              {permission.canDeleteLocale &&
                                <td>
                                  <Button bsSize='small'>
                                    <i className='fa fa-times'></i> Delete
                                  </Button>
                                </td>
                              }
                            </tr>
                          )
                        })}
                        </tbody>
                      </table>
                    </div>}
                </div>
              </div>
            </div>
          </View>
        </ScrollView>
      </Page>
    )
  }
}

Languages.propTypes = {
  searchText: PropTypes.string,
  permission: PropTypes.object,
  page: PropTypes.number,
  size: PropTypes.number,
  sort: PropTypes.string,
  results: PropTypes.array,
  loading: PropTypes.bool,
  handleInitLoad: PropTypes.func,
  handleDelete: PropTypes.func
}

const mapStateToProps = (state) => {
  return {
    searchText: state.routing.location.query.q,
    page: parseInt(state.routing.location.query.page),
    size: parseInt(state.routing.location.query.size),
    sort: state.routing.location.query.sort,
    results: state.languages.locales,
    loading: state.languages.loading,
    permission: state.languages.permission
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    handleInitLoad: () => {
      dispatch(initialLoad())
    },
    handleDelete: (event) => {
      handleDelete(event.target.value || '')
    }
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(Languages)
