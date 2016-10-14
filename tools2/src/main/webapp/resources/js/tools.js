var ADT = ADT || {};

// this method is being used as a wrapper for a callback functions for click events
// it prevents the cilck from happening twice (as it happens with primefaces)
ADT.preventDbClick = function (func, that) {
    "use strict";
    if (!$(that).data('clicked')) {
        $(that).data('clicked', true);
        setTimeout(function() {
            $(that).data('clicked', false);
        }, 40);
        func(that);
    }
};

ADT.stopBuble = function(e) {
    "use strict";

    e.cancelBubble = true;
    if (e.stopPropagation) {
        e.stopPropagation();
    }
};

// function extending out of the box primefaces multiple radio and checkbox widgets
// allows to expand/enable child panel for each item
// first parameter - are we extending radio or checkbox group, second - class name of the group
// third parameter - when true - showing/hiding replaced with disabling/enabling  for panels
ADT.subElExpander = function (radioOrCheck, elementClass, noShowHide) {
    "use strict";
    var elArr, contArr, activeItem, activeItems, index, i, panel, disable, showHideRadio, showHideCheck, disableContainer,
        radio, check, interval;

    elArr = $('table.' + elementClass + ' tbody > tr');
    contArr = $('.panel-' + elementClass);
    activeItem = false;
    activeItems = [];
    index = 0;

    showHideRadio = function (that, speed){
        var parent = $(that).parent().parent(),
            animSpeed = speed !== undefined ? speed : 'slow';
        if (!parent.hasClass('open')) {
            parent.parent().find(' > .open').each(function () {
                $(this).removeClass('open');
            });
            parent.addClass('open');
            contArr.each(function () {
                $(this).slideUp(animSpeed);
            });
            $('.panel-' + elementClass + '[data-no="' + (parent.index() + 1) + '"]').slideDown(animSpeed);
        }
    };

    showHideCheck = function (that, speed){
        var parent = $(that).parent().parent(),
            animSpeed = speed !== undefined ? speed : 'slow',
            panel = $('.panel-' + elementClass + '[data-no="' + (parent.index() + 1) + '"]');
        if (!parent.hasClass('open')) {
            panel.slideDown(animSpeed);
            parent.addClass('open');
        } else {
            panel.slideUp(animSpeed);
            parent.removeClass('open');
        }
    };

    disableContainer = function (that) {
        var parent = $(that).parent().parent();
        if (!parent.hasClass('open')) {
            parent.parent().find(' > .open').each(function () {
                $(this).removeClass('open');
                $('.panel-' + elementClass + '[data-no="' + ($(this).index() + 1) + '"]')
                    .css('opacity', 0.3).prepend('<div class="blocker"></div>');
            });
            parent.addClass('open');
            $('.panel-' + elementClass + '[data-no="' + (parent.index() + 1) + '"]')
                .css('opacity', 1).find('.blocker').remove();
        }
    };

    radio = function() {
        if (noShowHide) {
            $('.panel-' + elementClass).each(function () {
                $(this).css('opacity', 0.3).prepend('<div class="blocker"></div>');
            });
        }

        elArr.find(' > td:first-child > .ui-radiobutton').click(function() {
        	if (!noShowHide && !($(this).has(".ui-state-disabled").length > 0)) {
                ADT.preventDbClick(showHideRadio, this);
            } else if(!($(this).has(".ui-state-disabled").length > 0)) {
                ADT.preventDbClick(disableContainer, this);
            }
        });
    };

    check = function() {
        elArr.find(' > td:first-child > .ui-chkbox').click(function(e) {
            if (!noShowHide) {
                ADT.preventDbClick(showHideCheck, this);
            }
        });
    };

    //interval checks every 200 milliseconds if target element has been populated
    //if it has been populated, we can run the functionality and stop the interval
    interval = setInterval(function () {
        if ($('.' + elementClass + '.ui-selectoneradio').html() !== null ||
            $('.' + elementClass + '.ui-selectmanycheckbox').html() !== null) {
            clearInterval(interval);

            contArr.each(function () {
                $(this).detach().appendTo($(elArr.get($(this).data('no') - 1)).find('td:last-child'));
                $(elArr.get($(this).data('no') - 1)).css('vertical-align', 'top');
            });

            if (radioOrCheck === 'radio') {
                radio();
                elArr.each(function () {
                    if ($(this).find('> td:first-child div.ui-radiobutton-box').hasClass('ui-state-active')) {
                        activeItem = index;
                    }
                    index += 1;
                });

                if (activeItem !== false) {
                    $(elArr[activeItem]).addClass('open');
                    panel = $('.panel-' + elementClass + '[data-no="' + (activeItem + 1) + '"]');
                    if (noShowHide) {
                        panel.css('opacity', 1).find('.blocker').remove();
                    } else {
                        panel.show();
                    }
                }

            } else if (radioOrCheck === 'check') {
                check();
                elArr.each(function () {
                    if ($(this).find('> td:first-child div.ui-chkbox-box').hasClass('ui-state-active')) {
                        activeItems[activeItems.length] = index;
                    }
                    index += 1;
                });

                if (activeItems !== false) {
                    for (i = 0; i < activeItems.length; i += 1) {
                        $(elArr[activeItems[i]]).addClass('open');
                        panel = $('.panel-' + elementClass + '[data-no="' + (activeItems[i] + 1) + '"]');
                        if (noShowHide) {
                            panel.css('opacity', 1).find('.blocker').remove();
                        } else {
                            panel.show();
                        }
                    }
                }
            }
        }
    }, 200);
};

ADT.dualSlider = function (selector) {
    "use strict";
    $(selector).slider({
        animate: true,
        range: true,
        min: 0,
        max: 75,
        values: [ $( ".date-1" ).val(), $( ".date-2" ).val() ],
        slide: function( event, ui ) {
            $( ".date-1" ).val(ui.values[ 0 ]);
            $( ".date-2" ).val(ui.values[ 1 ]);
        }
    });
};

ADT.genderRangeExpander = function () {
    "use strict";
    $('.gender-1').val( 100 - $('.gender-2').val() );
};

