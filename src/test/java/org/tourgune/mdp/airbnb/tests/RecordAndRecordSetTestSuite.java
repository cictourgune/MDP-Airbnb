package org.tourgune.mdp.airbnb.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ RecordExpectedUsageTest.class, RecordCornerCasesTest.class })
public class RecordAndRecordSetTestSuite {}