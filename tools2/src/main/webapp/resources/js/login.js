var ADT = ADT || {};
ADT.activate = function (elem) {
    $(elem).addClass('ui-state-active').removeClass('ui-state-default');
};
ADT.deactivate = function (elem) {
    $(elem).addClass('ui-state-default').removeClass('ui-state-active');
};

$(function () {
    "use strict";

    $('#j_username').focus();
    $('#login-btn').mouseenter(function () {
        $(this).mousedown(function () {
            ADT.activate(this);
        });
        $(this).mouseup(function () {
            ADT.deactivate(this);
        });
    });
    $('#login-btn').mouseleave(function () {
        ADT.deactivate(this);
    });

});