// displays, updates time range grid on scheduling page (create campaign).
ADT.timeRangeFields = function (selector, testingMode) {
    "use strict";

    // When clicked by mouse, the checkbox changes it checked attribute before the click event is triggered.
    // When triggered programatically, the checkbox changes it checked attribute after the event is triggered.
    // That's why testingMode parameter has been implemented (to be used when testing functionality with Jasmine).
    jQuery.fn.checkboxState = function(executedAfterClick) {
        if (this.is(':checked') || this.find(' > div.ui-chkbox-box').hasClass('ui-state-active')) {
            if (testingMode) { return true; }
            return (executedAfterClick) ? false : true;
        } else {
            if (testingMode) { return false; }
            return (executedAfterClick) ? true : false;
        }
    };

    $(selector + ' .row-boxes > div').each(function () {
        if ($(selector + ' .x' + $(this).data('x')).checkboxState() && $(selector + ' .y' + $(this).data('y')).checkboxState()) {
            $(this).addClass('on');
        }
    });

    $(selector).delegate('.day', 'click', function () {
        var y = $(this).parent().data('no');

        if ($(this).checkboxState(true)) {
            $(selector + ' div[data-y="' + y + '"]').each(function () {
                $(this).attr('class', 'off');
            });
        } else {
            $(selector + ' div[data-y="' + y + '"]').each(function () {
                if ($(selector + ' .x' + $(this).data('x')).checkboxState()) {
                    $(this).attr('class', 'on');
                }
            });
        }
    });

    $(selector).delegate('.hour', 'click', function () {
        var x = $(this).parent().data('no');
        if ($(this).checkboxState(true)) {
            $(selector + ' div[data-x="' + x + '"]').each(function () {
                $(this).attr('class', 'off');
            });
        } else {
            $(selector + ' div[data-x="' + x + '"]').each(function () {
                if ($(selector + ' .y' + $(this).data('y')).checkboxState()) {
                    $(this).attr('class', 'on');
                }
            });
        }
    });
};

ADT.runDateTimeRange = function () {
    "use strict";
    var timer = 0, interval;

    interval = setInterval(function () {
        if (timer > 40) { return; }
        if ($('#time-range-boxes').html() !== null && $('#time-range-boxes-weekend').html() !== null) {
            clearInterval(interval);
            ADT.timeRangeFields('#time-range-boxes');
            ADT.timeRangeFields('#time-range-boxes-weekend');

        }
        timer +=1;
    }, 200);
};

ADT.showHide = function (triggerEl, toShowSel, toHideSel) {
    "use strict";
    $(triggerEl).click(function () {
        $(toShowSel).slideDown();
        $(toHideSel).slideUp();
    });
};

//custom accordion
ADT.accordion = function (container) {
    "use strict";
    function animateAcc(that) {
        var header = $(that);

        if (!header.hasClass('on')) {
            header.addClass('on').next('.acc-content').slideDown();
        } else {
            header.next('.acc-content').slideUp(function () {
                header.removeClass('on');
            });
        }
    }

    $(container).delegate('.acc-head .exec', 'click', function () {
        ADT.preventDbClick(animateAcc, $(this).parent());
    });
};

//accordion for creatives
ADT.crAccordion = function (container, statusIndicator) {
    "use strict";

    $(container).delegate('.acc-head', 'click', function () {

        if ($(statusIndicator).val() === 'CLOSED') {

            $('#submitIndex').val($(this).attr('class').replace(/.*no(\d+).*/, '$1'));
            $(this).slideUp().next().slideDown();
            $(statusIndicator).val('OPENED');

            try {
                //function defined by primefaces
                openPanel();
            } catch(e) {
                // do nothing
            }
        }
    });
};

ADT.blockInput = function(selector) {
    "use strict";
    $(selector).find('input').each(function () {
        $(this).focus(function () {
            $(this).blur();
        });
        $(this).keydown(function () {
            $(this).blur();
        });
    });
};

//char counter - fourth attr -> max range - when countRange exceded - count drops below 0 and turns red
ADT.charCount = function(inputSel, countSel, countRange, maxRange) {
    "use strict";

    var timeout,
        inputField = $(inputSel),
        counterContainer = $(countSel),
        body = $('body'),
        count = function() {
            if (inputField.val().length > maxRange) {
                inputField.val(inputField.val().slice(0, maxRange));
            }
            if (inputField.val().length > countRange) {
                counterContainer.parent().addClass('error');
            } else {
                counterContainer.parent().removeClass('error');
            }
            counterContainer.text(countRange - inputField.val().length);
        };

    count();

    body.delegate(inputSel, 'keydown', function () {
        count();
    });
    body.delegate(inputSel, 'focus', function () {
        timeout = setInterval(function () {
            count();
        }, 200);
    });
    body.delegate(inputSel, 'blur', function () {
        count();
        clearInterval(timeout);
    });
};

