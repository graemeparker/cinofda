package com.adfonic.webservices.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import com.adfonic.webservices.util.WSFixture.Format;

/*
 * wicked composite - ok for test
 */
public class MultiGet extends AbstractGetHandler {

    public MultiGet(WSFixture fixture, AbstractGetHandler... getters) {
        super(fixture);
        this.getters = getters;
    }


    public MultiGet getVerifyingMultiGet(int status, boolean invert) {// use current as protoype
        // memoize this instead of creatin
        MultiGet nMG = new MultiGet(f, getters);
        nMG.verifyFlag = true;
        nMG.invertFlag = invert;
        nMG.status = status;
        return (nMG);
    }

    AbstractGetHandler getter;


    @Override
    protected Format getFormat() {
        return (getter.getFormat());
    }


    @Override
    protected <T> T parseResponseEntity(InputStream entity, Class<T> clazz) throws IOException {
        return (getter.parseResponseEntity(entity, clazz));
    }

    GeneralAbort abort = new JunitFailer();

    private int status;
    private boolean invertFlag;
    private boolean verifyFlag;


    @Override
    protected void verifyStatus(int status) {
        System.out.println("Response.Status.Code: " + status);
        if (verifyFlag && ((this.status == status) == invertFlag)) {
            abort.abort("Return code - " + status);
        }
        getter.verifyStatus(status);
    }

    AbstractGetHandler[] getters;

    private boolean compareFlag = true;


    // private boolean compareFlag=false;//for now
    public void setComparisonFlag(boolean compareFlag) {
        this.compareFlag = compareFlag;
    }

    private int candidateIndex = 0;


    // int candidateIndex=2;//for now mandate xml
    public void specifyCandidateIndex(int idx) {
        this.candidateIndex = idx;
    }


    public <T> T get(Class<T> clazz, String path, String... params) throws Exception {
        T candidate = null;
        int count = 0;
        for (AbstractGetHandler g : getters) {
            getter = g;
            T currentT = super.get(clazz, path, params);
            if (candidate == null) {
                candidate = currentT;
            }

            if (compareFlag) {
                if ((currentT != candidate) && ((!clazz.isArray() && !candidate.equals(currentT)) || (clazz.isArray() && !arrayEquals((T[]) candidate, (T[]) currentT)))) {
                    abort.abort("Responses not equal:\nRm<<\n" + candidate + "\n>>\nRn<<" + currentT + "\n>>/n");
                }
            }

            if (++count == candidateIndex) {
                candidate = currentT;
            }
        }
        return (candidate);
    }


    private <T> boolean arrayEquals(T[] a1, T[] a2) {
        return (Arrays.deepEquals(a1, a2));
    }

}
