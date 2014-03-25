/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.integtests.resolve.ivy

import org.gradle.api.artifacts.resolution.JvmLibraryJavadocArtifact
import org.gradle.api.artifacts.resolution.JvmLibrarySourcesArtifact
import org.gradle.integtests.fixtures.AbstractDependencyResolutionTest
import org.gradle.integtests.resolve.JvmLibraryArtifactResolveTestFixture

class IvyJvmLibraryArtifactResolutionIntegrationTest extends AbstractDependencyResolutionTest {
    def repo = ivyHttpRepo
    def module = repo.module("some.group", "some-artifact", "1.0")
    JvmLibraryArtifactResolveTestFixture fixture

    def setup() {
        server.start()
        fixture = new JvmLibraryArtifactResolveTestFixture(buildFile)
        fixture.withRepository("ivy { url '$repo.uri' }")

        publishModule()
    }

    def "resolve sources artifacts"() {
        fixture.requestingTypes(JvmLibrarySourcesArtifact)
                .expectSourceArtifact("some-artifact-1.0-my-sources.jar")
                .prepare()

        when:
        module.ivy.expectGet()
        module.getArtifact(classifier: "my-sources").expectGet()

        then:
        checkArtifactsResolvedAndCached()
    }

    def "resolve javadoc artifacts"() {
        fixture.requestingTypes(JvmLibraryJavadocArtifact)
                .expectJavadocArtifact("some-artifact-1.0-my-javadoc.jar")
                .prepare()

        when:
        module.ivy.expectGet()
        module.getArtifact(classifier: "my-javadoc").expectGet()

        then:
        checkArtifactsResolvedAndCached()
    }

    def "resolve all artifacts"() {
        fixture.requestingTypes()
                .expectSourceArtifact("some-artifact-1.0-my-sources.jar")
                .expectJavadocArtifact("some-artifact-1.0-my-javadoc.jar")
                .prepare()

        when:
        module.ivy.expectGet()
        module.getArtifact(classifier: "my-sources").expectGet()
        module.getArtifact(classifier: "my-javadoc").expectGet()

        then:
        checkArtifactsResolvedAndCached()
    }

    // TODO:DAZ These tests should use a changing module and regular cache expiry
    def "fetches missing artifacts for module with --refresh-dependencies"() {
        fixture.requestingTypes(JvmLibrarySourcesArtifact)
                .expectSourceArtifactNotFound("some-artifact-my-sources.jar")
                .prepare()

        when:
        module.ivy.expectGet()
        module.getArtifact(classifier: "my-sources").expectGetMissing()

        then:
        checkArtifactsResolvedAndCached()

        when:
        executer.withArgument("--refresh-dependencies")
        fixture.clearExpectations()
                .expectSourceArtifact("some-artifact-1.0-my-sources.jar")
                .createVerifyTask("verifyRefresh")

        and:
        module.ivy.expectHead()
        module.getArtifact(classifier: "my-sources").expectGet()

        then:
        succeeds("verifyRefresh")
    }

    def "updates artifacts for module with --refresh-dependencies"() {
        final sourceArtifact = module.getArtifact(classifier: "my-sources")
        fixture.requestingTypes(JvmLibrarySourcesArtifact)
                .expectSourceArtifact("some-artifact-1.0-my-sources.jar")
                .prepare()

        when:
        module.ivy.expectGet()
        sourceArtifact.expectGet()

        then:
        checkArtifactsResolvedAndCached()

        when:
        def snapshot = file("sources/some-artifact-1.0-my-sources.jar").snapshot()
        module.publishWithChangedContent()
        executer.withArgument("--refresh-dependencies")

        and:
        module.ivy.expectHead()
        module.ivy.sha1.expectGet()
        module.ivy.expectGet()
        sourceArtifact.expectHead()
        sourceArtifact.sha1.expectGet()
        sourceArtifact.expectGet()

        then:
        succeeds("verify")
        file("sources/some-artifact-1.0-my-sources.jar").assertHasChangedSince(snapshot)
    }

    def "resolve artifacts of non-existing component"() {
        fixture.expectComponentNotFound().prepare()

        when:
        module.ivy.expectGetMissing()
        module.jar.expectHeadMissing()

        then:
        succeeds("verify")
    }

    def "resolve missing artifacts of existing component"() {
        fixture.expectSourceArtifactNotFound("some-artifact-my-sources.jar")
                .expectJavadocArtifactNotFound("some-artifact-my-javadoc.jar")
                .prepare()

        when:
        module.ivy.expectGet()
        module.getArtifact(classifier: "my-sources").expectGetMissing()
        module.getArtifact(classifier: "my-javadoc").expectGetMissing()

        then:
        checkArtifactsResolvedAndCached()
    }

    def "resolve partially missing artifacts"() {
        fixture.expectSourceArtifact("some-artifact-1.0-my-sources.jar")
                .expectJavadocArtifactNotFound("some-artifact-my-javadoc.jar")
                .prepare()

        when:
        module.ivy.expectGet()
        module.getArtifact(classifier: "my-sources").expectGet()
        module.getArtifact(classifier: "my-javadoc").expectGetMissing()

        then:
        checkArtifactsResolvedAndCached()
    }

    def "resolve partially broken artifacts"() {
        fixture.expectSourceArtifact("some-artifact-1.0-my-sources.jar")
                .expectJavadocArtifactFailure("Could not download artifact 'some.group:some-artifact:1.0:some-artifact-my-javadoc.jar'")
                .prepare()

        when:
        module.ivy.expectGet()
        module.getArtifact(classifier: "my-sources").expectGet()
        module.getArtifact(classifier: "my-javadoc").expectGetBroken()

        then:
        succeeds("verify")

        when:
        fixture.clearExpectations()
                .expectSourceArtifact("some-artifact-1.0-my-sources.jar")
                .expectJavadocArtifact("some-artifact-1.0-my-javadoc.jar")
                .createVerifyTask("verifyFixed")

        and:
        server.resetExpectations()
        // Only the broken artifact is not cached
        module.getArtifact(classifier: "my-javadoc").expectGet()

        then:
        succeeds("verifyFixed")
    }

    def checkArtifactsResolvedAndCached() {
        assert succeeds("verify")
        server.resetExpectations()
        assert succeeds("verify")
        true
    }

    private publishModule() {
        module.configuration("sources")
        module.configuration("javadoc")
        // use uncommon classifiers that are different from those used by maven, 
        // in order to prove that artifact names don't matter
        module.artifact(type: "source", classifier: "my-sources", ext: "jar", conf: "sources")
        module.artifact(type: "javadoc", classifier: "my-javadoc", ext: "jar", conf: "javadoc")
        module.publish()
    }
}