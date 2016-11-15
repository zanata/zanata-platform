import React, {Component} from 'react'
import {Button, InputGroup, FormGroup, FormControl} from 'react-bootstrap'
import Helmet from 'react-helmet'
import {Page, ScrollView, View, Icon} from 'zanata-ui'

const contentViewContainerTheme = {
  base: {
    w: 'W(100%)'
  }
}

class Roles extends Component {
  render () {
    return (
      <Page>
        <Helmet title='ManageRoles' />
        <ScrollView>
          <View theme={contentViewContainerTheme}>
            <div className='row clearfix' id='admin-lang'>
              <div className='row-height'>
                <div className='content clearfix center-block'>
                  <h2>Manage Roles&nbsp;<span className='badge'>6</span>
                  </h2>
                  <Button bsStyle='primary'>
                    <Icon name='plus'
                      atomic={{m: 'Va(sub)'}}
                      title='plus' />&nbsp;Create role
                  </Button>
                  <div className='left-form toolbar col-xs-12'>
                    <div className='search-box col-xs-6 col-sm-8 col-md-6
                       col-lg-7'>
                      <FormGroup>
                        <InputGroup>
                          <FormControl type='text' />
                          <InputGroup.Button>
                            <Button>
                              <Icon name='search'
                                atomic={{m: 'Va(sub)'}}
                                title='search' />
                            </Button>
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
                            <a href=''>admin
                            </a>
                            <br />
                            <span className='langcode'>
                            [user, glossary-admin]</span>
                          </td>
                          <td>
                            <Button>
                              <Icon name='cross'
                                atomic={{ m: 'H(ms0) Va(sub)' }}
                                title='cross' />
                            Delete</Button>
                          </td>
                        </tr>
                        <tr>
                          <td>
                            <a href=''>glossarist</a>
                          </td>
                          <td>
                            <Button>
                              <Icon name='cross'
                                atomic={{ m: 'H(ms0) Va(sub)' }}
                                title='cross' />
                            Delete</Button>
                          </td>
                        </tr>
                        <tr>
                          <td>
                            <a href=''>glossary-admin</a>
                            <br />
                            <span className='langcode'>
                            [glossarist]</span>
                          </td>
                          <td>
                            <Button>
                              <Icon name='cross'
                                atomic={{ m: 'H(ms0) Va(sub)' }}
                                title='cross' />
                            Delete</Button>
                          </td>
                        </tr>
                        <tr>
                          <td>
                            <a href=''>project-creator</a>
                          </td>
                          <td>
                            <Button>
                              <Icon name='cross'
                                atomic={{ m: 'H(ms0) Va(sub)' }}
                                title='cross' />
                            Delete</Button>
                          </td>
                        </tr>
                        <tr>
                          <td>
                            <a href=''>translator</a>
                            <br />
                            <span className='langcode'>
                            ar</span>
                          </td>
                          <td>
                            <Button>
                              <Icon name='cross'
                                atomic={{ m: 'H(ms0) Va(sub)' }}
                                title='cross' />
                            Delete</Button>
                          </td>
                        </tr>
                        <tr>
                          <td>
                            <a href=''>user</a>
                            <br />
                            <span className='langcode'>
                            [project-creator]</span>
                          </td>
                          <td>
                            <Button>
                              <Icon name='cross'
                                atomic={{ m: 'H(ms0) Va(sub)' }}
                                title='cross' />
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

export default Roles
