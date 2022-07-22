package com.rrm.fulltextsearch.performance_test

import org.junit.jupiter.api.Tag

/**
 * This annotation is used to indicate that a test is a performance test. There
 * is an associated Gradle task for executing these tests, which is defined in
 * build.gradle.
 */
@Target(AnnotationTarget.CLASS)
@Retention
@Tag("performance-test")
annotation class PerformanceTest