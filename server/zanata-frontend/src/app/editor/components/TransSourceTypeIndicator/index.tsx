import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Tooltip from 'antd/lib/tooltip'
import 'antd/lib/tooltip/style/css'
import React from 'react'

interface BACKENDATTR {
  href: string,
  img?: string,
  alt: string
}
interface BACKEND {
  [key: string]: BACKENDATTR
}

const SUPPORTED_MT_BACKEND: BACKEND = {
  GOOGLE: {
    href: 'https://translate.google.com/',
    img: 'http://zanata.org/images/translated-by-google-white-short.png',
    alt: 'Translated by Google'
  },
  MS: {
    href: 'http://aka.ms/MicrosoftTranslatorAttribution',
    alt: 'Translated by Microsoft'
  }
}

const TransSourceTypeIndicator: React.StatelessComponent<{ type: string, metadata: string }> = (
  {
    type,
    metadata
  }) => {
  if (type !== 'MT') {
    return null
  }
  const backend = SUPPORTED_MT_BACKEND[metadata.toUpperCase()]
  const title = backend
    ? <a href={backend.href} target='_blank'>{backend.img ?
      <img src={backend.img} alt={backend.alt}/> :
      <label className='Label-MT'>{backend.alt}</label>}</a>
    : 'Translated by ' + metadata

  return (
    <span className='Button--MT mh1'>
      <Tooltip placement='right' title={title}>
        <Button className='u-roundish Button--neutral' size={'small'}>
          <span className='f7'>MT</span>
        </Button>
      </Tooltip>
    </span>
  )
}

export default TransSourceTypeIndicator
