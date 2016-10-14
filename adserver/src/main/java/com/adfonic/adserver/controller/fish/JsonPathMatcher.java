package com.adfonic.adserver.controller.fish;

import com.adfonic.adserver.controller.fish.RtbFishnet.MatchContext;
import com.adfonic.adserver.controller.rtb.RtbExecutionContext;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

/**
 * 
 * @author mvanek
 *
 */
public abstract class JsonPathMatcher extends FishMatcherBase {

    private static final Configuration configuration = Configuration.builder().options(Option.SUPPRESS_EXCEPTIONS).build();

    private final JsonPath jsonPath;

    public JsonPathMatcher(String expression) {
        this.jsonPath = JsonPath.compile(expression);
    }

    protected abstract String getMatchInput(RtbExecutionContext<?, ?> execContext, MatchContext matchContext);

    @Override
    public boolean doMatch(RtbExecutionContext<?, ?> execContext, MatchContext matchContext) {
        String input = getMatchInput(execContext, matchContext);
        if (input != null) {
            DocumentContext context = JsonPath.parse(input, configuration);
            if (context.read(jsonPath) == null) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
}

class JsonPathRtbRequestStringMatcher extends JsonPathMatcher implements RtbRequestMatcher {

    public JsonPathRtbRequestStringMatcher(String expression) {
        super(expression);
    }

    @Override
    protected String getMatchInput(RtbExecutionContext<?, ?> execContext, MatchContext matchContext) {
        return execContext.getRtbRequestString();
    }
}

class JsonPathRtbResponseStringMatcher extends JsonPathMatcher implements RtbResponseMatcher {

    public JsonPathRtbResponseStringMatcher(String expression) {
        super(expression);
    }

    @Override
    protected String getMatchInput(RtbExecutionContext<?, ?> execContext, MatchContext matchContext) {
        return execContext.getRtbResponseString();
    }
}
