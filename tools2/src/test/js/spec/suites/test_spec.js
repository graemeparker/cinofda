describe("timeRangeFields", function() {
    "use strict";

    beforeEach(function() {
        jasmine.getFixtures().fixturesPath = "../../src/test/js/spec/fixtures";
        loadFixtures('date_range.html');

    });

    it("Should unmark all fields for corresponding column when x-axis checkbox unchecked", function() {
        ADT.timeRangeFields('#time-range-boxes', true);
        $('.hour.x3').trigger('click');
        expect($('div[data-x="3"][data-y="0"]')).toHaveClass('off');
        expect($('div[data-x="3"][data-y="1"]')).toHaveClass('off');
    });

    it("Should unmark all fields for corresponding row when y-axis checkbox unchecked", function() {
        ADT.timeRangeFields('#time-range-boxes', true);
        $('.day.y0').trigger('click');
        expect($('div[data-x="0"][data-y="0"]')).toHaveClass('off');
        expect($('div[data-x="1"][data-y="0"]')).toHaveClass('off');
        expect($('div[data-x="2"][data-y="0"]')).toHaveClass('off');
        expect($('div[data-x="3"][data-y="0"]')).toHaveClass('off');
    });

    it("Should mark correct fields for corresponding column when x-axis checkbox checked", function() {
        ADT.timeRangeFields('#time-range-boxes', true);
        $('.hour.x0').trigger('click');
        expect($('div[data-x="0"][data-y="0"]')).toHaveClass('on');
        expect($('div[data-x="0"][data-y="1"]')).toHaveClass('off');
    });

    it("Should mark correct fields for corresponding row when y-axis checkbox checked", function() {
        ADT.timeRangeFields('#time-range-boxes', true);
        $('.day.y1').trigger('click');
        expect($('div[data-x="0"][data-y="1"]')).toHaveClass('off');
        expect($('div[data-x="1"][data-y="1"]')).toHaveClass('on');
        expect($('div[data-x="2"][data-y="1"]')).toHaveClass('off');
        expect($('div[data-x="3"][data-y="1"]')).toHaveClass('on');
    });

    it("Should leave not-corresponding rows when y-axis checkbox checked", function() {
        ADT.timeRangeFields('#time-range-boxes', true);
        $('.day.y1').trigger('click');
        expect($('div[data-x="0"][data-y="0"]')).toHaveClass('off');
        expect($('div[data-x="1"][data-y="0"]')).toHaveClass('on');
        expect($('div[data-x="2"][data-y="0"]')).toHaveClass('off');
        expect($('div[data-x="3"][data-y="0"]')).toHaveClass('on');
    });

    it("Should leave not-corresponding columns when x-axis checkbox checked", function() {
        ADT.timeRangeFields('#time-range-boxes', true);
        $('.day.x2').trigger('click');
        expect($('div[data-x="0"][data-y="0"]')).toHaveClass('off');
        expect($('div[data-x="0"][data-y="1"]')).toHaveClass('off');
        expect($('div[data-x="1"][data-y="0"]')).toHaveClass('on');
        expect($('div[data-x="1"][data-y="1"]')).toHaveClass('off');
        expect($('div[data-x="3"][data-y="0"]')).toHaveClass('on');
        expect($('div[data-x="3"][data-y="1"]')).toHaveClass('off');
    });

    it("Should leave not-corresponding rows when y-axis checkbox unchecked", function() {
        ADT.timeRangeFields('#time-range-boxes', true);
        $('.day.y0').trigger('click');
        expect($('div[data-x="0"][data-y="1"]')).toHaveClass('off');
        expect($('div[data-x="1"][data-y="1"]')).toHaveClass('off');
        expect($('div[data-x="2"][data-y="1"]')).toHaveClass('off');
        expect($('div[data-x="3"][data-y="1"]')).toHaveClass('off');
    });

    it("Should leave not-corresponding columns when x-axis checkbox unchecked", function() {
        ADT.timeRangeFields('#time-range-boxes', true);
        $('.day.x3').trigger('click');
        expect($('div[data-x="0"][data-y="0"]')).toHaveClass('off');
        expect($('div[data-x="0"][data-y="1"]')).toHaveClass('off');
        expect($('div[data-x="1"][data-y="0"]')).toHaveClass('on');
        expect($('div[data-x="1"][data-y="1"]')).toHaveClass('off');
        expect($('div[data-x="2"][data-y="0"]')).toHaveClass('off');
        expect($('div[data-x="2"][data-y="1"]')).toHaveClass('off');
    });

    it("Should mark checked range on load", function() {

        $('.hour.x0')[0].setAttribute('checked', 'checked');
        $('.hour.x2')[0].setAttribute('checked', 'checked');
        $('.day.y1')[0].setAttribute('checked', 'checked');

        $('.row-boxes > div').each(function () {
            $(this).removeAttr('class');
        });

        ADT.timeRangeFields('#time-range-boxes', true);

        expect($('div[data-x="0"][data-y="0"]')).toHaveClass('on');
        expect($('div[data-x="0"][data-y="1"]')).toHaveClass('on');
        expect($('div[data-x="1"][data-y="0"]')).toHaveClass('on');
        expect($('div[data-x="1"][data-y="1"]')).toHaveClass('on');
        expect($('div[data-x="2"][data-y="0"]')).toHaveClass('on');
        expect($('div[data-x="2"][data-y="1"]')).toHaveClass('on');
        expect($('div[data-x="3"][data-y="0"]')).toHaveClass('on');
        expect($('div[data-x="3"][data-y="1"]')).toHaveClass('on');
    });
});

