var adfonic_isIE6 = false;

function bindHighlight() {
  jQuery('.first,.second').live('mouseover', function(){
    jQuery(this).css('background-color','#ffffc9');
  }).live('mouseout', function(){
    jQuery(this).css('background-color','');
  });
}

function showAlertDiv(selector,divSelector) {
    var alertOpts = {
    className: "alert",
    cornerRadius: 0,
    orient: "above",
    width: 280,
    arrowHeight: 0,
    arrowInset: 0,
    nudgeHorizontal:-120,
    nudgeVertical:-13,
    region:'.adHome',
    content: divSelector
    }
    jQuery(selector).callout(alertOpts);
}

function showAlert(selector,header,text) {
   var str = '<table cellpadding="0" cellspacing="0" border="0">'
       + '<tr><td class="popupDiv1">' + header + '</td></tr>'
    + '<tr><td class="popupDiv2">' + text
    + '</td></tr>'
    + '<tr><td class="popupDiv3"></td></tr></table>';

    var alertOpts = {
    className: "alert",
    cornerRadius: 0,
    orient: "above",
    width: 280,
    arrowHeight: 0,
    arrowInset: 0,
    nudgeHorizontal:-120,
    nudgeVertical:-13,
    region:'.adHome',
    text: str
    }
    jQuery(selector).callout(alertOpts);
}

function bindCallout(selector,suffix,x,y,orientation) {
    if (!suffix) {
      suffix = '';
      x = -8;
      y = -3;
      orientation = "above";
    }

    var tooltipDefaults = {
    className: "tooltip",
    cornerRadius: 0,
    orient: orientation,
    width: 280,
    arrowHeight: 0,
    arrowInset: 0,
    nudgeHorizontal:x,
    nudgeVertical:y,
    region:'.adHome'
    }
  jQuery(selector).hover(function(){
    var header = jQuery('#'+this.id + '>.calloutHeader').text();
    var text = jQuery('#'+this.id + '>.calloutText').html();

    var str = '<table cellpadding="0" cellspacing="0" border="0">'
       + '<tr><td class="callout' + suffix + 'Div1">' + header + '</td></tr>'
    + '<tr><td class="calloutDiv2">' + text + '</td></tr>'
    + '<tr><td class="callout' + suffix + 'Div3"></td></tr></table>';
    jQuery(this).callout(jQuery.extend({}, tooltipDefaults, {
      text: str,
      align: "left"
    }));
    if (adfonic_isIE6) { jQuery('.tooltip').each(function() { ie6SelectHide(this, true); }); }
  }, function(){
    if (adfonic_isIE6) { jQuery('.tooltip').each(function() { ie6SelectHide(this, false); }); }
    jQuery(this).closeCallout();
  });
}

jQuery(document).ready(function(){
    bindCallout('.callout', '',-8,-3,"above");
    bindCallout('.calloutReverse', 'Reverse',-330,-3,"above");
    bindCallout('.calloutFlip', 'Flip',-246,6,"below");

});

function parseQuery ( query ) {
   var Params = new Object ();
   if ( ! query ) return Params;
   var Pairs = query.split(/[;&]/);
   for ( var i = 0; i < Pairs.length; i++ ) {
      var KeyVal = Pairs[i].split('=');
      if ( ! KeyVal || KeyVal.length != 2 ) continue;
      var key = unescape( KeyVal[0] );
      var val = unescape( KeyVal[1] );
      val = val.replace(/\+/g, ' ');
      Params[key] = val;
   }
   return Params;
}

// This function doesn't work if checkize is enabled.
function selectAll(id, value) {
  jQuery("input[id$='" + id + "']").attr("checked", value);
}

// Invokes the button with a (local) id matching 'action'.
function doAction(obj, action) {
  jQuery(obj.form).prepend('<input type="hidden" name="' + obj.form.id + ':' + action + '" value="" />');
  obj.form.submit();
}

// This has to go outside the function
var jsfAjaxNativeSubmit = new Object();
var jsfAjaxNativeSubmitAny = null;