//fixed/floated columns functionality
ADT.fixedPanel = function (headerHeight) {
    "use strict";

    var navPanel = {},
        dataPanel =  {},
        windowObj = {},
        marginTop,
        fixedToBottom = false,
        applyMargin;

    applyMargin = function (margin, targetObj) {
        if (margin > 0) {
            targetObj.jqObj.css('margin-top', margin);
        } else {
            targetObj.jqObj.css('margin-top', 0);
        }
    };

    navPanel.jqObj = $('#side-panel');
    dataPanel.jqObj = $('#navigationDiv');
    navPanel.height = navPanel.jqObj.height();
    dataPanel.height = dataPanel.jqObj.height();

    $('.content').css('margin-top', (headerHeight + 40));

    $(document).scroll(function () {

        windowObj.vertSpace = $(window).height() - (headerHeight + 20);  // minus height of the header + footer
        windowObj.scrollUp = $(window).scrollTop() < windowObj.offsetTop;
        windowObj.offsetTop = $(window).scrollTop();
        windowObj.top = windowObj.offsetTop + headerHeight;
        windowObj.bottom = $(window).height() + windowObj.offsetTop - 20; // 20 -> footer height

        navPanel.jqObj = $('#side-panel');
        dataPanel.jqObj = $('#navigationDiv');

        navPanel.sizeChanged = false;
        dataPanel.sizeChanged = false;

        //check if size of nav col have been changed
        if (navPanel.height !== navPanel.jqObj.height()) {
            navPanel.sizeChanged = true;
            fixedToBottom = false;
            //if nav panel size has been decreased
            if (navPanel.jqObj.height() < navPanel.height) {
                //move up data panel - that amount higher
                marginTop = parseInt(dataPanel.jqObj.css('margin-top'), 10) - (navPanel.height - navPanel.jqObj.height());
                applyMargin(marginTop, dataPanel);
            }
        }

        //check if size of data column have been changed
        if (dataPanel.height !== dataPanel.jqObj.height()) {
            dataPanel.sizeChanged = true;
            fixedToBottom = false;
            //if data panel size has been decreased
            if (dataPanel.jqObj.height() < dataPanel.height) {
                //move up nav panel - that amount higher
                marginTop = parseInt(navPanel.jqObj.css('margin-top'), 10) - (dataPanel.height - dataPanel.jqObj.height());
                applyMargin(marginTop, navPanel);
            }
        }

        navPanel.height = navPanel.jqObj.height();
        navPanel.top = parseInt(navPanel.jqObj.css('margin-top') ,10) + (headerHeight + 40); // + content margin top
        navPanel.bottom = navPanel.height + parseInt(navPanel.jqObj.css('margin-top') ,10) + (headerHeight + 40); // + content margin top
        dataPanel.height = dataPanel.jqObj.height();
        dataPanel.top = parseInt(dataPanel.jqObj.css('margin-top') ,10) + (headerHeight + 40); // + content margin top
        dataPanel.bottom = dataPanel.height + parseInt(dataPanel.jqObj.css('margin-top') ,10) + (headerHeight + 40); // + content margin top

        if (dataPanel.sizeChanged || navPanel.sizeChanged) { return; }

        //if left and right panel is bigger than content area
        if ((navPanel.height + 80) > windowObj.vertSpace && (dataPanel.height + 80) > windowObj.vertSpace) { // plus top and bottom space -> 80

            //if going down and user didn't reach the end of the page
            if (!windowObj.scrollUp  && navPanel.bottom !== dataPanel.bottom) {

                //if data is not fixed to the page bottom and bottom of nav column is more than 40 px above footer
                if (fixedToBottom !== 'data' && windowObj.bottom - navPanel.bottom > 40) {

                    if (navPanel.bottom < dataPanel.bottom) {
                        // fix it to the bottom
                        navPanel.jqObj.css('margin-top', ((windowObj.bottom - 40) - (headerHeight + 40)) - navPanel.height);
                        //flag it
                        fixedToBottom = 'nav';
                        // disable floating on data column
                        dataPanel.jqObj.removeAttr('style');
                        dataPanel.floated = false;
                    }

                    //if nav is not fixed to the page bottom and bottom of data column is more than 40 px above footer
                } else if (fixedToBottom !== 'nav' && windowObj.bottom - dataPanel.bottom > 40) {

                    if (navPanel.bottom > dataPanel.bottom) {
                        //fix it to the bottom
                        dataPanel.jqObj.css('margin-top', ((windowObj.bottom - 40) - (headerHeight + 40)) - dataPanel.height);
                        //flag it
                        fixedToBottom = 'data';
                        //disable floating on nav column
                        navPanel.jqObj.removeAttr('style');
                        navPanel.floated = false;
                    }
                }

                //if scrolling up
            } else if (windowObj.scrollUp) {

                //if nav fixed to page bottom and top of nav col is more than 40px below from header OR
                //if no cols fixed to the bottom and top of nav col is more than 40px below from header
                if ((fixedToBottom === "nav" && navPanel.top - windowObj.top > 40) || (navPanel.top - windowObj.top > 40)){
                    navPanel.floated = true;
                    marginTop = $(window).scrollTop();
                    applyMargin(marginTop, navPanel);
                    fixedToBottom = false;

                    //if data fixed to page bottom and top of data col is more than 40px below from header OR
                    //if no cols fixed to the bottom and top of data col is more than 40px below from header
                } else if ((fixedToBottom === "data" && dataPanel.top - windowObj.top > 40) || (dataPanel.top - windowObj.top > 40)){
                    dataPanel.floated = true;
                    marginTop = $(window).scrollTop();
                    applyMargin(marginTop, dataPanel);
                    fixedToBottom = false;
                }
            }

            // left or right panel is smaller than content area
        }else{
            // right col is longer than left col
            if(dataPanel.height > navPanel.height){
                marginTop = $(window).scrollTop();
                applyMargin(marginTop, navPanel);
            }else{
                marginTop = $(window).scrollTop();
                applyMargin(marginTop, dataPanel);
            }
        }
    });
};

// helper for fixedPanel method - "scrolls" the page to top
ADT.panelsTop = function () {
    "use strict";

    $('#navigationDiv').removeAttr('style');
    $('#side-panel').removeAttr('style');
    window.scrollTo(0, 0);
};

//for creative upload on create campaign
ADT.fileUploadMod = function(){	
    $("body").on("click", ".file.upload-button.CLOSED", function(){
		$(this).siblings().
			find('input[type="file"]').
			trigger('click');
    });
    
    $("body").on("click", ".trigger.upload-button.CLOSED", function(){
		$(this).siblings('button').
			trigger('click');
    });     
};

//status images popup - dashboard datatable
ADT.statusPopup = function () {
    $('body').on("mouseenter mouseleave", "td.tooltip", function() {
    	var $tooltip = $(".ui-overlaypanel", this)
    	if(String($.trim($("span", $tooltip).text())).length){
    		$tooltip.toggle();
    	}
    });
};

// Show hide watermark on auto complete based on the selected contents
ADT.toggleAutoCompleteWatermark = function (autoCompleteWidget, watermarkWidget) {
	autoCompleteWidget.input.attr('placeholder', (autoCompleteWidget.hinput.children().length == 0) ? watermarkWidget.cfg.value : '');
};

// Show hide clear device model link based on the selected contents in autocomplete
ADT.toggleClearDeviceModelsLink = function (autoCompleteWidget) {
	var clearDeviceModelsLink = $('a[id$="clearDeviceModelsLink"]'); // targeting device page has prefix on ids
	if(autoCompleteWidget.hinput.children().length <= 1) {
		clearDeviceModelsLink.hide();
	} else {
		clearDeviceModelsLink.show();
	}
};

// Show loading icon on the right side of the command button
ADT.showLoadingIcon = function (commandButtonWidget) {
	commandButtonWidget.disable();										// prevent the user clicking until the action is completed
	commandButtonWidget.jq.removeClass('ui-button-text-only');			// remove button text only style
	commandButtonWidget.jq.addClass('ui-button-text-icon-right');		// apply the right side icon related style
	commandButtonWidget.jq.append('<span></span>');						// create span for holding the icon
	commandButtonWidget.jq.children().last().addClass(
		'ui-button-icon-right ui-icon ui-c ui-icon-loading');			// apply styles for displaying the icon on the right
};

// Hide loading icon from the right side of the command button
ADT.hideLoadingIcon = function (commandButtonWidget) {
	commandButtonWidget.enable();										// allow the button to be clickable again
	if (!commandButtonWidget.jq.hasClass('ui-button-text-only')) {
		commandButtonWidget.jq.addClass('ui-button-text-only');			// apply button text only style again
	}
	if (commandButtonWidget.jq.hasClass('ui-button-text-icon-right')) {
		commandButtonWidget.jq.removeClass('ui-button-text-icon-right');// remove icon related class
	}
	if (commandButtonWidget.jq.children('.ui-icon-loading').length > 0) {
		commandButtonWidget.jq.children().last().remove();				// delete the span with the icon
	}
};

// Set watermark on the selectCheckboxMenu based on all/none or just some items were selected
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

/* Limit the num of selected buttons in case of campaign hourly chart */
ADT.limitSelectedManyButtons = function (event, selectManyButtonWidget, limit) {
    var checkedCount = selectManyButtonWidget.jq.find(':checkbox:checked').length;
     
    // Remove one already checked
    if (checkedCount > limit) {
        var firstChecked = selectManyButtonWidget.jq.find(':checkbox:checked:first');
        if (firstChecked.val() == event.target.value) {                    // if the current selected one is the first then uncheck the last
            firstChecked = selectManyButtonWidget.jq.find(':checkbox:checked:last');
        }
        firstChecked.attr("checked", false);                               // uncheck the first selected
        firstChecked.parent().removeClass("ui-state-active");              // remove checked state
    }
};

