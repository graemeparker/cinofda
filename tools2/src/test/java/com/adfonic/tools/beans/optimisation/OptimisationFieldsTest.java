package com.adfonic.tools.beans.optimisation;

import static com.adfonic.tools.beans.optimisation.OptimisationFields.COLUMN_KEY_ECPA;
import static com.adfonic.tools.beans.optimisation.OptimisationFields.COLUMN_KEY_ECPC;
import static com.adfonic.tools.beans.optimisation.OptimisationFields.COLUMN_KEY_ECPM;
import static com.adfonic.tools.beans.optimisation.OptimisationFields.COLUMN_KEY_IAB_CATEGORY;
import static com.adfonic.tools.beans.optimisation.OptimisationFields.COLUMN_KEY_INVENTORY_SOURCE;
import static com.adfonic.tools.beans.optimisation.OptimisationFields.COLUMN_KEY_PUBLICATION_NAME;
import static com.adfonic.tools.beans.optimisation.OptimisationFields.COLUMN_KEY_PUBLICATION_TYPE;
import static com.adfonic.tools.beans.optimisation.OptimisationFields.COLUMN_KEY_PUB_EXT_ID;
import static com.adfonic.tools.beans.optimisation.OptimisationFields.isFieldVisible;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.domain.OptimisationReportFields;

public class OptimisationFieldsTest {

    private Set<OptimisationReportFields> reportFields;

    @Before
    public void before() {
        reportFields = Collections.emptySet();
    }

    @After
    public void after() {
        reportFields = null;
    }

    @Test
    public void testNullOptimisationField() {
        reportFields = null;

        // AT-1020 if name and pid are both suppressed id must be displayed
        showFields(COLUMN_KEY_PUB_EXT_ID);

        hideFields(COLUMN_KEY_ECPA, COLUMN_KEY_ECPC, COLUMN_KEY_ECPM, COLUMN_KEY_IAB_CATEGORY, COLUMN_KEY_INVENTORY_SOURCE,
                COLUMN_KEY_PUBLICATION_NAME, COLUMN_KEY_PUBLICATION_TYPE);
    }

    @Test
    public void testEmptyOptimisationField() {
        reportFields = Collections.emptySet();

        // AT-1020 if name and pid are both suppressed id must be displayed
        showFields(COLUMN_KEY_PUB_EXT_ID);

        hideFields(COLUMN_KEY_ECPA, COLUMN_KEY_ECPC, COLUMN_KEY_ECPM, COLUMN_KEY_IAB_CATEGORY, COLUMN_KEY_INVENTORY_SOURCE,
                COLUMN_KEY_PUBLICATION_NAME, COLUMN_KEY_PUBLICATION_TYPE);
    }

    @Test
    public void testEcpaOptimisationField() {
        reportFields = Collections.singleton(OptimisationReportFields.ECPA);

        // AT-1020 if name and pid are both suppressed id must be displayed
        showFields(COLUMN_KEY_PUB_EXT_ID, COLUMN_KEY_ECPA);

        hideFields(COLUMN_KEY_ECPC, COLUMN_KEY_ECPM, COLUMN_KEY_IAB_CATEGORY, COLUMN_KEY_INVENTORY_SOURCE, COLUMN_KEY_PUBLICATION_NAME,
                COLUMN_KEY_PUBLICATION_TYPE);
    }

    @Test
    public void testEcpcOptimisationField() {
        reportFields = Collections.singleton(OptimisationReportFields.ECPC);

        // AT-1020 if name and pid are both suppressed id must be displayed
        showFields(COLUMN_KEY_PUB_EXT_ID, COLUMN_KEY_ECPC);

        hideFields(COLUMN_KEY_ECPA, COLUMN_KEY_ECPM, COLUMN_KEY_IAB_CATEGORY, COLUMN_KEY_INVENTORY_SOURCE, COLUMN_KEY_PUBLICATION_NAME,
                COLUMN_KEY_PUBLICATION_TYPE);
    }