function jsfAjaxInit(divId, url) {
  jQuery('#' + divId + ' form').each(function(i) {
    if (this.submit) {
        jsfAjaxNativeSubmit[this.id] = this.submit;
        jsfAjaxNativeSubmitAny = this.submit;
    } else {
    // Work around a strange issue in Chrome where this.submit is undefined on some forms, but not all... not sure why?
    jsfAjaxNativeSubmit[this.id] = jsfAjaxNativeSubmitAny;
    }
    this.submit = function() {
      if (jQuery('#' + divId + ' form input[type="hidden"][name$="_NO_AJAX"]').length > 0) {
      this.submit = jsfAjaxNativeSubmit[this.id];
      this.submit();
        return true;
      }

      jQuery('#' + divId).css('position','relative').append('<div class="loading"></div>');

      if (this.method == 'post') {
      jQuery('#' + divId).load(url + '?__ts=' + new Date().getTime(), jQuery(this).serializeArray(), function(data, textStatus) {
          jsfAjaxInit(divId, url);
        }); // load
      } else {
      jQuery.get(url + '?' + jQuery(this).serialize() + '&__ts=' + new Date().getTime(), null, function(data, textStatus) {
          jQuery('#' + divId).html(data);
          jsfAjaxInit(divId, url);
        }); // get
      }
      return false;
    }; // function
    jQuery(this).submit(this.submit);
  }); // each form
}

function jsfAjaxLoad(divId, url) {
    var useUrl = url + '?__ts=' + new Date().getTime();
    jQuery('#' + divId).load(useUrl, null, function() {
      jsfAjaxInit(divId, url);
    });
}

function jsfAjaxImageOver(idSuffix, url) {
    SI_MM_swapImage(jQuery('[id$="' + idSuffix + '"]')[0].id, '', url, 1);
}
function jsfAjaxImageOut() {
    SI_MM_swapImgRestore();
}

var countdown = {
    init: function() {
    countdown.remaining = countdown.max - jQuery(countdown.obj).val().length;
    if (countdown.remaining > countdown.max) {
        jQuery(countdown.obj).val(jQuery(countdown.obj).val().substring(0,countdown.max));
    }
    jQuery(countdown.obj).closest(".remainingBlock").find(".remaining").html(countdown.remaining + " characters remaining");
    if (countdown.remaining != countdown.max) {
        jQuery('#previewText').html(jQuery(countdown.obj).val());
        jQuery('#previewTextWithAppIcon').html(jQuery(countdown.obj).val());
    } else {
        jQuery('#previewText').text('');
        jQuery('#previewTextWithAppIcon').text('');
    }
    },
    max: null,
    remaining: null,
    obj: null
};

jQuery(function() {
jQuery(".countdown").each(function() {
        jQuery(this).focus(function() {
        var c = jQuery(this).attr("class");
        countdown.max = parseInt(c.match(/limit_[0-9]{1,}_/)[0].match(/[0-9]{1,}/)[0]);
        countdown.obj = this;
        iCount = setInterval(countdown.init,200);
        }).blur(function() {
            countdown.init();
            clearInterval(iCount);
        });
    // Set initial value
    var c = jQuery(this).attr("class");
    countdown.max = parseInt(c.match(/limit_[0-9]{1,}_/)[0].match(/[0-9]{1,}/)[0]);
    countdown.obj = this;
    countdown.init();
    });//each
});//jQuery

var ADT = ADT || {};

//Set watermark on the selectCheckboxMenu based on all/none or just some items were selected
ADT.toggleSelectCheckboxMenuWatermark = function (selectCheckboxMenuWidget, allWatermarkWidget, someWatermarkWidget) {
	var all = selectCheckboxMenuWidget.itemContainer.children('.ui-selectcheckboxmenu-item').length;		// number of all items
  var checked = selectCheckboxMenuWidget.itemContainer.children('.ui-selectcheckboxmenu-checked').length; // number of all selected items
  var watermark = '';																						// the watermark of the selectCheckboxMenu
  if(checked == 0 || all == checked) {
      watermark = allWatermarkWidget.cfg.value.replace('{0}', all);
  } else {
      watermark = someWatermarkWidget.cfg.value.replace('{0}', checked);
  }
  selectCheckboxMenuWidget.labelContainer.children().text(watermark);
};

