import React from 'react'
import { Checkbox } from 'react-bootstrap'

const ValidationOptions = React.createClass({

  render: function () {
    return (
        <div>
          <h2 className="validation">Validation options</h2>
          <ul>
            <li>
              <Checkbox checked>
                HTML/XML tags
              </Checkbox>
            </li>
            <li>
              <Checkbox checked>
                Java variables
              </Checkbox>
            </li>
            <li>
              <Checkbox checked>
                Leading/trailing newline (\n)
              </Checkbox>
            </li>
            <li>
              <Checkbox>
                Positional printf (XSI extension)
              </Checkbox>
            </li>
            <li>
              <Checkbox>
                Printf variables
              </Checkbox>
            </li>
            <li>
              <Checkbox checked>
                Tab characters (\t)
              </Checkbox>
            </li>
            <li>
              <Checkbox checked>
                XML entity reference
              </Checkbox>
            </li>
          </ul>
        </div>
    )
  }
  })

export default ValidationOptions

