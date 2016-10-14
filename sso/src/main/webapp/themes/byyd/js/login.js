var ADT = {};

ADT.validator = function (formSelector) {
    var i,
        validations = [],
        formObj = $(formSelector),
        regexpObj = {
            email: /^\s*[\w\-\+_]+(\.[\w\-\+_]+)*\@[\w\-\+_]+\.[\w\-\+_]+(\.[\w\-\+_]+)*\s*$/,
            notempty: /.+/,
            notnumempty: /\D+/
        },
        runValidators = function (validatorsArray) {
            var formElem,
                formElem2,
                status = true,
                fieldsToCompare,
                paramString,
                method,
                displayError = function () {
                    $('#' + validatorsArray[i].errorContainerId).show().find('.ui-message-error-detail').html(validatorsArray[i].errorMessage);
                    status = false;
                };

            $('.error').hide();

            for (i in validatorsArray) {
                method = validatorsArray[i].validateMethod;
                if (method === 'compare') {
                    fieldsToCompare = validatorsArray[i].formFieldName.split(' ');
                    formElem = formObj.find('[name="' + fieldsToCompare[0] + '"]');
                    formElem2 = formObj.find('[name="' + fieldsToCompare[1] + '"]');
                    if (formElem.val() !== formElem2.val()) {
                        displayError();
                    }
                } else {
                    formElem = formObj.find('[name="' + validatorsArray[i].formFieldName + '"]');
                    if (method === 'checked') {
                        if (!formElem.is(':checked')) {
                            displayError();
                        }
                    } else if (/^not-/.test(method)) {
                        paramString = method.slice(4);
                        if (formElem.val() === paramString) {
                            displayError();
                        }
                    } else if (/^min-/.test(method)) {
                        paramString = method.slice(4);
                        if (formElem.val().length < +paramString) {
                            displayError();
                        }
                    } else {
                        if (regexpObj[method].test($.trim(formElem.val())) === false) {
                            displayError();
                        }
                    }
                }
            }
            return status;
        };

    formObj.find('input[type="submit"]').click(function () {
        return runValidators(validations);
    });

    return {
        addValidation: function (formFieldName, validationMethod, errorContainerId, message) {
            validations.push({
                formFieldName: formFieldName,
                validateMethod: validationMethod,
                errorContainerId: errorContainerId,
                errorMessage: message
            });
        },
        validate: function () {
            return runValidators(validations);
        },
        getValidations: function () {
            return validations;
        }
    }
};

(function( $ ){
    $.fn.chkbox = function() {
        var chxInner = this.find('.ui-chkbox-box'),
            chxInput = this.find('input[type="checkbox"]'),
            chxIcon = this.find('span.ui-chkbox-icon');
        chxInput.focus(function () {
            chxInner.addClass('ui-state-focus')
        });
        chxInput.blur(function () {
            chxInner.removeClass('ui-state-focus')
        });
        this.click(function () {
            if (chxInner.hasClass('ui-state-active')) {
                chxInner.removeClass('ui-state-active')
                chxIcon.removeClass('ui-icon ui-icon-check');
                chxInput.val('false');
            } else {
                chxInner.addClass('ui-state-active');
                chxIcon.addClass('ui-icon ui-icon-check');
                chxInput.val('true');
            }
        });
    };
})( jQuery );

(function( $ ){
    $.fn.menubar = function() {
        this.find('.ui-menu-parent').hover(function () {
            $('.ui-menu-child').show();
        },
        function () {
            $('.ui-menu-child').hide();
        });
    };
})( jQuery );

$(function(){
    $('#login-btn').button();
    $('#remember_me').chkbox();
    $('#sub-menu').menubar();
    ADT.loginValidate = ADT.validator('#sso-form');
});

