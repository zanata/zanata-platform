import React, {FormEvent} from 'react'
import {connect} from 'react-redux'
import { isEqual, toString} from 'lodash'
import Helmet from 'react-helmet'
import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Form from 'antd/lib/form'
import 'antd/lib/form/style/css'
import Input from 'antd/lib/input'
import 'antd/lib/input/style/css'
import InputNumber from 'antd/lib/input-number'
import 'antd/lib/input-number/style/css'
import Card from 'antd/lib/card'
import Select from 'antd/lib/select'
import 'antd/lib/select/style/css'
import Layout from 'antd/lib/layout'
import Breadcrumb from 'antd/lib/breadcrumb'
import 'antd/lib/breadcrumb/style/css'
import {Link} from '../../components'
import Row from 'antd/lib/row'
import 'antd/lib/row/style/css'
import Col from 'antd/lib/col'
import 'antd/lib/col/style/css'
import Checkbox from 'antd/lib/checkbox'
import 'antd/lib/checkbox/style/css'
import Spin from 'antd/lib/spin'
import 'antd/lib/spin/style/css'
import Notification, {IconType} from 'antd/lib/notification'
import 'antd/lib/notification/style/css'
import {fetchServerSettings, handleSaveServerSettings
} from '../../actions/admin-actions'

interface NOTIFICATION {
  severity: IconType,
  message: string,
  description: string
}

interface AdminState {
  admin: {
    notification: NOTIFICATION,
    serverSettings: {
      loading: boolean,
      saving: boolean,
      settings: SettingsState
    }
  }
}

interface SettingProperty {
  value: string | boolean | number | undefined,
  defaultValue: string | boolean | number | undefined
}

export interface SettingsState {
  ['host.url']: SettingProperty,
  ['register.url']: SettingProperty,
  ['email.domain']: SettingProperty,
  ['email.admin.addr']: SettingProperty,
  ['email.from.addr']: SettingProperty,
  ['terms.conditions.url']: SettingProperty,
  ['help.url']: SettingProperty,
  ['piwik.url']: SettingProperty,
  ['piwik.idSite']: SettingProperty,
  ['log.email.active']: SettingProperty,
  ['log.email.level']: SettingProperty,
  ['log.destination.email']: SettingProperty,
  ['permitted.user.email.domain']: SettingProperty,
  ['gravatar.rating']: SettingProperty,
  ['display.user.email']: SettingProperty,
  ['allow.anonymous.user']: SettingProperty,
  ['accept.translator.requests']: SettingProperty,
  ['max.concurrent.req.per.apikey']: SettingProperty,
  ['max.active.req.per.apikey']: SettingProperty,
  ['fileupload.max.files.per.upload']: SettingProperty,
  ['tm.fuzzy.bands']: SettingProperty,
  [key: string]: SettingProperty
}

interface Props {
  notification: NOTIFICATION
  loading: boolean,
  saving: boolean,
  settings: SettingsState,
  handleSaveSettings:
    (prevSettings: SettingsState, newSettings: SettingsState) => void,
  fetchServerSettings: () => void,
  form: any,
  router: any
}

class ServerSettings extends React.Component<Props, SettingsState> {
  constructor(props: Props) {
    super(props)
    this.state = this.getDefaultState(props)
  }

  public componentDidMount() {
    this.props.fetchServerSettings()
  }

  public componentWillReceiveProps(nextProps: Props) {
    const { notification } = nextProps
    if (notification && this.props.notification !== notification) {
      Notification[notification.severity]({
        message: notification.message,
        description: notification.description,
        duration: null
      })
    }
    if (!isEqual(this.props, nextProps)) {
      this.setState(this.getDefaultState(nextProps))
    }
  }