/* Update UI for recency */
ADT.updateAudienceRecencySelect = function() {
	for(var i = 0; i < $('tr[id$="recencyRangeRow"]').length; i++) {
		ADT.onSegmentChange(i);
	}
};

/* Perform segment change */
ADT.onSegmentChange = function (id) {
	var debug = false;
	var idx = id !== undefined ? id : '';
	var recencySelectWidget = PF('recencySelectWidget' + idx);
	var segmentSelectWidget = PF('segmentSelectWidget' + idx);
	if (recencySelectWidget !== undefined && segmentSelectWidget !== undefined) {
		var selectedAudienceType = segmentSelectWidget.getActiveItem().attr('title');
		ADT.debug("Selected audience type: " + selectedAudienceType, debug);
		
		ADT.enableSettingsForLocationOnly(idx, selectedAudienceType);
		ADT.enableRecencySelectForCampaignEventOnly(selectedAudienceType, recencySelectWidget);
		ADT.toggleRecencyRangeAndWindowRow(idx);
	}
};

/* Disable or enable location audience settings */
ADT.enableSettingsForLocationOnly = function (id, selectedAudienceType) {
	var idx = id !== undefined ? id : '';
	var locationSettingsRow = $('tr[id$="' + idx +':locationSettingsRow"]');
	
	// Show settings only in case of selected Location segments 
	if(selectedAudienceType === "LOCATION") {
		locationSettingsRow.show();
	} else {
		locationSettingsRow.hide();
	}
};

/* Disable or enable recency lookback select options */
ADT.enableRecencySelectForCampaignEventOnly = function (selectedAudienceType, recencySelectWidget) {
	var debug = false;
	var disabledNA = recencySelectWidget.items.filter(".ui-state-disabled");
	
	// Disable Recency lookback options if not CAMPAIGN_EVENT - show greyed out box with N/A
	if(selectedAudienceType !== "CAMPAIGN_EVENT") {	
    	ADT.debug("Disable recency with N/A", debug);
    	disabledNA.show();
		recencySelectWidget.selectValue(recencySelectWidget.options[0].value); // N/A option
		recencySelectWidget.disable();
	} else {		
		ADT.debug("Enable recency", debug);
		disabledNA.hide();
		if (recencySelectWidget.getSelectedValue() == null) {
			ADT.debug("... with None", debug);
			recencySelectWidget.selectValue(recencySelectWidget.options[1].value); // None option
		}
		recencySelectWidget.enable();
	}
};

/* Hide or show recency window and range based on the selected recency lookback option */
ADT.toggleRecencyRangeAndWindowRow = function (id) {
	var debug = false;
	var idx = id !== undefined ? id : '';
	var selectedRecencyType = PF('recencySelectWidget' + idx).getActiveItem().attr('title');
	var recencyRangeRow = $('tr[id$="' + idx +':recencyRangeRow"]'); // targeting audience page has prefix on ids
	var recencyWindowHeaderRow = $('tr[id$="' + idx +':recencyWindowHeaderRow"]');
	var recencyWindowSliderRow = $('tr[id$="' + idx +':recencyWindowSliderRow"]');

	ADT.debug("Lookback window: " + selectedRecencyType, debug);
	switch(selectedRecencyType) {
		case "RANGE":
			ADT.debug("Hide window show range", debug);
			PF('endDateCalendar' + idx).input.attr('readonly', 'readonly');
			PF('startDateCalendar' + idx).input.attr('readonly', 'readonly');
			recencyRangeRow.show();
			recencyWindowHeaderRow.hide(); recencyWindowSliderRow.hide();
	        break;
	    case "WINDOW":
	    	ADT.debug("Hide range show window", debug);
	    	recencyRangeRow.hide();
	    	recencyWindowHeaderRow.show(); recencyWindowSliderRow.show();
	        break;
	    case "NA":
	    case "NONE":
	    default:
	    	ADT.debug("Hide both window and range", debug);
	    	recencyRangeRow.hide();
	    	recencyWindowHeaderRow.hide(); recencyWindowSliderRow.hide();
	        break;
	}
};

ADT.insertStatIcons = function () {
    "use strict";

    var statusObj, label, dType;

    statusObj = {
        'All': 'All',
        'Active': 'ACTIVE',
        'Paused': 'PAUSED',
        'Pending to Active': 'PENDING',
        'Pending to Paused': 'PENDING_PAUSED',
        'Incomplete': 'NEW',
        'New': 'NEW_REVIEW',
        'Completed': 'COMPLETED',
        'Stopped': 'STOPPED',
        'Rejected': 'REJECTED',
        'Inactive': 'STOPPED'	
    };

    if ($('#dataTable\\:campaignStatusFilter_label').length) {
        dType = 'campaign';
    } else if ($('#dataTable\\:publicationStatusFilter_label').length) {
        dType = 'publication';
    } else if ($('#dataTable\\:agencyStatusFilter_label').length) {
        dType = 'agency';
    }
    // Didn't want this variable in global space, so I'm localising it.
    var label = $('#dataTable\\:' + dType + 'StatusFilter_label');
    
    $("body").on("click", function(){   
		if(label.text() !== 'All' && !$(".status-filter .status").length) {
	  	    statusObj = {
	           'All': 'All',
	           'Active': 'ACTIVE',
	           'Paused': 'PAUSED',
	           'Pending to Active': 'PENDING',
	           'Pending to Paused': 'PENDING_PAUSED',
	           'Incomplete': 'NEW',
	           'New': 'NEW_REVIEW',
	           'Completed': 'COMPLETED',
	           'Stopped': 'STOPPED',
	           'Rejected': 'REJECTED',
	           'Inactive': 'STOPPED'	
	       };
			label.addClass('selected-label').html('<div class="status ' + statusObj[label.text()] + '"></div>');
 		} 
	});    

    //fuction being used on complete - primefaces attribute (before, the whole function has been used)
    //now whole function gets executed only once, and then we use the one below to update
    ADT.updateStatusIcons = function () {
    	// Didn't want this variable in global space, so I'm localising it.
    	var label = $('#dataTable\\:' + dType + 'StatusFilter_label');
    	
    	/*
        if(label.text() !== 'All') {
            label.addClass('selected-label').html('<div class="status ' + statusObj[label.text()] + '"></div>');
        }
    	*/

        $("#dataTable\\:" + dType + "StatusFilter_panel .ui-selectonemenu-list-item").each(function () {
            if($(this).text() !== "All") {
                $(this).html("<div class='status " + statusObj[$(this).text()] + "'></div><div class='text-label'>" + $(this).text()) + "</div>";
            }
        });
        
        $("body").trigger("click");
    };

    ADT.updateStatusIcons();
};	  
ADT.hideDialog = function () {
    "use strict";

    setTimeout(function() {
        if ($('.template-upload').length === 0) {
            progressDialog.hide();
        }
    }, 1000);
};

