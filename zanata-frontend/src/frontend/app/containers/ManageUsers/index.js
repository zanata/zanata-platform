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

class ManageUsers extends Component {
  render () {
    return (
      <Page>
        <Helmet title='ManageUsers' />
        <ScrollView>
          <View theme={contentViewContainerTheme}>
            <div className="row clearfix" id="manage-users">
                <div className="row-height">
                    <div className="contentx clearfix center-block">
                        <h2>Manage users <span className="badge">6</span></h2>
                            <div className="left-form toolbar col-xs-12">
                                <div className="search-box col-xs-12 col-sm-6 col-md-6 col-lg-6">
                                    <FormGroup>
                                      <InputGroup>
                                        <FormControl type="text" />
                                        <InputGroup.Button>
                                          <Button><i className="fa fa-search"></i>&nbsp;</Button>
                                        </InputGroup.Button>
                                      </InputGroup>
                                    </FormGroup>
                                </div>

                            </div>
                            <div className="left-form tablepadding col-xs-12">
                                <table className="table">
                                    <thead>
                                        <tr>
                                            <th>&nbsp;</th>
                                            <th>&nbsp;</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <tr>
                                            <td><a href="">admin</a> [admin]
                                            <br />admin@zanata.org
                                            </td>
                                            <td><Button bsSize="small"><i className="fa fa-times"></i>  Delete</Button>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td><a href="">glossarist</a> [glossarist, user]
                                            <br />glossarist@no-email.com
                                            </td>
                                            <td><Button bsSize="small"><i className="fa fa-times"></i>  Delete</Button>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td><a href="">glossaryadmin</a> [glossaryadmin, user]
                                              <br />glossaryadmin@no-email.com
                                            </td>
                                            <td><Button bsSize="small"><i className="fa fa-times"></i>  Delete</Button>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td><a href="">langcoor</a> [translator, user]
                                            <br />lang-coor@no-email.com
                                            </td>
                                            <td><Button bsSize="small"><i className="fa fa-times"></i>  Delete</Button>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td><a href="">pmaint</a> [translator, user]
                                            <br />pmaint@no-email.com
                                            </td>
                                            <td><Button bsSize="small"><i className="fa fa-times"></i>  Delete</Button>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td><a href="">translator</a> [translator, user]
                                            <br />translator@no-email.com
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

export default ManageUsers