  public render() {
    /* tslint:disable */
    const URLPattern = /(http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/
    const emailPattern = /^(([a-zA-Z0-9_\-\.]+)@([a-zA-Z0-9_\-\.]+)\.([a-zA-Z]{2,5}){1,25})/
    const emailListPattern = /^(([a-zA-Z0-9_\-\.]+)@([a-zA-Z0-9_\-\.]+)\.([a-zA-Z]{2,5}){1,25})+([,]\s?(([a-zA-Z0-9_\-\.]+)@([a-zA-Z0-9_\-\.]+)\.([a-zA-Z]{2,5}){1,25})+)*$/
    const domainNamePattern = /^[a-zA-Z0-9][a-zA-Z0-9-]{1,61}[a-zA-Z0-9](?:\.[a-zA-Z]{2,})+$/
    const domainNameListPattern = /^[a-zA-Z0-9][a-zA-Z0-9-]{1,61}[a-zA-Z0-9](?:\.[a-zA-Z]{2,})+([,]\s?[a-zA-Z0-9][a-zA-Z0-9-]{1,61}[a-zA-Z0-9](?:\.[a-zA-Z]{2,}))*$/
    /* tslint:enable */

    const {
      getFieldDecorator, isFieldsTouched
    } = this.props.form
    const {loading, saving} = this.props
    const enableSaveButton = !saving && isFieldsTouched()
    const formItemLayout = {
      labelCol: {
        xs: {span: 24},
        sm: {span: 6},
      },
      wrapperCol: {
        xs: {span: 24},
        sm: {span: 18},
      }
    }

    const tailFormItemLayout = {
      wrapperCol: {
        xs: {
          span: 24,
          offset: 0,
        },
        sm: {
          span: 24,
          offset: 4,
        },
      },
    }

    const concurrentHelp = (
      <span>Max concurrent requests per API key. Once over the limit server will return status
        code 403. 0 means no limit. <span className='u-textBold'>
          Default is {this.state['max.concurrent.req.per.apikey'].defaultValue}</span>
      </span>)

    const activeHelp = (
      <span>Max active requests per API key. Request may block. 0 means no limit. If this is greater
                    than max concurrent request limit, it will have no effect. <span
          className='u-textBold'>Default is {this.state['max.active.req.per.apikey'].defaultValue}</span>
      </span>)

    const maxFileUploadHelp = (
      <span>Maximum number of files a user can queue for upload in the web upload dialog. <span
        className='u-textBold'>Default is {this.state['fileupload.max.files.per.upload'].defaultValue}</span>
      </span>
    )

    const helpUrlPlaceholder = 'Default to (if empty) ' +  this.state['help.url'].defaultValue
    const termUrlPlaceholder = 'Default to (if empty) ' +  this.state['terms.conditions.url'].defaultValue

    return (
        <div className='container centerWrapper'>
          <Layout>
            <Breadcrumb>
              <Breadcrumb.Item key='home'>
                <Link link='/admin/home' id='admin-home-link' useHref={false}>
                  Administration
                </Link>
              </Breadcrumb.Item>
            </Breadcrumb>
            <Helmet title='Server settings'/>
            {loading ? <Spin size='large' />
              : <Form>
              <Card title='General'>
                <Form.Item  {...formItemLayout} label='Server URL'
                  extra='The base URL for the server, including the application
                  context path (no final slash)'>
                  {getFieldDecorator('host.url', {
                    initialValue: this.state['host.url'].value,
                    rules: [{
                      pattern: URLPattern,
                      message: 'Invalid URL. Example: http://zanata.example.com'
                    }]
                  })(
                    <Input
                      onChange={(e) => this.updateFormField('host.url', e.target.value)}
                      placeholder='http://zanata.example.com'/>
                  )}
                </Form.Item>

                <Form.Item {...formItemLayout} label='Register URL'
                  extra='The user registration URL for the server'>
                  {getFieldDecorator('register.url', {
                    initialValue: this.state['register.url'].value,
                    rules: [{
                      pattern: URLPattern,
                      message: 'Invalid URL. Example: http://example.com/register'
                    }]
                  })(
                    <Input
                      onChange={(e) => this.updateFormField('register.url', e.target.value)}
                      placeholder='http://example.com/register'/>
                  )}
                </Form.Item>

                <Form.Item {...formItemLayout} label='Email Domain Name'
                  extra='Email Domain Name should be in example.com format'>
                  {getFieldDecorator('email.domain', {
                    initialValue: this.state['email.domain'].value,
                    rules: [{
                      pattern: domainNamePattern,
                      message: 'Invalid domain name. Example: redhat.com'
                    }]
                  })(
                    <Input
                      onChange={(e) => this.updateFormField('email.domain', e.target.value)}
                      placeholder='redhat.com'/>
                  )}
                </Form.Item>

                <Form.Item {...formItemLayout} label='Contact Admin Email'
                  extra={<span>Email will be sent to these addresses when the 'Contact
                    Admin' form is used. <span className='u-block'>
                    This field does not change the individual email address for
                    any admin users</span></span>}>
                  {getFieldDecorator('email.admin.addr', {
                    initialValue: this.state['email.admin.addr'].value,
                    rules: [{
                      pattern: emailListPattern,
                      message: 'Invalid format. Example: username@domain.com'
                    }]
                  })(
                    <Input
                      onChange={(e) => this.updateFormField('email.admin.addr', e.target.value)}
                      placeholder='username@domain.com (comma separated for multiple'/>
                  )}
                </Form.Item>

                <Form.Item {...formItemLayout} label='From Email Address'
                  extra='This will be used in the "from" field of any emails sent by
                    this Zanata server'>
                  {getFieldDecorator('email.from.addr', {
                    initialValue: this.state['email.from.addr'].value,
                    rules: [{
                      pattern: emailPattern,
                      message: 'Invalid format. Example: username@domain.com'
                    }]
                  })(
                    <Input
                      onChange={(e) => this.updateFormField('email.from.addr', e.target.value)}
                      placeholder='username@domain.com'/>
                  )}
                </Form.Item>

                <Form.Item {...formItemLayout} label='Terms of Use URL'
                  extra='The URL for terms of use statement'>
                  {getFieldDecorator('terms.conditions.url', {
                    initialValue: this.state['terms.conditions.url'].value,
                    rules: [{
                      pattern: URLPattern,
                      message: 'Invalid URL. Example: http://zanata.org/term'
                    }]
                  })(
                    <Input
                      onChange={(e) => this.updateFormField('terms.conditions.url', e.target.value)}
                      placeholder={termUrlPlaceholder}/>
                  )}
                </Form.Item>

                <Form.Item {...formItemLayout} label='Help URL'
                  extra='The URL for help page'>
                  {getFieldDecorator('help.url', {
                    initialValue: this.state['help.url'].value,
                    rules: [{
                      pattern: URLPattern,
                      message: 'Invalid URL. Example: http://docs.zanata.org/en/release'
                    }]
                  })(
                    <Input onChange={(e) => this.updateFormField('help.url', e.target.value)}
                      placeholder={helpUrlPlaceholder} />
                  )}
                </Form.Item>

                <Form.Item {...formItemLayout} label='Piwik analytic tools'>
                  <Row gutter={8}>
                    <Col span={20}>
                      <Form.Item extra={<span className='u-textWarning'>
                          Please check the anonymous user access setting.
                          If you disable anonymous access,
                          your Piwik server should also be protected</span>}>
                        {getFieldDecorator('piwik.url', {
                          initialValue: this.state['piwik.url'].value,
                          rules: [{
                            pattern: URLPattern,
                            message: 'Invalid URL. Example: http://example/piwik'
                          }]
                        })(
                          <Input
                            onChange={(e) => this.updateFormField('piwik.url', e.target.value)}
                            placeholder='http://example/piwik'/>
                        )}
                      </Form.Item>
                    </Col>
                    <Col span={4}>
                      <Form.Item>
                        {getFieldDecorator('piwik.idSite', {
                          initialValue: this.state['piwik.idSite'].value
                        })(
                          <Input
                            onChange={(e) => this.updateFormField('piwik.idSite', e.target.value)}
                            placeholder='Id in Piwik'/>
                        )}
                      </Form.Item>
                    </Col>
                  </Row>
                </Form.Item>

                <Card title='Server log' type='inner'>
                  <Form.Item {...tailFormItemLayout}
                      extra={<div>Enables or disables the sending of Zanata diagnostics log
                      information via email.<span className='u-block'>
                      <span className='u-textDanger'>Error</span> will only send error
                        messages, while <span className='u-textWarning'>
                        Warning</span> will send both warning and error messages
                      </span></div>}>
                    {getFieldDecorator('log.email.active', {
                      initialValue: this.state['log.email.active'].value,
                      valuePropName: 'checked'
                    })(
                      <Checkbox
                        onChange={(e) => this.updateFormField('log.email.active', e.target.checked)}>
                        Enable the sending of Zanata diagnostics log
                        information via email
                      </Checkbox>
                    )}
                  </Form.Item>

                  <Form.Item {...formItemLayout} label='Email server logs'>
                    <Row gutter={8}>
                      <Col span={4} id='log.email.level'>
                        <Form.Item>
                          {getFieldDecorator('log.email.level', {
                            initialValue: this.state['log.email.level'].value
                          })(
                            <Select placeholder='Log level'
                              disabled={!this.state['log.email.active'].value}
                              onChange={(e) => this.updateFormField('log.email.level', e)}>
                              <Select.Option key={'WARN'}>
                                Warning
                              </Select.Option>
                              <Select.Option key={'ERROR'}>
                                Error
                              </Select.Option>
                            </Select>
                          )}
                        </Form.Item>
                      </Col>
                      <Col span={20}>
                        <Form.Item>
                          {getFieldDecorator('log.destination.email', {
                            initialValue: this.state['log.destination.email'].value,
                            rules: [{
                              pattern: emailListPattern,
                              message: 'Invalid format. Example: username@domain.com'
                            }]
                          })(
                            <Input
                              onChange={(e) => this.updateFormField('log.destination.email', e.target.value)}
                              placeholder='username@domain.name (comma separated for multiple)'
                              disabled={!this.state['log.email.active'].value}/>
                          )}
                        </Form.Item>
                      </Col>
                    </Row>
                  </Form.Item>
                </Card>
              </Card>
              <br/>
              <Card title='Security/Privacy'>
                <Form.Item {...formItemLayout} className='multi-rows-label'
                  label='Permitted user email domain'
                  extra='Only users with email addresses from these domains are
                    allowed to register and use Zanata'>

                  {getFieldDecorator('permitted.user.email.domain', {
                    initialValue: this.state['permitted.user.email.domain'].value,
                    rules: [{
                      pattern: domainNameListPattern,
                      message: 'Invalid format. Example: redhat.com'
                    }]
                  })(
                    <Input
                      onChange={(e) => this.updateFormField('permitted.user.email.domain', e.target.value)}
                      placeholder='redhat.com (comma separated for multiple)'/>
                  )}
                </Form.Item>

                <Form.Item {...formItemLayout} label='Gravatar Rating'
                  extra='Maximum Gravatar rating shown by Zanata'>
                  {getFieldDecorator('gravatar.rating', {
                    initialValue: this.state['gravatar.rating'].value
                  })(
                    <Select
                      onChange={(e) => this.updateFormField('gravatar.rating', e)}>
                      <Select.Option key='G'>
                        G Rating
                      </Select.Option>
                      <Select.Option key='PG'>
                        PG Rating
                      </Select.Option>
                      <Select.Option key='R'>
                        R Rating
                      </Select.Option>
                      <Select.Option key='X'>
                        X Rating
                      </Select.Option>
                    </Select>
                  )}
                </Form.Item>

                <Form.Item {...tailFormItemLayout}
                  extra='If enabled, user email will be visible'>
                  {getFieldDecorator('display.user.email', {
                    initialValue: this.state['display.user.email'].value,
                    valuePropName: 'checked'
                  })(
                    <Checkbox
                      onChange={(e) => this.updateFormField('display.user.email', e.target.checked)}>
                      Display user email
                    </Checkbox>
                  )}
                </Form.Item>

                <Form.Item {...tailFormItemLayout} extra={
                  <span>If enabled, anonymous user will be able to access read only
                  resources.<span className='u-textWarning u-block'>If disabled, please also consider the Piwik settings
                    and the home page content</span></span>
                }>
                  {getFieldDecorator('allow.anonymous.user', {
                    initialValue: this.state['allow.anonymous.user'].value,
                    valuePropName: 'checked'
                  })(
                    <Checkbox
                      onChange={(e) => this.updateFormField('allow.anonymous.user', e.target.checked)}>
                      Allow anonymous user to access resources
                    </Checkbox>
                  )}
                </Form.Item>

                <Form.Item {...tailFormItemLayout}
                  extra='If enabled, user requests to join a language team will be
                  accepted automatically'>
                  {getFieldDecorator('accept.translator.requests', {
                    initialValue: this.state['accept.translator.requests'].value,
                    valuePropName: 'checked'
                  })(
                    <Checkbox
                      onChange={(e) => this.updateFormField('accept.translator.requests', e.target.checked)}>
                      Automatically accept requests for language team
                      translators
                    </Checkbox>
                  )}
                </Form.Item>
              </Card>
              <br/>
              <Card title='Client'>
                <Card title='Max requests per API key' type='inner'>
                  <Form.Item {...formItemLayout}
                    label='Concurrent' extra={concurrentHelp}>
                    {getFieldDecorator('max.concurrent.req.per.apikey', {
                      initialValue: this.state['max.concurrent.req.per.apikey'].value
                    })(
                      <InputNumber
                        placeholder={toString(this.state['max.concurrent.req.per.apikey'].defaultValue)}
                        onChange={(e) => this.updateFormField('max.concurrent.req.per.apikey', e)}
                        min={0} max={99999}/>
                    )}
                  </Form.Item>

                  <Form.Item {...formItemLayout}
                    label='Active' extra={activeHelp}>
                    {getFieldDecorator('max.active.req.per.apikey', {
                      initialValue: this.state['max.active.req.per.apikey'].value
                    })(
                      <InputNumber placeholder={toString(this.state['max.active.req.per.apikey'].defaultValue)}
                        onChange={(e) => this.updateFormField('max.active.req.per.apikey', e)}
                        min={0} max={99999}/>
                    )}
                  </Form.Item>
                </Card>
                <br/>
                <Form.Item {...formItemLayout}
                  label='Max files per upload' extra={maxFileUploadHelp}>
                  {getFieldDecorator('fileupload.max.files.per.upload', {
                    initialValue: this.state['fileupload.max.files.per.upload'].value
                  })(
                    <InputNumber
                      placeholder={toString(this.state['fileupload.max.files.per.upload'].defaultValue)}
                      onChange={(e) => this.updateFormField('fileupload.max.files.per.upload', e)}
                      min={0} max={99999}/>
                  )}
                </Form.Item>
              </Card>
              <br/>
              <Card title='Others'>
                <Form.Item {...formItemLayout} className='multi-rows-label'
                  label='TM Merge Fuzzy Bands (Experimental)'
                  extra={
                    <span>Lower percentage bound for each custom fuzzy band. eg "80 90"
                    will define the ranges 0-79, 80-89, 90-99, 100-100. <span
                        className='u-textWarning u-block'>Note that this feature and its
                    configuration are EXPERIMENTAL and subject to change without
                    notice.</span></span>
                  }>
                  {getFieldDecorator('tm.fuzzy.bands', {
                    initialValue: this.state['tm.fuzzy.bands'].value
                  })(
                    <Input placeholder='e.g. 80 90'
                      onChange={(e) => this.updateFormField('tm.fuzzy.bands', e.target.value)}/>
                  )}
                </Form.Item>
              </Card>
              <br/>
              <Form.Item>
                <Button aria-label='button'
                  className='u-sMR-1-4' type='primary'
                  loading={saving} disabled={!enableSaveButton}
                  onClick={this.handleSubmit} >
                  Save
                </Button>
                <Button aria-label='button' key='back'
                  className='btn-default' disabled={saving}
                  onClick={this.handleCancel} >
                  Cancel
                </Button>
              </Form.Item>
            </Form>
              }
          </Layout>
        </div>
    )
  }

  private handleSubmit = (e: FormEvent<any>) => {
    e.preventDefault()
    let validated = true
    this.props.form.validateFields((err: string) => {
      if (err) {
        validated = false
      }
    })

    if (validated) {
      this.props.handleSaveSettings(this.props.settings, this.state)
    }
  }

  private handleCancel = () => {
    this.resetFields()
    this.props.router.push('/admin/home')
  }

  private resetFields = () => {
    this.props.form.resetFields()
    this.setState(this.getDefaultState(this.props))
  }

  private updateFormField =
    (field: string, value: any) => {
    this.props.form.setFieldsValue({
      [field]: value
    })
    this.setState(prevState => ({
      ...prevState,
      [field]: {
        ...prevState[field],
        value
      }
    }))
  }

  private getDefaultState = (props: Props) => {
    const { settings } = props
    return {
      ['host.url']: getPropertyValue(settings, 'host.url'),
      ['register.url']: getPropertyValue(settings, 'register.url'),
      ['email.domain']: getPropertyValue(settings, 'email.domain'),
      ['email.admin.addr']: getPropertyValue(settings, 'email.admin.addr'),
      ['email.from.addr']: getPropertyValue(settings, 'email.from.addr'),
      ['terms.conditions.url']: getPropertyValue(settings, 'terms.conditions.url'),
      ['help.url']: getPropertyValue(settings, 'help.url'),
      ['piwik.url']: getPropertyValue(settings, 'piwik.url'),
      ['piwik.idSite']: getPropertyValue(settings, 'piwik.idSite'),
      ['log.email.active']: getPropertyValue(settings, 'log.email.active'),
      ['log.email.level']: getPropertyValue(settings, 'log.email.level'),
      ['log.destination.email']: getPropertyValue(settings, 'log.destination.email'),
      ['permitted.user.email.domain']: getPropertyValue(settings, 'permitted.user.email.domain'),
      ['gravatar.rating']: getPropertyValue(settings, 'gravatar.rating'),
      ['display.user.email']: getPropertyValue(settings, 'display.user.email'),
      ['allow.anonymous.user']: getPropertyValue(settings, 'allow.anonymous.user'),
      ['accept.translator.requests']: getPropertyValue(settings, 'accept.translator.requests'),
      ['max.concurrent.req.per.apikey']: getPropertyValue(settings, 'max.concurrent.req.per.apikey'),
      ['max.active.req.per.apikey']: getPropertyValue(settings, 'max.active.req.per.apikey'),
      ['fileupload.max.files.per.upload']: getPropertyValue(settings, 'fileupload.max.files.per.upload'),
      ['tm.fuzzy.bands']: getPropertyValue(settings, 'tm.fuzzy.bands')
    }
  }
}

function getPropertyValue(settings: SettingsState, key: string) {
  return {
    value: settings && settings[key] && settings[key].value,
    defaultValue: settings && settings[key] && settings[key].defaultValue
  }
}

function mapStateToProps(state: AdminState) {
  const { notification, serverSettings } = state.admin
  const { saving, loading, settings } = serverSettings
  return {
    saving,
    loading,
    settings,
    notification
  }
}

function mapDispatchToProps(dispatch: any) {
  return {
    fetchServerSettings: () => dispatch(fetchServerSettings()),
    handleSaveSettings: (prevSettings: SettingsState, newSettings: SettingsState) =>
      dispatch(handleSaveServerSettings(prevSettings, newSettings))
  }
}

export default connect(mapStateToProps, mapDispatchToProps)
  (Form.create({})(ServerSettings) as any)
