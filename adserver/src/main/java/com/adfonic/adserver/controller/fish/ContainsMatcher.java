package com.adfonic.adserver.controller.fish;

import org.apache.commons.lang.StringUtils;

import com.adfonic.adserver.controller.fish.RtbFishnet.MatchContext;
import com.adfonic.adserver.controller.rtb.RtbExecutionContext;

/**
 * 
 * @author mvanek
 *
 */
public abstract class ContainsMatcher extends FishMatcherBase {

    private final String expression;

    private final boolean positive;

    public ContainsMatcher(String expression) {
        this(expression, true);
    }

    public ContainsMatcher(String expression, boolean positive) {
        if (StringUtils.isEmpty(expression)) {
            throw new IllegalArgumentException("Empty expression");
        }
        this.expression = expression;
        this.positive = positive;
    }

    protected abstract String getMatchInput(RtbExecutionContext<?, ?> execContext, MatchContext matchContext);

    @Override
    public boolean doMatch(RtbExecutionContext<?, ?> execContext, MatchContext matchContext) {
        String input = getMatchInput(execContext, matchContext);
        if (input != null) {
            boolean contains = input.contains(expression);
            return positive && contains || !positive && !contains;
        } else {
            return false;
        }
    }

}

class ContainsRtbRequestStringMatcher extends ContainsMatcher implements RtbRequestMatcher {

    public ContainsRtbRequestStringMatcher(String expression) {
        super(expression);
    }

    public ContainsRtbRequestStringMatcher(String expression, boolean positive) {
        super(expression, positive);
    }

    @Override
    protected String getMatchInput(RtbExecutionContext<?, ?> execContext, MatchContext matchContext) {
        return execContext.getRtbRequestString();
    }
}

class ContainsRtbResponseStringMatcher extends ContainsMatcher implements RtbResponseMatcher {

    public ContainsRtbResponseStringMatcher(String expression) {
        super(expression);
    }

    public ContainsRtbResponseStringMatcher(String expression, boolean positive) {
        super(expression, positive);
    }

    @Override
    protected String getMatchInput(RtbExecutionContext<?, ?> execContext, MatchContext matchContext) {
        return execContext.getRtbResponseString();
    }
}
