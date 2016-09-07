import React, {Component} from 'react'
import {Button, Panel} from 'react-bootstrap'
import Helmet from 'react-helmet'
import {Page, ScrollView, View} from 'zanata-ui'

const contentViewContainerTheme = {
  base: {
    w: 'W(100%)'
  }
}
const title = (
  <h3>Panel title</h3>
)
const panelsInstance = (
  <div>
    <Panel header={title}>
      <div className='panel-body'>Panel content</div>
    </Panel>
  </div>
)

class ManageSearch extends Component {

  render () {
    return (
      <Page>
        <Helmet title='ManageSearch' />
        <ScrollView>
          <View theme={contentViewContainerTheme}>
            <div className='row clearfix' id='admin-lang'>
              <div className='row-height'>
                <div className='contentx clearfix center-block'>
                  <h2>Manage Search
                  </h2>
                  <panelsInstance>
                    <Panel bsStyle='primary' header={title}>
                        Panel content
                    </Panel>
                  </panelsInstance>
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
                            <Button bsSize='small'>
                              <i className='fa fa-times'></i>
                            Delete</Button>
                          </td>
                        </tr>
                        <tr>
                          <td>
                            <a href=''>glossarist</a>
                          </td>
                          <td>
                            <Button bsSize='small'>
                              <i className='fa fa-times'></i>
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
                            <Button bsSize='small'>
                              <i className='fa fa-times'></i>
                            Delete</Button>
                          </td>
                        </tr>
                        <tr>
                          <td>
                            <a href=''>project-creator</a>
                          </td>
                          <td>
                            <Button bsSize='small'>
                              <i className='fa fa-times'></i>
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
                            <Button bsSize='small'>
                              <i className='fa fa-times'></i>
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
export default ManageSearch
