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
                  <h2>Manage Users
                    <span className='badge'>6</span>
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
                            <a href=''>Afrikaans
                            </a>
                            <br />
                            <span className='langcode'>
                            af [Afrikaans]</span>
                          </td>
                          <td>
                            <Button bsSize='small'>
                              <i className='fa fa-times'></i>
                            Delete</Button>
                          </td>
                        </tr>
                        <tr>
                          <td>
                            <a href=''>Albanian</a>
                            <br />
                            <span className='langcode'>
                            sq [shqip]</span>
                          </td>
                          <td>
                            <Button bsSize='small'>
                              <i className='fa fa-times'></i>
                            Delete</Button>
                          </td>
                        </tr>
                        <tr>
                          <td>
                            <a href=''>Amharic</a>
                            <br />
                            <span className='langcode'>
                            am</span>
                          </td>
                          <td>
                            <Button bsSize='small'>
                              <i className='fa fa-times'></i>
                            Delete</Button>
                          </td>
                        </tr>
                        <tr>
                          <td>
                            <a href=''>Angika</a>
                            <br />
                            <span className='langcode'>
                            anp [Angika]</span>
                          </td>
                          <td>
                            <Button bsSize='small'>
                              <i className='fa fa-times'></i>
                            Delete</Button>
                          </td>
                        </tr>
                        <tr>
                          <td>
                            <a href=''>Arabic</a>
                            <br />
                            <span className='langcode'>
                            ar</span>
                          </td>
                          <td>
                            <Button bsSize='small'>
                              <i className='fa fa-times'></i>
                            Delete</Button>
                          </td>
                        </tr>
                        <tr>
                          <td>
                            <a href=''>Arabic (Saudi Arabia)
                              <span className='dis badge'>DISABLED</span>
                            </a>
                            <br />
                            <span className='langcode'>
                            ar-SA</span>
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
