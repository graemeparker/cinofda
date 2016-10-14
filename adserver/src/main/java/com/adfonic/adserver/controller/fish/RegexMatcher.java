package com.adfonic.adserver.controller.fish;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.adfonic.adserver.controller.fish.RtbFishnet.MatchContext;
import com.adfonic.adserver.controller.rtb.RtbExecutionContext;

/**
 * 
 * @author mvanek
 *
 */
public abstract class RegexMatcher extends FishMatcherBase {

    private final Pattern pattern;

    private final boolean positive;

    public RegexMatcher(String expression) {
        this(expression, true);
    }

    public RegexMatcher(String expression, boolean positive) {
        this.pattern = Pattern.compile(expression);
        this.positive = positive;
    }

    protected abstract String getMatchInput(RtbExecutionContext<?, ?> execContext, MatchContext matchContext);

    @Override
    public boolean doMatch(RtbExecutionContext<?, ?> execContext, MatchContext matchContext) {
        String input = getMatchInput(execContext, matchContext);
        if (input != null) {
            Matcher matcher = pattern.matcher(input);
            boolean matches = matcher.matches();
            return positive && matches || !positive && !matches;
        } else {
            return false;
        }
    }

}

class RegexRtbRequestStringMatcher extends RegexMatcher implements RtbRequestMatcher {

    public RegexRtbRequestStringMatcher(String expression) {
        super(expression);
    }

    public RegexRtbRequestStringMatcher(String expression, boolean positive) {
        super(expression, positive);
    }

    @Override
    protected String getMatchInput(RtbExecutionContext<?, ?> execContext, MatchContext matchContext) {
        return execContext.getRtbRequestString();
    }
}

class RegexRtbResponseStringMatcher extends RegexMatcher implements RtbResponseMatcher {

    public RegexRtbResponseStringMatcher(String expression) {
        super(expression);
    }

    public RegexRtbResponseStringMatcher(String expression, boolean positive) {
        super(expression, positive);
    }

    @Override
    protected String getMatchInput(RtbExecutionContext<?, ?> execContext, MatchContext matchContext) {
        return execContext.getRtbResponseString();
    }
}