import React, { Component, PropTypes } from 'react'
import {
  Button,
  InputGroup,
  FormGroup,
  FormControl,

 } from 'react-bootstrap'
import Helmet from 'react-helmet'
import {
  Page,
  ScrollView,
  View
} from 'zanata-ui'

const contentViewContainerTheme = {
  base: {
    w: 'W(100%)'
  }
}

class Languages extends Component {
  render () {
    return (
      <Page>
        <Helmet title='Languages' />
        <ScrollView>
          <View theme={contentViewContainerTheme}>
            <div className="row clearfix" id="admin-lang">
                <div className="row-height">
                    <div className="contentx clearfix center-block">
                        <h2>Languages <span className="badge">121</span></h2>
                        <Button className="btn-primary"><i className="fa fa-plus"></i> Add new language</Button>
                            <div className="left-form toolbar col-xs-12">
                                <div className="search-box col-xs-6 col-sm-8 col-md-6 col-lg-7">
                                    <FormGroup>
                                      <InputGroup>
                                        <FormControl type="text" />
                                        <InputGroup.Button>
                                          <Button><i className="fa fa-search"></i>&nbsp;</Button>
                                        </InputGroup.Button>
                                      </InputGroup>
                                    </FormGroup>
                                </div>

                                <div className="sort-items col-xs-6 col-sm-4 col-md-4 col-lg-3">
                                    <FormControl componentClass="select" className="pull-right" id="ddlList1">
                                        <option value="2">Alphabetical</option>
                                        <option value="1">Locale code</option>
                                        <option value="3">Members</option>
                                    </FormControl>
                                </div>

                                <div className="show-items col-xs-5 col-sm-3 col-md-2 col-lg-2">
                                    <span>Show
                                    </span>
                                    <FormControl inline componentClass="select" id="ddlList2">
                                        <option value="1">10</option>
                                        <option value="2">20</option>
                                        <option value="3">50</option>
                                        <option value="4">100</option>
                                    </FormControl>
                                </div>
                                <div className="page-count pull-right col-xs-7 col-sm-8 col-md-12">
                                    <nav>
                                        <ul className="pagination pull-right">
                                            <li className="disabled">
                                                <span>
                                                  <span aria-hidden="true">«</span>
                                                </span>
                                            </li>
                                            <li className="active">
                                                <span>1 <span className="sr-only">(current)</span></span>
                                            </li>
                                            <li><a href="#">2 <span className="sr-only"></span></a>
                                            </li>

                                            <li><a href="#">3 <span className="sr-only"></span></a>
                                            </li>
                                            <li><a href="#">4 <span className="sr-only"></span></a>
                                            </li>
                                            <li><a href="#">5 <span className="sr-only"></span></a>
                                            </li>
                                            <li><a href="#">6 <span className="sr-only"></span></a>
                                            </li>
                                            <li><a href="#">7 <span className="sr-only"></span></a>
                                            </li>
                                            <li><a href="#">8 <span className="sr-only"></span></a>
                                            </li>

                                            <li><a href="#">9 <span className="sr-only"></span></a>
                                            </li>
                                            <li><a href="#">10 <span className="sr-only"></span></a>
                                            </li>
                                            <li><a href="#">11 <span className="sr-only"></span></a>
                                            </li>
                                            <li><a href="#">12 <span className="sr-only"></span></a>
                                            </li>
                                            <li>
                                                <a href="#" aria-label="Next">
                                                  <span aria-hidden="true">»</span>
                                                </a>
                                            </li>
                                        </ul>
                                    </nav>
                                </div>
                            </div>
                            <div className="left-form tablepadding col-xs-12">
                                <table className="table">
                                    <thead>
                                        <tr>
                                            <th>Language</th>
                                            <th>&nbsp;</th>
                                            <th>&nbsp;</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <tr>
                                            <td><a href="">Afrikaans <span className="greentext badge">DEFAULT</span></a>
                                            <br /><span className="langcode"> af [Afrikaans]</span>
                                            </td>
                                            <td><span><i className="fa fa-user"></i>0</span>
                                            </td>
                                            <td><Button bsSize="small"><i className="fa fa-times"></i>  Delete</Button>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td><a href="">Albanian</a>
                                            <br /><span className="langcode"> sq [shqip]</span>
                                            </td>
                                            <td><span><i className="fa fa-user"></i>2</span>
                                            </td>
                                            <td><Button bsSize="small"><i className="fa fa-times"></i>  Delete</Button>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td><a href="">Amharic</a>
                                              <br /><span className="langcode"> am</span>
                                            </td>
                                            <td><span><i className="fa fa-user"></i>4</span>
                                            </td>
                                            <td><Button bsSize="small"><i className="fa fa-times"></i>  Delete</Button>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td><a href="">Angika</a>
                                            <br /><span className="langcode"> anp [Angika]</span>
                                            </td>
                                            <td><span><i className="fa fa-user"></i>1</span>
                                            </td>
                                            <td><Button bsSize="small"><i className="fa fa-times"></i>  Delete</Button>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td><a href="">Arabic</a>
                                            <br /><span className="langcode"> ar</span>
                                            </td>
                                            <td><span><i className="fa fa-user"></i>5</span>
                                            </td>
                                            <td><Button bsSize="small"><i className="fa fa-times"></i>  Delete</Button>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td><a href="">Arabic (Saudi Arabia) <span className="dis badge">DISABLED</span></a>
                                            <br /><span className="langcode"> ar-SA</span>
                                            </td>
                                            <td><span><i className="fa fa-user"></i>0</span>
                                            </td>
                                            <td><Button bsSize="small"><i className="fa fa-times"></i>  Delete</Button>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td><a href="">Armenian</a>
                                            <br /><span className="langcode"> hy</span>
                                            </td>
                                            <td><span><i className="fa fa-user"></i>1</span>
                                            </td>
                                            <td><Button bsSize="small"><i className="fa fa-times"></i>  Delete</Button>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td><a href="">Assamese</a>
                                            <br /><span className="langcode"> as</span>
                                            </td>
                                            <td><span><i className="fa fa-user"></i>8</span>
                                            </td>
                                            <td><Button bsSize="small"><i className="fa fa-times"></i>  Delete</Button>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td><a href="">Assamese (India) <span className="dis badge">DISABLED</span></a>
                                            <br /><span className="langcode"> anp [Angika]</span>
                                            </td>
                                            <td><span><i className="fa fa-user"></i>1</span>
                                            </td>
                                            <td><Button bsSize="small"><i className="fa fa-times"></i>  Delete</Button>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td><a href="">Arabic</a>
                                            <br /><span className="langcode"> ar</span>
                                            </td>
                                            <td><span><i className="fa fa-user"></i>5</span>
                                            </td>
                                            <td><Button bsSize="small"><i className="fa fa-times"></i>  Delete</Button>
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
          </View>
        </ScrollView>
      </Page>
    )
  }
}

export default Languages