ADT.errorOrInfo = function () {
    "use strict";

    $('#daily-budget').focus(function () {
        if ($(this).next().hasClass('ui-message-error')) {
            $(this).next().next().next().remove();
        }
    });
};

ADT.escapeAutocompleteResults = function(selector){
	$(selector || ".ui-autocomplete-list-item").each(function(){
	    var rawText = $(this).text();
		$(this).text($("<div />").html(rawText).text());
	});
};

// Prevent the page from scrolling, after launching a modal popup.
ADT.togglePageScroll = function(disable){
	disable? 
		$("body").addClass("disable-scroll"):
		$("body").removeClass("disable-scroll");	
};


ADT.selectAllElement = function(selector){
	var $containerElement = $(selector);
	var $selectAllCheckbox = $(selector).find(".select-all .ui-chkbox-box");	
	
	// Reset this, just incase. a click handler was attached elsewhere.
	$selectAllCheckbox.off("click.selectallaction");
	
	ADT.applySelectAllAction($selectAllCheckbox, $containerElement);
	
	$(document).on("change.selectallcheckboxes", selector + " .ui-chkbox:not(.select-all) input", function(){
		var totalCheckboxCount = $(selector).find(".ui-chkbox:not(.select-all) .ui-chkbox-box").length;
		var selectedCheckboxCount = $(selector).find(".ui-chkbox:not(.select-all) .ui-chkbox-box.ui-state-active").length;

		var ifNoneAvailableAndSelectAllChecked = ((totalCheckboxCount == 0) && ($selectAllCheckbox.find(".ui-icon-check").length));		
		
		var ifAllSelectedAndSelectAllUnchecked = (
			((totalCheckboxCount == selectedCheckboxCount) && (selectedCheckboxCount > 0)) && 
			(!$selectAllCheckbox.find(".ui-icon-check").length)
		);
		
		var ifNoneSelectedAndSelectAllChecked = (
			((totalCheckboxCount > 0) && (selectedCheckboxCount == 0)) && 
			($selectAllCheckbox.find(".ui-icon-check").length)
		);
		
		var ifSomeSelectedAndSelectAllChecked = (
			((totalCheckboxCount > 0) && (selectedCheckboxCount < totalCheckboxCount)) && 
			($selectAllCheckbox.find(".ui-icon-check").length)
		);
				
		if(
			ifAllSelectedAndSelectAllUnchecked || ifNoneAvailableAndSelectAllChecked || 
			ifNoneSelectedAndSelectAllChecked || ifSomeSelectedAndSelectAllChecked
		) {
			$selectAllCheckbox.off("click.selectallaction");
			$selectAllCheckbox.trigger("click");
			ADT.applySelectAllAction($selectAllCheckbox, $containerElement);
		}
	});
	
	// Default the checkbox selection, when there are absolutely none available (datatables).
	// This should be cleaner, and use the variables set in the click handler itself.
	if(
		(
			!$(selector + " .ui-chkbox:not(.select-all) input").length && 
			$selectAllCheckbox.find(".ui-icon-check").length
		) 
			||
		(
			$(selector + " .ui-chkbox:not(.select-all) input").length && 
			!$(selector).find(".ui-chkbox:not(.select-all) .ui-chkbox-box.ui-state-active").length &&
			$selectAllCheckbox.find(".ui-icon-check").length
		) 
			||
		(	
			(($(selector).find(".ui-chkbox:not(.select-all) .ui-chkbox-box").length == $(selector).find(".ui-chkbox:not(.select-all) .ui-chkbox-box.ui-state-active").length) && 
			($(selector).find(".ui-chkbox:not(.select-all) .ui-chkbox-box.ui-state-active").length > 0)) && 
			(!$selectAllCheckbox.find(".ui-icon-check").length)		
		)	
	){
		$selectAllCheckbox.trigger("click");
	}
};

ADT.applySelectAllAction = function($element, $selectAllContainer){
	$element.on("click.selectallaction", function(){	
		$(this).is(".ui-state-active")?
			$selectAllContainer.find(".ui-chkbox-box:not(.ui-state-active)").trigger("click"):
			$selectAllContainer.find(".ui-chkbox-box.ui-state-active").trigger("click");								
	});
};

ADT.setFullsizeMenuDropdown = function(element, dropdown){
	var $inputElement = $('.' + element);
	$("." + dropdown).css({
		"position": "absolute",
		"width": $inputElement.width(),
		"left": $inputElement.offset().left + "px",
		"top": $inputElement.offset().top + $inputElement.height() + "px !important"
	});                        	
};

