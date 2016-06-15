## Basic

    const iconList = require('./list');
    <div className='D(f) Flw(w) Flxs(1) Flxg(1) W(100%)'>
      {iconList.map((iconName, i) => (
        <span className='D(ib) M(rq) P(rq) D(f) Fld(c) Ai(c) Ta(c) W(r6)'>
          <Icon key={i} name={iconName} size='3'/>
          <div className='Ff(zmono) C(muted) Fz(msn1) Mt(rq)'>{iconName}</div>
        </span>
      ))}
    </div>

## Size

With a size property. This is based of the modular scale 'n2', 'n1', '0'-'10'

    <Icon name='project' size='5'/>
