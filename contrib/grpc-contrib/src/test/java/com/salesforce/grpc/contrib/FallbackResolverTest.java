/*
 *  Copyright (c) 2019, Salesforce.com, Inc.
 *  All rights reserved.
 *  Licensed under the BSD 3-Clause license.
 *  For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.grpc.contrib;

import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.*;

@SuppressWarnings("ALL")
public class FallbackResolverTest {

    private final NameResolverFakes fakes = new NameResolverFakes();
    @Test
    public void firstShouldFind() throws Exception {
        NameResolver fakeResolver = new FakeResolver();
        FakeResolverProvider canResolve = new FakeResolverProvider("aaa://", fakeResolver);
        FakeResolverProvider cannotResolve = new FakeResolverProvider("bbb://", null);

        NameResolver.Factory factory = FallbackResolver.startWith(canResolve).thenCheck(cannotResolve);

        assertEquals(fakeResolver, factory.newNameResolver(new URI("aaa://foo"), fakes.fakeArgs));
    }

    @Test
    public void secondShouldFind() throws Exception {
        NameResolver fakeResolver = new FakeResolver();
        FakeResolverProvider canResolve = new FakeResolverProvider("aaa://", fakeResolver);
        FakeResolverProvider cannotResolve = new FakeResolverProvider("bbb://", null);

        NameResolver.Factory factory = FallbackResolver.startWith(cannotResolve).thenCheck(canResolve);

        assertEquals(fakeResolver, factory.newNameResolver(new URI("bbb://foo"), fakes.fakeArgs));
    }

    @Test
    public void neitherShouldFind() throws Exception {
        FakeResolverProvider cannotResolve = new FakeResolverProvider("bbb://", null);

        NameResolver.Factory factory = FallbackResolver.startWith(cannotResolve).thenCheck(cannotResolve);

        assertNull(factory.newNameResolver(new URI("bbb://foo"), fakes.fakeArgs));
    }

    @Test
    public void firstSchemeIsDefaultScheme() {
        NameResolver fakeResolver = new FakeResolver();
        FakeResolverProvider canResolve = new FakeResolverProvider("aaa://", fakeResolver);
        FakeResolverProvider cannotResolve = new FakeResolverProvider("bbb://", null);

        NameResolver.Factory factory = FallbackResolver.startWith(canResolve).thenCheck(cannotResolve);

        assertEquals("aaa://", factory.getDefaultScheme());
    }

    private static class FakeResolverProvider extends NameResolverProvider {
        private final String scheme;
        private final NameResolver resolver;

        public FakeResolverProvider(String scheme, NameResolver resolver) {
            this.scheme = scheme;
            this.resolver = resolver;
        }

        @Override
        protected boolean isAvailable() {
            return true;
        }

        @Override
        protected int priority() {
            return 0;
        }

        @Override
        public NameResolver newNameResolver(URI targetUri, NameResolver.Args args) {
            return resolver;
        }

        @Override
        public String getDefaultScheme() {
            return scheme;
        }
    }

    private static class FakeResolver extends NameResolver {

        @Override
        public String getServiceAuthority() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void start(Listener listener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void shutdown() {
            throw new UnsupportedOperationException();
        }
    }
}