ADT.dashboardChart = {
	// Common Configuration values for all charts.		
	commonConfig: {
		gridBackground: "#FFFFFF",
		gridShadow: false,
		showLabel: false,
		lineWidth: 2,
		shadow: false,
		showMarker: false,
		markerOptionsSize: 10,
		markerOptionsShadowBoolean: false,
		fill: true,
		fillAndStroke: true,
		fillAlpha: 0.2
	},
	
   	largeConfig: {	
		cfgAxesXaxis: true,
		yaxisShow: true,
		xaxisShow: true,
		xaxisTickAngle: 0,
		highlighterShow: true,
		useAxesFormattersBoolean: false,
		formatDateItemNo: 1,
		formatDateFormat: "d M yy",
		markerRendererStyle: "circle",
		markerRendererLineWidth: 3,
		markerRendererSize: 12,
		markerRendererAlpha: 1,
		markerRendererBgColor: "#FFFFFF",
		lineWidth: 3,
		showMarker: true
   	}, 
	
	smallConfig: {	
		cfgAxesXaxis: false,
		highlighterShow: false,
		useAxesFormattersBoolean: null,
		formatDateItemNo: null,
		markerRendererStyle: null,
		markerRendererLineWidth: null,
		markerRendererSize: null,
		markerRendererAlpha: null,
		markerRendererBgColor: null,
		lineWidth: 1,
		showMarker: false
	},	
		
	// Find the value of the date range selection dropdown. 	
	getDateRangeSelect: function(){
		var selection = 0;
		
		$(".date-selection").
			find(".ui-button").
			each(function(index){
				//console.log(index, $(this).attr("class"))
				if($(this).is(".ui-state-active")) selection = index;
			});
		
		return selection;
	},	
	
	// Get the "highest" and "lowest" value in a multi-dimensional array based on a sort key.
	getMinMaxValues: function(array, key){
		var maxVal = 0;
		var minVal = 0;
		
		function max(a, b){ return (a[key] - b[key]) }
		array.sort(max);
				
		maxVal = array[array.length - 1];
		minVal = array[0];
		//console.log(key, maxVal[key], minVal[key]);
		//console.log("ARRAY", _self.cfg.data[0], "KEY", 0, "MIN", xAxisRange.min, "MAX", xAxisRange.max);
		return { max: maxVal[key], min: minVal[key] };
	},
	
	getRoundedValue: function(value, percentage){
		//console.log("Valued?: ", value, value.toPrecision(1));
		return Number(value < 1 ? Number(value * 1.5).toPrecision(1) : Math.ceil(value * 1.2));
	},		
	
	// To determine the units shown on the X axis labels,
	// and how far apart they should be.
	getXAxisUnits: function(dateSelection){
		var xAxisTickOptions = {};
		
		switch(dateSelection){
			case 0: xAxisTickOptions = { formatString: "%H:%M", tickInterval: "1 hour" }; break;
			case 1: xAxisTickOptions = { formatString: "%H:%M", tickInterval: "1 hour" }; break;
			case 2: xAxisTickOptions = { formatString: "%a", tickInterval: "1 day" }; break;
			default: xAxisTickOptions = { formatString: "%d", tickInterval: "1 day"};
		}
		
		return xAxisTickOptions;
	},
	
   	getYAxisInterval: function(dataRange){
		var yAxisFloor = 0;
		var cap = dataRange.max;
		
		if(dataRange.max == 0){
			cap = 100;
		}
		//console.log("CAP", cap, "INTERVAL", interval);
		return {
			floor: yAxisFloor,
			cap: cap
		};
	},
	
	selectFormatter: function (_self){
		var formatter_function;
		
		switch (_self.id){
			case "linear-fill-rate":
			case "linear-ctr":
				formatter_function = this.percentageFormatter;
				break;
			case "linear-spend":
			case "linear-revenue":
			case "linear-ecpm":
				formatter_function = this.moneyFormatter;
				break;
			default:
				formatter_function = this.longFormatter;
		}
		
		return formatter_function;
	},
	
	longFormatter: function (format, val) {
		var result = val;
		
		if (val>=1000000000){
			result = val / 1000000000;
			result = result.toFixed(1)+"bn";
		}else if (val >= 1000000) {
			result = val / 1000000;
			result = result.toFixed(1)+"m";
	    }else if (val >= 1000) {
	    	result = val / 1000;
            if (result < 10) {
            	result = result.toFixed(1)+"k";
            }else{
            	result = result.toFixed(0)+"k";
            }
	    }else if (val > 1){
	    	result = result.toFixed(0);
	    }else if (val > 0){
	    	result = val.toFixed(2);
	    } 
		//console.log("longFormatter: VAL=", val, "RESULT=", result);
	    return result;
	},
	
	percentageFormatter: function (format, val) {
		var result = val.toFixed(2) + "%";
		//console.log("percentageFormatter: VAL=", val, "RESULT=", result);
		return result;
	},
	
	moneyFormatter: function (format, val) {
		var result = "$" + val.toFixed(2);
		//console.log("moneyFormatter: VAL=", val, "RESULT=", result);
		return result
	},
	
   	extender: function(tabText, config, color, _self){
   		var xAxisRange = this.getMinMaxValues(_self.cfg.data[0], 0);
   		var yAxisConfig = this.getYAxisInterval(this.getMinMaxValues(_self.cfg.data[0], 1));
   		// in case of agency console this structure not exists
   		//var currentTabText = $.trim($(".mainTab td.active .tabHeader").text());
   		var currentTabText = tabText;
   		var currentTabValue = $.trim($(".mainTab td.active .tabData").text());  
   		
   		_self.cfg.axes.yaxis = {
   			renderer: $.jqplot.LogAxisRenderer,
   			tickDistribution: "even",
   	        min: yAxisConfig.floor,
   	        max: yAxisConfig.cap
   		}
   		
   		_self.cfg.axes.yaxis.tickOptions = {
			formatter: this.selectFormatter(_self)
   		};
   		
		_self.cfg.axes.xaxis = {
		    renderer: $.jqplot.DateAxisRenderer,
	   		tickRenderer: $.jqplot.CanvasAxisTickRenderer,
	        min: xAxisRange.min,
	        max: xAxisRange.max,
		    tickOptions: { formatString: this.getXAxisUnits(this.getDateRangeSelect()).formatString },
		    tickInterval: this.getXAxisUnits(this.getDateRangeSelect()).tickInterval
		};
   
   		 
   		if(_self.cfg.series[0]){
   			var gridPointMarkerStyle = _self.cfg.series[0];
   			gridPointMarkerStyle.showLabel = this.commonConfig.showLabel;
   		    gridPointMarkerStyle.lineWidth = config.lineWidth;
   		    gridPointMarkerStyle.shadow = this.commonConfig.shadow;
   		    gridPointMarkerStyle.color = color;
   		    gridPointMarkerStyle.showMarker = config.showMarker;
   		    gridPointMarkerStyle.fill = this.commonConfig.fill;
   		    gridPointMarkerStyle.fillAndStroke = this.commonConfig.fillAndStroke;
   		    gridPointMarkerStyle.fillAlpha = this.commonConfig.fillAlpha;
   		    gridPointMarkerStyle.markerOptions = {
   		        size: this.commonConfig.markerOptionsSize,
   		        shadow: this.commonConfig.markerOptionsShadowBoolean
   		    };
   		}

   	 	_self.cfg.highlighter.show = config["highlighterShow"];
   	 	_self.cfg.highlighter.useAxesFormatters = config["useAxesFormattersBoolean"];
   		_self.cfg.highlighter.formatDate = {
   		    itemNo: config["formatDateItemNo"],
   		    dateFormat: config["formatDateFormat"]
   		};

   		ADT.tooltipLabel = String(currentTabText);

   		ADT.formatString = String(
				currentTabValue[currentTabValue.length - 1] == "%"? 
					"<div class='inner'><p class='date'>%s</p><p class='rate'>%label</p><p class='value'>%.2f%</p></div>":
					currentTabValue[0] == "$"?
						"<div class='inner'><p class='date'>%s</p><p class='rate'>%label</p><p class='value'>$%.2f</p></div>":
					"<div class='inner'><p class='date'>%s</p><p class='rate'>%label</p><p class='value'>%d</p></div>"	
			);
   		
   		_self.cfg.highlighter.tooltipLabel = ADT.tooltipLabel;
   		_self.cfg.highlighter.tooltipUnit = ADT.tooltipUnit;
   		_self.cfg.highlighter.formatString = ADT.formatString;
   		_self.cfg.highlighter.markerRenderer = new $.jqplot.MarkerRenderer({
   		    shadow: false,
   		    customOptions: {
   		        style: 'circle',
   		        lineWidth: config.lineWidth,
   		        size: 12,
   		        alpha: 1,
   		        bgColor: '#FFFFFF'
   		    }
   		});
   		
   		_self.cfg.grid = {
   			drawGridlines: false,
   			background: this.commonConfig.gridBackground,
   			drawBorder: false,
   			shadow: this.commonConfig.gridShadow,
   			series:	[
   		       	 {
   	       		 	showLine: true,
   	       		 	markerOptions: { show:true, style:'filledCircle'}
   		       	 }
   		   ]
   		};
   		
   		_self.cfg.drawBorder = false;
   	}
	
	/* Temporary mocker.
    fudgeValues: function(array){
    	var newArray = [];
    	
    	for(var item in array){
    		newArray.push(array[item]);
    		newArray[item][1] = Number((Math.random() * 0.05).toPrecision(5));	
    	}
    	return newArray;
    }
    */	
};   