describe("accordion", function() {
    "use strict";

    beforeEach(function() {
        jasmine.getFixtures().fixturesPath = "../../src/test/js/spec/fixtures";
        loadFixtures('accordion.html');
        ADT.accordion('#cont');
    });

    it("Should expand accordion content when header clicked", function() {
        runs(function () {
            $('.acc-head.no1 .exec').trigger('click');
        });

        waits(505);

        runs(function () {
            expect($('.acc-content.no1')).toBeVisible();
        });
    });

    it("Should collapse expanded accordion content when header clicked", function() {
        runs(function () {
            $('.acc-head.no3 .exec').trigger('click');
        });

        waits(505);

        runs(function () {
            expect($('.acc-content.no3')).toBeHidden();
        });
    });

    it("Should not change other accordion items when one clicked", function() {
        runs(function () {
            $('.acc-head.no2 .exec').trigger('click');
        });

        waits(505);

        runs(function () {
            expect($('.acc-content.no1')).toBeHidden();
            expect($('.acc-content.no3')).toBeVisible();
            expect($('.acc-head.no3')).toHaveClass('on');
        });
    });

    it("Should not expand accordion when element other than arrow or name clicked", function() {
        runs(function () {
            $('.acc-head.no1 .none').trigger('click');
        });

        waits(505);

        runs(function () {
            expect($('.acc-content.no1')).toBeHidden();
            expect($('.acc-head.no1')).not.toHaveClass('on');
        });
    });
});

describe("crAccordion (creatives page)", function() {
    "use strict";

    beforeEach(function() {
        jasmine.getFixtures().fixturesPath = "../../src/test/js/spec/fixtures";
        loadFixtures('cr_accordion.html');
        ADT.crAccordion('#cont', '#submitStatus');
    });

    it("Should expand accordion content and hide header when header clicked", function() {
        runs(function () {
            $('.acc-head.no1').trigger('click');
        });

        waits(505);

        runs(function () {
            expect($('.acc-content.no1')).toBeVisible();
            expect($('.acc-head.no1')).toBeHidden();
        });
    });

    it("Should disable other accordion items when one has been expanded open", function() {
        runs(function () {
            $('.acc-head.no1').trigger('click');
        });

        waits(505);

        runs(function () {
            expect($('.acc-content.no1')).toBeVisible();
            expect($('.acc-head.no1')).toBeHidden();

            $('.acc-head.no2').trigger('click');
        });

        waits(505);

        runs(function () {
            expect($('.acc-content.no1')).toBeVisible();
            expect($('.acc-head.no1')).toBeHidden();
            expect($('.acc-content.no2')).toBeHidden();
            expect($('.acc-head.no2')).toBeVisible();
        });
    });

    it("Shouldn't close accordion when status field is not equal to 'CLOSED'", function() {
        runs(function () {
            $('#submitStatus').val('OPENED');
            $('.close.no3').trigger('click');
        });

        waits(505);

        runs(function () {
            expect($('.acc-content.no3')).toBeVisible();
            expect($('.acc-head.no3')).toBeHidden();
        });
    });
});

