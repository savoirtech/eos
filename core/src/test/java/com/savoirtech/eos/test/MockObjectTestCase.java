package com.savoirtech.eos.test;

import org.junit.Assert;
import org.junit.Before;
import org.mockito.MockitoAnnotations;

public abstract class MockObjectTestCase extends Assert {
    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }
}
