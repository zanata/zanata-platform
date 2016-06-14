A button that looks like a link. Should only be used for page interaction,
if you need to link to a url, use the `<Link />` component.

## Default

    <ButtonLink>Button Link</ButtonLink>

## Types

    const types = require('../../constants/styles').types;
    <div>
      {types.map((type, i) =>
        <ButtonLink key={i} type={type} atomic={{m: 'Mend(rh)', tt: 'Tt(c)'}}>
          {type}
        </ButtonLink>
      )}
    </div>