ADT.validationArray = {};

ADT.storeValue = function(key, value) {
	ADT.validationArray[key] = value;
};
	
ADT.hasError = function() {
	var hasError = false;
	ADT.debug(ADT.validationArray, false);
	for (var key in ADT.validationArray) {
		if (!ADT.validationArray[key]) {
			return true;
		}
	}
	return false;
};

/* Show specific error message if an input is not match a criteria */
ADT.showErrorPreventSearch = function(elemId, minCharNum, minCharRequiredMsg, filterNumMsg, freeFilterNumMsg, numberRequiredMsg) {
	var inputElem = $('input[id$="'+ elemId +'Input"]');
	var messageElem = $('#' + elemId + 'Err');
	var inputLength = inputElem.val().length;
	
	// Invalid num of chars
	if (inputLength > 0 && inputLength < minCharNum) {
		ADT.showElementWithWarning(messageElem, ADT.format(minCharRequiredMsg, minCharNum));
		ADT.storeValue(elemId, false);
	} else if (numberRequiredMsg) {
		if(!inputElem.val().match(/^[0-9~]*$/)) {
			ADT.showElementWithWarning(messageElem, numberRequiredMsg);
			ADT.storeValue(elemId, false);
		} else {
			ADT.hideElement(messageElem);
			ADT.storeValue(elemId, true);
		}
	} else {
		ADT.hideElement(messageElem);
		ADT.storeValue(elemId, true);
	}
	
	// Minimum filled filters
	var filterMessageElem = $('#filterErr');
	var minFilterNum = 1;
	if (!ADT.satisfyMinFilterNum(minFilterNum)) {
		ADT.showElementWithWarning(filterMessageElem, ADT.format(filterNumMsg, minFilterNum));
		ADT.storeValue('filterErr', false);
	} else {
		ADT.hideElement(filterMessageElem);
		ADT.storeValue('filterErr', true);
	}
	
	// Minimum filled free filters
	var freeFilterMessageElem = $('#freeFilterErr');
	var minFreeFilterNum = 2;
	if (!ADT.satisfyMinFreeFilterNum(minFreeFilterNum)) {
		ADT.showElementWithWarning(freeFilterMessageElem, ADT.format(freeFilterNumMsg, minFreeFilterNum));
		ADT.storeValue('freeFilterErr', false);
	} else {
		ADT.hideElement(freeFilterMessageElem);
		ADT.storeValue('freeFilterErr', true);
	}
	
	// Disable / enable run query button
	var runQueryButtonWidget = PF('runQueryButtonWidget');
	if (ADT.hasError()) {
		runQueryButtonWidget.disable();
	} else {
		runQueryButtonWidget.enable();
	}
};

/* Check all the given publication free filters whether they reach the minimum */
ADT.satisfyMinFreeFilterNum = function(limit) {
	var allFreeInputs = $('input[id^="free"]');
	
	var filledFreeFilters = 0;
	for (var i = 0; i < allFreeInputs.length; i++) {
		filledFreeFilters += ($('#'+allFreeInputs[i].id).val()) ? 1 : 0;
	}

	return (filledFreeFilters < limit && filledFreeFilters > 0) ? false : true;
};

/* Check all the publication filters whether they reach the minimum */
ADT.satisfyMinFilterNum = function(limit) {
	var allInputs = $('input[id$="Input"]');
	
	var filledFilters = 0;
	for (var i = 0; i < allInputs.length; i++) {
		filledFilters += ($('#'+allInputs[i].id).val()) ? 1 : 0;
	}

	return (filledFilters < limit) ? false : true;
};

/* Save publication: If status has changed then comments is required */
ADT.commentRequiredWhenStatusHasChanged = function(
	commentErrId, commentInputWidget, saveButtonWidget, statusSelectWidget, originalSelectedStatus, commentRequiredMsg) {
	
	var commentErrElem = $('#' + commentErrId);
	// Comment is empty but status has changed
	if(commentInputWidget.jq.val().length == 0 && originalSelectedStatus != statusSelectWidget.getSelectedValue()) {
		// Show the hidden error with proper text
		ADT.showElementWithWarning(commentErrElem, ADT.format(commentRequiredMsg, originalSelectedStatus));
		//saveButtonWidget.disable();
		ADT.storeValue('commentRequired', false);
	} else {
		ADT.hideElement(commentErrElem);
		ADT.storeValue('commentRequired', true);
	}
	ADT.toggleSavePublicationButton(saveButtonWidget);
};

