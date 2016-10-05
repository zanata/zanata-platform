import React, {Component} from 'react'
import {Button, InputGroup, FormGroup, FormControl} from 'react-bootstrap'
import Helmet from 'react-helmet'
import {Page, ScrollView, View} from 'zanata-ui'

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
            <div className='row clearfix' id='admin-lang'>
              <div className='row-height'>
                <div className='contentx clearfix center-block'>
                  <h2>Manage Users&nbsp;<span className='badge'>6</span>
                  </h2>
                  <div className='left-form toolbar col-xs-12'>
                    <div className='search-box col-xs-6 col-sm-8 col-md-6
                       col-lg-7'>
                      <FormGroup>
                        <InputGroup>
                          <FormControl type='text' />
                          <InputGroup.Button>
                            <Button>
                              <i className='fa fa-search'></i>&nbsp;</Button>
                          </InputGroup.Button>
                        </InputGroup>
                      </FormGroup>
                    </div>
                  </div>
                  <div className='left-form tablepadding col-xs-12'>
                    <table className='table'>
                      <thead>
                        <tr className='hidden'>
                          <th>Username</th>
                          <th>&nbsp;</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr>
                          <td>
                            <a href=''>admin</a> <small>[admin]</small>
                            <br />
                            <span className='langcode'>
                            admin@zanata.org</span>
                          </td>
                          <td>
                            <Button bsSize='small'>
                              <i className='fa fa-times'></i>
                            Delete</Button>
                          </td>
                        </tr>
                        <tr>
                          <td>
                            <a href=''>glossarist</a> <small>[glossarist, user]
                            </small>
                            <br />
                            <span className='langcode'>
                            glossarist@no-email.com</span>
                          </td>
                          <td>
                            <Button bsSize='small'>
                              <i className='fa fa-times'></i>
                            Delete</Button>
                          </td>
                        </tr>
                        <tr>
                          <td>
                            <a href=''>glossaryadmin</a> <small>
                            [glossary-admin, user]</small>
                            <br />
                            <span className='langcode'>
                            glossary-admin@no-email.com</span>
                          </td>
                          <td>
                            <Button bsSize='small'>
                              <i className='fa fa-times'></i>
                            Delete</Button>
                          </td>
                        </tr>
                        <tr>
                          <td>
                            <a href=''>langcoor</a> <small>[translator, user]
                            </small>
                            <br />
                            <span className='langcode'>
                            lang-coor@no-email.com</span>
                          </td>
                          <td>
                            <Button bsSize='small'>
                              <i className='fa fa-times'></i>
                            Delete</Button>
                          </td>
                        </tr>
                        <tr>
                          <td>
                            <a href=''>pmaint</a> <small>[translator, user]
                            </small>
                            <br />
                            <span className='langcode'>
                            pmaint@no-email.com</span>
                          </td>
                          <td>
                            <Button bsSize='small'>
                              <i className='fa fa-times'></i>
                            Delete</Button>
                          </td>
                        </tr>
                        <tr>
                          <td>
                            <a href=''>translator</a> <small>[translator, user]
                            </small>
                            <br />
                            <span className='langcode'>
                            translator@no-email.com</span>
                          </td>
                          <td>
                            <Button bsSize='small'>
                              <i className='fa fa-times'></i>
                            Delete</Button>
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
