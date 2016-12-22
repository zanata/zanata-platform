var variables = require('zanata-ui/lib/constants/styles')

function pxToRem (pixels, baseFontSize) {
  baseFontSize = baseFontSize || '16'
  return +(pixels / baseFontSize).toFixed(3) + 'rem'
}

function flatten (target) {
  var output = {}

  function step (object, prev) {
    Object.keys(object).forEach(function (key) {
      var value = object[key]
      var type = Object.prototype.toString.call(value)
      var isobject = (
      type === '[object Object]' ||
        type === '[object Array]'
      )

      if (isobject && Object.keys(value).length) {
        return step(value, key)
      }

      output[key] = value
    })
  }

  step(target)

  return output
}

// Adding font-families here as sass doesn't like importing them from json.
// Also, we don't need them in sass anyway
var customVars = flatten(
  Object.assign(
    {},
    {
      zsans: "'Source Sans Pro', 'Helvetica Neue', Helvetica, Arial, sans-serif", // eslint-disable-line max-len
      zmono: "'Source Code Pro', Hack, Consolas, monaco, monospace"
    },
    variables
  )
)

module.exports = {
  cssDest: './dist/atomic.css',
  configs: {
    breakPoints: {
      oxsm: '@media screen and (max-width: ' + pxToRem(469) + ')',
      sm: '@media screen and (min-width: ' + pxToRem(470) + ')',
      lesm: '@media screen and (max-width: ' + pxToRem(879) + ')',
      md: '@media screen and (min-width: ' + pxToRem(880) + ')',
      lg: '@media screen and (min-width: ' + pxToRem(1200) + ')'
    },
    custom: customVars,
    classNames: [
      'Bgc(i)',
      'Bgc(i)!',
      'Bgc(#fff)',
      'C(dark)',
      'D(f)',
      'Ff(zsans)',
      'Ff(zmono)',
      'Flw(w)',
      'Flxg(1)',
      'Flxs(1)',
      'Fz(i)',
      'Fz(msn1)',
      'Fz(msn1)!',
      'Fz(ms3)',
      'Fz(16px)',
      'Fz(16px)!',
      'H(100%)',
      'H(msn2)',
      'H(msn1)',
      'H(ms0)',
      'H(ms1)',
      'H(ms2)',
      'H(ms3)',
      'H(ms4)',
      'H(ms5)',
      'H(ms6)',
      'Lh(1.5)',
      'M(0)',
      'M(rq)',
      'Maw(20em)',
      'Mstart(r1)',
      'Mih(maxc)',
      'Mih(100vh)',
      'Miw(r6)',
      'My(r2)',
      'Or(1)',
      'Or(0)--sm',
      'Ov(a)',
      'Ovx(h)',
      'Ovx(h)--lg',
      'Ovx(s)',
      'Ovs(touch)',
      'Ov(h)',
      'Pt(rq)',
      'Pt(0)!',
      'Tt(c)',
      'W(r6)'
    ]
  }
}
