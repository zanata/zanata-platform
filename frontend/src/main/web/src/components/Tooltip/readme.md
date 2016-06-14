Note that Tooltip must only be used with an OverlayTrigger, as shown in these examples.

## Default

    const tooltip = (
      <Tooltip id='tt1'><strong>Holy guacamole!</strong> Check this info.</Tooltip>
    );
    <OverlayTrigger overlay={tooltip}>
      <Button>Hover for tooltip</Button>
    </OverlayTrigger>

## Inverse

    const tooltip = (
      <Tooltip id='tt2' inverse><strong>Holy guacamole!</strong> Check this info.</Tooltip>
    );
    <OverlayTrigger overlay={tooltip}>
      <Button>Hover for inverted tooltip</Button>
    </OverlayTrigger>

## Placement

    const tooltip = (
      <Tooltip id='tt3' placement='right'><strong>Holy guacamole!</strong> Check this info.</Tooltip>
    );
    <OverlayTrigger overlay={tooltip}>
      <Button>Hover for right positioned tooltip</Button>
    </OverlayTrigger>

## Alignment

    const tooltip = (
      <Tooltip id='tt4' alignment='left' title='Aligned Left'>
        <strong>Holy guacamole!</strong> Check this info.
      </Tooltip>
    );
    <OverlayTrigger overlay={tooltip}>
      <Button>Hover for left aligned tooltip</Button>
    </OverlayTrigger>

## Title

    const tooltip = (
      <Tooltip id='tt5' title='Look at me'><strong>Holy guacamole!</strong> Check this info.</Tooltip>
    );
    <OverlayTrigger overlay={tooltip}>
      <Button>Hover for tooltip with a title</Button>
    </OverlayTrigger>

## On Focus

    const tooltip = (
      <Tooltip id='tt6'>
        <strong>Holy guacamole!</strong> Check this clickable info.
      </Tooltip>
    );
    <OverlayTrigger trigger='focus' overlay={tooltip}>
      <Button>Click or focus for tooltip</Button>
    </OverlayTrigger>
