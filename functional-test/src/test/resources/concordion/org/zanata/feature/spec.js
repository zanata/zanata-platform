$(document).ready(function () {
    $('span').filter(function () {
        return $(this).attr('c:execute');
    }).each(function () {
            $(this).addClass("concordion-execute");
        });
});
$(document).ready(function () {
    $('span').filter(function () {
        return $(this).attr('c:set');
    }).each(function () {
            $(this).addClass("concordion-data");
        });
});