    @Test
    public void testEcpmOptimisationField() {
        reportFields = Collections.singleton(OptimisationReportFields.ECPM);

        // AT-1020 if name and pid are both suppressed id must be displayed
        showFields(COLUMN_KEY_PUB_EXT_ID, COLUMN_KEY_ECPM);

        hideFields(COLUMN_KEY_ECPA, COLUMN_KEY_ECPC, COLUMN_KEY_IAB_CATEGORY, COLUMN_KEY_INVENTORY_SOURCE, COLUMN_KEY_PUBLICATION_NAME,
                COLUMN_KEY_PUBLICATION_TYPE);
    }

    @Test
    public void testEcpmEcpcEcpaOptimisationFields() {
        reportFields = new HashSet<OptimisationReportFields>(Arrays.asList(OptimisationReportFields.ECPA, OptimisationReportFields.ECPC,
                OptimisationReportFields.ECPM));

        // AT-1020 if name and pid are both suppressed id must be displayed
        showFields(COLUMN_KEY_PUB_EXT_ID, COLUMN_KEY_ECPA, COLUMN_KEY_ECPC, COLUMN_KEY_ECPM);

        hideFields(COLUMN_KEY_IAB_CATEGORY, COLUMN_KEY_INVENTORY_SOURCE, COLUMN_KEY_PUBLICATION_NAME, COLUMN_KEY_PUBLICATION_TYPE);
    }

    @Test
    public void showAllOptimisationFields() {
        reportFields = new HashSet<OptimisationReportFields>(Arrays.asList(OptimisationReportFields.ECPA, OptimisationReportFields.ECPC,
                OptimisationReportFields.ECPM, OptimisationReportFields.IAB_CATEGORY, OptimisationReportFields.INVENTORY_SOURCE,
                OptimisationReportFields.PID, OptimisationReportFields.PUBLICATION_NAME, OptimisationReportFields.SITE_APP));

        showFields(COLUMN_KEY_PUB_EXT_ID, COLUMN_KEY_ECPA, COLUMN_KEY_ECPC, COLUMN_KEY_ECPM, COLUMN_KEY_IAB_CATEGORY,
                COLUMN_KEY_INVENTORY_SOURCE, COLUMN_KEY_PUBLICATION_NAME, COLUMN_KEY_PUBLICATION_TYPE);
    }

    @Test
    public void noPidButPubNameOptimisationFields() {
        reportFields = new HashSet<OptimisationReportFields>(Arrays.asList(OptimisationReportFields.ECPA, OptimisationReportFields.ECPC,
                OptimisationReportFields.ECPM, OptimisationReportFields.IAB_CATEGORY, OptimisationReportFields.INVENTORY_SOURCE,
                OptimisationReportFields.PUBLICATION_NAME, OptimisationReportFields.SITE_APP));

        showFields(COLUMN_KEY_ECPA, COLUMN_KEY_ECPC, COLUMN_KEY_ECPM, COLUMN_KEY_IAB_CATEGORY, COLUMN_KEY_INVENTORY_SOURCE,
                COLUMN_KEY_PUBLICATION_NAME, COLUMN_KEY_PUBLICATION_TYPE);

        hideFields(COLUMN_KEY_PUB_EXT_ID);
    }

    @Test
    public void otherOptimisationFields() {
        reportFields = new HashSet<OptimisationReportFields>(Arrays.asList(OptimisationReportFields.ECPA, OptimisationReportFields.ECPC,
                OptimisationReportFields.ECPM, OptimisationReportFields.IAB_CATEGORY, OptimisationReportFields.INVENTORY_SOURCE,
                OptimisationReportFields.PUBLICATION_NAME, OptimisationReportFields.SITE_APP));

        showFields(COLUMN_KEY_ECPA, COLUMN_KEY_ECPC, COLUMN_KEY_ECPM, COLUMN_KEY_IAB_CATEGORY, COLUMN_KEY_INVENTORY_SOURCE,
                COLUMN_KEY_PUBLICATION_NAME, COLUMN_KEY_PUBLICATION_TYPE, "CustomColumn", "CustomColumn2");

        hideFields(COLUMN_KEY_PUB_EXT_ID);
    }

    private void showFields(String... columnKeys) {
        for (String columnKey : columnKeys) {
            assertTrue(columnKey + " should be visible", isFieldVisible(columnKey, reportFields));
        }
    }

    private void hideFields(String... columnKeys) {
        for (String columnKey : columnKeys) {
            assertFalse(columnKey + " should be hidden", isFieldVisible(columnKey, reportFields));
        }
    }
}