// You can pass in JQuery objects, as well as strings into this method.
ADT.scrollToElement = function(selector, parentSelector){
	if($(selector).length){
		// This is hacky. The else statement excuses it only slightly.
		if($(selector).parent().is(".item-data")){
			$("html, body").animate({ scrollTop: Number($(selector).closest("form").offset().top - $("#headerForm").height()) + "px" });
		} else {
			$(parentSelector || "html, body").animate({ scrollTop: Number(
				($(selector, ":first").offset().top - $("#headerForm").height()) - $(selector, ":first").height())
			+ "px" });
		}
	}
};

ADT.centreDialog = function(horizontally){
	var newPosition = Number($(window).height() / 2) - ($(".ui-dialog.ui-overlay-visible").height() / 2);
	var newHorizontalPosition = Number($(window).width() / 2) - ($(".ui-dialog.ui-overlay-visible").width() / 2);
	    $(".ui-dialog.ui-overlay-visible").css("top", Number(newPosition < 0 ? 10 : newPosition) + "px").show();
	    if(horizontally){
	    $(".ui-dialog.ui-overlay-visible").css("left", Number(newHorizontalPosition < 0 ? 10 : newHorizontalPosition) + "px").show(); 
	    }
	};

ADT.$originalBalanceNotificationButton = null;

ADT.setBalanceNotificationButton = function(){
	//console.log(ADT.$originalBalanceNotificationButton);
	if(!ADT.$originalBalanceNotificationButton){
		ADT.$originalBalanceNotificationButton = $("#saveBalanceNotificationButton").clone();
	}
	
	var $currentEmailField = $("#balanceNotificationForm .email-row.new input");								
	var $submitButton = ADT.$originalBalanceNotificationButton;
	var $submitButtonProxy = ADT.$originalBalanceNotificationButton.
		clone().
		attr({
			"name": "",
			"onclick": ""
		});
	
	$("#saveBalanceNotificationButton").replaceWith($submitButtonProxy);
	
	$submitButtonProxy.on("click", function(e){
		var _self = $(this);  
		e.preventDefault();
		
		if($currentEmailField.length && $currentEmailField.val().length > 0){
			ADT.validateEmail(
				$currentEmailField.val(), 
				function(status){
					if(status == "ERROR"){
						$currentEmailField.
							parent().
							find(".email-error").
							show();
					} else {
						triggerSubmit(true);
					}
				}
			);
		} else {
			triggerSubmit();
		}
		
		function triggerSubmit(includeCurrentField){
			ADT.composeNewBalanceNotificationEmailList(
				function(emailItems){
					$submitButtonProxy.replaceWith($submitButton.clone());
					$submitButton.trigger("click");
				},
				includeCurrentField
			);										
		}
	});
};

ADT.composeNewBalanceNotificationEmailList = function(callback, addCurrent){
	var $notificationItems = $("#balanceNotificationForm .email-row.disabled input");
	var newEmailList = "";
	// var activeEmailList = $("#notifyAdditionalEmails").val("");
	
	if($notificationItems.length){
		//console.log(newEmailList, addCurrent);
		$notificationItems.each(function(index){
			index == 0?
				newEmailList += $(this).val():
				newEmailList += "," + $(this).val();
			
			if(index == Number($notificationItems.length - 1)){
				if(addCurrent){
					// Add the currently highlighted field, if the user is saving the form,
					// and the content is admissable.
					$notificationItems.length == 0?
						newEmailList += $("#balanceNotificationForm .email-row.new input").val():
						newEmailList += "," + $("#balanceNotificationForm .email-row.new input").val();																						
				}
				createNewListAndCallback();
			}
		});
	} else {
		if(addCurrent){
			$notificationItems.length == 0?
				newEmailList += $("#balanceNotificationForm .email-row.new input").val():
				newEmailList += "," + $("#balanceNotificationForm .email-row.new input").val();																						
		}									
		createNewListAndCallback();
	}
	
	function createNewListAndCallback(){
		$("#notifyAdditionalEmails").val(newEmailList);
		callback($("#notifyAdditionalEmails").val());
	}
};

ADT.transactionTypeSelect = function(elementSelector){
	$(elementSelector).
		closest("th").
		find(">select option").
		each(function(){
			if ($(this).text() == $(elementSelector).find("label").text()) {
				$(elementSelector).
					closest("th").
					css("display", "block");
				 
				$(elementSelector).
					closest("th").
					find(">select").
					val($(this).attr("value")).change();
			}
		});
};

// ADT.initialiseBalanceNotificationForm?
ADT.displayBalanceNotificationRecipients = function(){
	var activeEmailArray = String($("#notifyAdditionalEmails").val()).split(",");
	var $emailListWrapper = $(".add-email-block");
	var $emailListItemTemplate = $(".add-email-block .new.email-row");
	
	$("#balanceNotificationForm .email-row.disabled").remove();
	// Can also clear error messages at this point. 
	$("#balanceNotificationForm .email-error").hide();
	
	if(activeEmailArray.length >= 1 && activeEmailArray[0].length > 0){
		for(var item in activeEmailArray){
			var $newListItem = $emailListItemTemplate.
				clone().
				removeClass("new").
				addClass("disabled");
			
			$newListItem.find("input").
				prop("disabled", true).
				val(activeEmailArray[item]);
			
			if($emailListWrapper.find(".email-row:first").is(".new")){
				$newListItem.prependTo($emailListWrapper);
			} else {									
				$newListItem.
					insertAfter(".add-email-block .email-row.disabled:last").
					removeClass("first");	
				
				if($emailListItemTemplate.is(".first")){
					$emailListItemTemplate.removeClass("first");
				}
			}
		}
		
		// Take the template item (the default new row), and  
		$emailListItemTemplate.removeClass("first");
	}
	
	ADT.scrollToElement( 
		$(".add-email-block .new.email-row:last"), 
		$("#balanceNotificationForm .form-row.add-email-block")
	);
};
	
