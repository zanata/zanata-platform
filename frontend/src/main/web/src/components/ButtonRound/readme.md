A rounded button. Should only be used for page interaction.

## Default

    <ButtonRound>Button Round</ButtonRound>

## Disabled

    <ButtonRound disabled>Button Round Disabled</ButtonRound>

## Types

    const types = require('../../constants/styles').types;
    <div>
      {types.map((type, i) =>
        <ButtonRound key={i} type={type} atomic={{m: 'Mend(rh)', tt: 'Tt(c)'}}>
          {type}
        </ButtonRound>
      )}
    </div>

## Sizes

    <div className='D(f) Ai(c)'>
      {['n1','0','1','2'].map((size, i) =>
        <ButtonRound key={i} size={size} atomic={{m: 'Mend(rh)'}}>
          Size&nbsp;{size}
        </ButtonRound>
      )}
    </div>
