import Button from 'antd/lib/button'
import 'antd/lib/button/style/css'
import Tooltip from 'antd/lib/tooltip'
import 'antd/lib/tooltip/style/css'
import React from 'react'

interface BACKEND_ATTR {
  href: string,
  img?: string,
  alt: string
}
interface BACKEND {
  [key: string]: BACKEND_ATTR
}

const SUPPORTED_BACKEND: BACKEND = {
  'GOOGLE': {
    href: 'https://translate.google.com/',
    img: 'http://zanata.org/images/translated-by-google-white-short.png',
    alt: 'Translated by Google'
  },
  'MS': {
    href: 'http://aka.ms/MicrosoftTranslatorAttribution',
    img: undefined,
    alt: 'Translated by Microsoft'
  }
}

const MTSuggestionsButton: React.StatelessComponent<{ backendId: string }> = ({
  backendId
}) => {
  const backend = SUPPORTED_BACKEND[backendId.toUpperCase()]
  const title = backend
    ? <a href={backend.href} target='_blank'>{backend.img ?
      <img src={backend.img} alt={backend.alt}/> :
      <label className='Label-MT'>{backend.alt}</label>}</a>
    : 'Translated by ' + backendId

  return (
    <span className='Button--MT'>
      <Tooltip placement='right' title={title}>
        <Button className='Button--snug u-roundish Button--neutral'>
          MT
        </Button>
      </Tooltip>
    </span>
  )
}

export default MTSuggestionsButton