describe("subElExpander for checkbox group", function() {
    "use strict";

    beforeEach(function() {
        jasmine.getFixtures().fixturesPath = "../../src/test/js/spec/fixtures";
        loadFixtures('checkExpander.html');
        ADT.subElExpander('check', 'group');
    });

    it("Should prepare markup - put panels as children elements of checkbox items on page load", function() {
        waits(250);

        runs(function () {
            expect($('.label-td-2 > .p2')).toExist();
            expect($('.label-td-3 > .p3')).toExist();
        });
    });

    it("Should expand subitem of selected checkbox on page load", function() {
        waits(250);

        runs(function () {
            expect($('.label-td-3 > .p3')).toExist();
            expect($('.label-td-3 > .p3')).toBeVisible();
            expect($('.tr3')).toHaveClass('open');
        });
    });

    it("Should expand subitem of checked checkbox", function() {
        waits(250);

        runs(function () {
            $('.check2').trigger('click');
        });

        waits(505);

        runs(function () {
            expect($('.label-td-2 > .p2')).toExist();
            expect($('.label-td-2 > .p2')).toBeVisible();
            expect($('.tr2')).toHaveClass('open');
            expect($('.tr5')).toHaveClass('open');
        });
    });

    it("Shouldn't close expanded subitem when other checkbox checked", function() {
        waits(250);

        runs(function () {
            $('.check2').trigger('click');
        });

        waits(505);

        runs(function () {
            expect($('.label-td-3 > .p3')).toExist();
            expect($('.label-td-3 > .p3')).not.toBeHidden();
            expect($('.tr3')).toHaveClass('open');
        });
    });
});

describe("subElExpander - for radio button group", function() {
    "use strict";

    beforeEach(function() {
        jasmine.getFixtures().fixturesPath = "../../src/test/js/spec/fixtures";
        loadFixtures('radioExpander.html');
        ADT.subElExpander('radio', 'group');
    });

    it("Should prepare markup - put panels as children elements of radio button items on page load", function() {
        waits(250);

        runs(function () {
            expect($('.label-td-2 > .p2')).toExist();
            expect($('.label-td-3 > .p3')).toExist();
        });
    });

    it("Should expand subitem of selected radio button on page load", function() {
        waits(250);

        runs(function () {
            expect($('.label-td-3 > .p3')).toExist();
            expect($('.label-td-3 > .p3')).toBeVisible();
            expect($('.tr3')).toHaveClass('open');
        });
    });

    it("Should expand subitem of clicked radio button item", function() {
        waits(250);

        runs(function () {
            $('.radio2').trigger('click');
        });

        waits(505);

        runs(function () {
            expect($('.label-td-2 > .p2')).toExist();
            expect($('.label-td-2 > .p2')).toBeVisible();
            expect($('.tr2')).toHaveClass('open');
        });
    });

    it("Should close expanded subitem when other radio button item clicked", function() {
        waits(250);

        runs(function () {
            $('.radio2').trigger('click');
        });

        waits(1250);

        runs(function () {
            expect($('.label-td-3 > .p3')).toExist();
            expect($('.label-td-3 > .p3')).toBeHidden();
            expect($('.tr3')).not.toHaveClass('open');
        });
    });

});

describe("charCount", function () {
    "use strict";

    beforeEach(function () {
        jasmine.getFixtures().fixturesPath = "../../src/test/js/spec/fixtures";
        loadFixtures('counter.html');
    });

    it("Should update the number of allowed chars on the counter when initialized", function () {
        $('#field').val('xxxx5xxx10xxx15');

        ADT.charCount('#field', '#count', 35, 100);

        expect($('#count').text()).toBe('20');
    });

    it("Should reduce the number of allowed chars on the counter, when chars entered into text field", function () {
        ADT.charCount('#field', '#count', 35, 100);

        expect($('#count').text()).toBe('35');

        $('#field').val('xxxxx').keydown();

        expect($('#count').text()).toBe('30');

        $('#field').val('xxxx5xxx10xxx15xxx20xxx25xxx30xxx35xxx40').keydown();

        expect($('#count').text()).toBe('-5');

        $('#field').val('xxxx5xxx10xxx15xxx20xxx25xxx30xxx35xxx40xxx45xxx50' +
            'xxx55xxx60xxx65xxx70xxx75xxx80xxx85xxx90xxx95xx100xx105').keydown();

        expect($('#count').text()).toBe('-65');
        expect($('#field').val().length).toBe(100);

    });

    it("Should increase the number of allowed chars on the counter, when chars removed from text field", function () {
        ADT.charCount('#field', '#count', 35, 100);

        $('#field').val('xxxxx').keydown();

        expect($('#count').text()).toBe('30');

        $('#field').val('xx').keydown();

        expect($('#count').text()).toBe('33');

        $('#field').val('').keydown();

        expect($('#count').text()).toBe('35');
    });

    it("Shoud apply error class to counter when count is less than 0", function () {
        ADT.charCount('#field', '#count', 35, 100);

        $('#field').val('xxxx5xxx10xxx15xxx20xxx25xxx30xxx35xxx40').keydown();

        expect($('#count').parent().attr('class')).toBe('error');
    });
});