/* Save publication: If status has changed to Active then IAB is required */
ADT.iabRequiredWhenStatusChangedToActive = function(
	iabErrId, iabInputWidget, saveButtonWidget, statusSelectWidget, activeStatus, iabRequiredMsg) {
	var iabErrElem = $('#' + iabErrId);
	// IAB is empty and status is rejected
	if((iabInputWidget.currentItems[0].length == 0 || iabInputWidget.input.val().length == 0) && activeStatus == statusSelectWidget.getSelectedValue()) {
		// Show the hidden error with proper text
		ADT.showElementWithWarning(iabErrElem, ADT.format(iabRequiredMsg, activeStatus));
		ADT.storeValue('iabRequired', false);
	} else {
		// Hide error and allow the save button
		ADT.hideElement(iabErrElem);
		ADT.storeValue('iabRequired', true);
	}
	ADT.toggleSavePublicationButton(saveButtonWidget);
};

/** Track wheather the save publication button should be ddisabled/enabled */
ADT.toggleSavePublicationButton = function(saveButtonWidget) {
	if (ADT.hasError()) {
		saveButtonWidget.disable();
	} else {
		saveButtonWidget.enable();
	}
};

// Show loading icon on the right side of the command button after and remove the left icon
ADT.showLoadingIcon = function (commandButtonWidget, existingIconClass) {
	// prevent the user clicking until the action is completed
	commandButtonWidget.disable();
	
	// apply the right side icon related style
	commandButtonWidget.jq.addClass('ui-button-text-icon-right');
	
	// create span for holding the right loading icon
	commandButtonWidget.jq.append('<span></span>');
	
	// apply styles for displaying the loading icon on the right
	commandButtonWidget.jq.children().last().addClass('ui-button-icon-right ui-icon ui-c ui-icon-loading');			
	
	// remove existing left icon
	commandButtonWidget.jq.children('.' + existingIconClass + '').remove();							
};

// Hide loading icon from the right side of the command button and add back the left icon
ADT.hideLoadingIcon = function (commandButtonWidget, existingIconClass) {
	// allow the button to be clickable again
	commandButtonWidget.enable();
	
	// remove right icon related class
	if (commandButtonWidget.jq.hasClass('ui-button-text-icon-right')) {
		commandButtonWidget.jq.removeClass('ui-button-text-icon-right');
	}
	
	// delete the span with the loading icon
	if (commandButtonWidget.jq.children('.ui-icon-loading').length > 0) {
		commandButtonWidget.jq.children().last().remove();				
	}
	// create span for holding the left icon
	commandButtonWidget.jq.append('<span></span>');
	
	// apply styles for displaying the left icon on the right
	commandButtonWidget.jq.children().last().addClass('ui-button-icon-left ui-icon ui-c ' + existingIconClass);		
};

/* Display element with warning class */
ADT.showElementWithWarning = function(messageElem, message) {
	messageElem.removeClass('hide').addClass('warning').text(message);
};

/* Set button text with parameters */
ADT.setButtonText = function(commandButton, text, params) {
	commandButton.jq.children('.ui-button-text').text(ADT.format(text, params[0], params[1]));
};

/* Hide element */
ADT.hideElement = function(messageElem) {
	messageElem.addClass('hide');
};

// Format string based on placeholders
// Usage:	var text = "{0} plus {1} is {2}";
//			text.format(input, 1, 4, 5); // -> 1 plus 4 is 5
ADT.format = function(input) {
    var formattedInput = input;
    if (arguments.length > 1) {
        for (var i = 1; i < arguments.length; i++) {
            formattedInput = formattedInput.replace(new RegExp("\\{"+(i-1)+"\\}", "g"), arguments[i]);
        }
    }
    return formattedInput;
};

/* Simple debug message if enabled */
ADT.debug = function(message, enabled) {
	if(enabled) console.log(message);
}; 