ADT.addBalanceNotificationRecipient = function($element, callback){
	var $currentEmailRow = $element.parent();
	var $currentInput = $currentEmailRow.find("input");
	var $newEmailRow = $currentEmailRow.clone().removeClass("first").addClass("new");
	var $emailAddressWrapper = $currentEmailRow.parent(); //Inferred. May possibly make this explicit, as above. 
	
	//console.log($element.attr("class"), String($currentInput.val()).match(testEmail), activeEmailList, $currentInput.val());
	
	ADT.validateEmail(String($currentInput.val()), function(status){
		// Reset the state of the submit button proxy.
		ADT.setBalanceNotificationButton();
		
		if(status == "SUCCESS"){										
			$currentEmailRow.
				removeClass("new").
				addClass("disabled");
			
			$currentInput.prop("disabled", true);
			$newEmailRow.appendTo($emailAddressWrapper);
			$newEmailRow.find("input").val("");
			
			// When a new form field is created, we need to reattach the submit form
			// button click handler, to account for the new form file target.
			ADT.setBalanceNotificationButton();
			
			// Always make an attempt to scroll to the latest field.
			ADT.scrollToElement( 
				$(".add-email-block .new.email-row:last"), 
				$("#balanceNotificationForm .form-row.add-email-block")
			);	
			
			if(callback) callback("SUCCESS");
		} else {
			$currentEmailRow.find(".email-error").show();
			if(callback) callback("ERROR");
		}
	});
};

ADT.validateEmail = function(inputString, callback){
	var testEmail = /^[-0-9a-zA-Z.+_]+@[-0-9a-zA-Z.+_]+\.[a-zA-Z]{2,4}$/;
	
	inputString.match(testEmail)? 
		callback("SUCCESS"):
		callback("ERROR");
};

ADT.enableCampaignReactivateButton = function(){
	var selectedItems = 0;
	
    $(".ui-chkbox:not(.ui-chkbox-all)").each(function(){
    	$thisCell = $(this).parent();
    	$nextCell = $thisCell.next("td"); // Should be the status indicator cell.
    	$thisChecked = $thisCell.find(".ui-icon-check");
    	$thisStatus = $nextCell.find(".status");
    	
		if($thisChecked.length && ($thisStatus.is(".COMPLETED") || $thisStatus.is(".STOPPED"))) selectedItems++;
    });
    
   	selectedItems? reButton.enable() : reButton.disable();
    
    return selectedItems;
};

/* Prevent select row within datatable if you click on each cell (allow only via checkbox with .select-col class)
 * Exception is the celleditor where you need the click event to bubble up to display the cell editor properly
 * http://stackoverflow.com/questions/15128945/primefaces-datatable-with-checkbox-selection-only
 */
ADT.cancelDatatableRowClickSelect = function(){
	$(".ui-datatable tbody td:not(.select-col)").on("click", function(e) {
		// Only if not editable cell (allow cell editor feature)
		if (!$(e.target).hasClass('ui-cell-editor-output')) {
			e.stopPropagation(); 
		}
	});
};

/* Scheduling calendar.js, campaign scheduling and reporting start/end date related javascript logic */

ADT.setStartDateCalendar = function(){
	$("#ui-datepicker-div").removeClass("end-date-calendar");
	// ADT.blockDatesBasedOnEndDate();
};

// Start date later than end date set end date to the same day
/*ADT.adjustEndDate = function(){
	ADT.adjustEndDate('');
};*/
ADT.adjustEndDate = function(id){
	var idx = id !== undefined ? id : '';
	var startDateWidget = PF('startDateCalendar' + idx);
	var endDateWidget = PF('endDateCalendar' + idx);
	if(startDateWidget.getDate() > endDateWidget.getDate()) {
		endDateWidget.setDate(startDateWidget.getDate());
	}
};
	
/*ADT.setEndDateCalendar = function(){
	ADT.setEndDateCalendar('');
};*/
ADT.setEndDateCalendar = function(id){
	var idx = id !== undefined ? id : '';
	$(".ui-datepicker-next, .ui-datepicker-prev").off("click.blockdates");
	$("#ui-datepicker-div").addClass("end-date-calendar");
	ADT.blockDatesBasedOnStartDate(idx);
};

/*ADT.blockDatesBasedOnStartDate = function(){
	ADT.blockDatesBasedOnStartDate('');	
};*/
ADT.blockDatesBasedOnStartDate = function(id){
	var idx = id !== undefined ? id : '';
    $(".ui-datepicker-next, .ui-datepicker-prev").on("click.blockdates", function(e){
	    ADT.blockDatesBasedOnStartDate(idx);
    });
	
	$("#ui-datepicker-div td").each(function(){
		var startDateWidget = PF('startDateCalendar' + idx);
		if(startDateWidget.getDate()){
			var selectedStartDate = startDateWidget.getDate();
			var selectedYear = selectedStartDate.getFullYear();
			var selectedMonth = selectedStartDate.getMonth();
			var selectedDate = selectedStartDate.getDate();
			
			var sameMonthEalierDateStatement = (
				Number($(this).text()) < selectedDate &&
				$(this).data("month") == selectedMonth &&
				$(this).data("year") == selectedYear &&
				$(this).is(":not(.ui-datepicker-current-day)")								
			);
			
			var ealierMonthStatement = (
				($(this).data("month") < selectedMonth && $(this).data("year") == selectedYear) || $(this).data("year") < selectedYear
			);
			
			if(sameMonthEalierDateStatement || ealierMonthStatement){
				var $disabledCellTextSpan = $("<span></span>").text($(this).text()); // Create a span to replace the <a> (styling)
				$(this).addClass("ui-datepicker-unselectable ui-state-disabled");
				$(this).empty();
				$disabledCellTextSpan.appendTo(this);
			}
		}
	});    	
};

/*ADT.blockDatesBasedOnEndDate = function(){
	ADT.blockDatesBasedOnEndDate('');  	
};
ADT.blockDatesBasedOnEndDate = function(id){
	var idx = id !== undefined ? id : '';
    $(".ui-datepicker-next, .ui-datepicker-prev").on("click.blockdates", function(e){
	    ADT.blockDatesBasedOnEndDate(idx);
    });
	
	$("#ui-datepicker-div td").each(function(){
		var endDateWidget = PF('endDateCalendar' + idx);
		if(endDateWidget.getDate()){
			var selectedEndDate = endDateWidget.getDate();
			var selectedYear = selectedEndDate.getFullYear();
			var selectedMonth = selectedEndDate.getMonth();
			var selectedDate = selectedEndDate.getDate();
			
			var sameMonthLaterDateStatement = (
				Number($(this).text()) > selectedDate &&
				$(this).data("month") == selectedMonth &&
				$(this).data("year") == selectedYear &&
				$(this).is(":not(.ui-datepicker-current-day)")								
			);
			
			var laterMonthStatement = (
				($(this).data("month") > selectedMonth && $(this).data("year") == selectedYear) || $(this).data("year") > selectedYear
			);
			
			if(sameMonthLaterDateStatement || laterMonthStatement){
				var $disabledCellTextSpan = $("<span></span>").text($(this).text()); // Create a span to replace the <a> (styling)
				$(this).addClass("ui-datepicker-unselectable ui-state-disabled");
				$(this).empty();
				$disabledCellTextSpan.appendTo(this);
			}
		}
	});    	
};
*/

/* Simple debug message if enabled */
ADT.debug = function(message, enabled) {
	if(enabled) alert(message);
};