package de.envite.proa.util;

import org.junit.jupiter.api.Test;

import static io.smallrye.common.constraint.Assert.*;

public class LabelMatcherTest {

	@Test
	public void testInitialization() {
		LabelMatcher lm = new LabelMatcher();
		assertNotNull(lm);
	}

	@Test
	public void testIsSimilar_ShouldReturnFalseIfBothStringsAreNull() {
		assertFalse(LabelMatcher.isSimilar(null, null));
	}

	@Test
	public void testIsSimilar_ShouldReturnFalseIfFirstStringIsNull() {
		assertFalse(LabelMatcher.isSimilar(null, "Order Dispatched"));
	}

	@Test
	public void testIsSimilar_ShouldReturnFalseIfSecondStringIsNull() {
		assertFalse(LabelMatcher.isSimilar("Order Dispatched", null));
	}

	@Test
	public void testIsSimilar_IdenticalShouldBeTrue() {
		assertTrue(LabelMatcher.isSimilar("Order Dispatched", "Order Dispatched"));
	}

	@Test
	public void testIsSimilar_DifferentCaseShouldBeTrue() {
		assertTrue(LabelMatcher.isSimilar("Order Dispatched", "order dispatched"));
	}

	@Test
	public void testIsSimilar_DifferentWordOrderShouldBeTrue() {
		assertTrue(LabelMatcher.isSimilar("Order Dispatched", "Dispatched Order"));
	}

	@Test
	public void testIsSimilar_SimilarShouldBeTrue() {
		assertTrue(LabelMatcher.isSimilar("Order Accepted", "Accept Order"));
	}

	@Test
	public void testIsSimilar_ExtraWordShouldBeTrue() {
		assertTrue(LabelMatcher.isSimilar("Order Dispatched", "Order was Dispatched"));
	}

	@Test
	public void testIsSimilar_MinorTypoShouldBeTrue() {
		assertTrue(LabelMatcher.isSimilar("Order Dispatched", "Ordr Dispatched"));
	}

	@Test
	public void testIsSimilar_DifferentContextShouldBeFalse() {
		assertFalse(LabelMatcher.isSimilar("Order Dispatched", "Order Cancelled"));
	}

	@Test
	public void testIsSimilar_CompletelyDifferentShouldBeFalse() {
		assertFalse(LabelMatcher.isSimilar("Order Dispatched", "Payment Received"));
	}

	@Test
	public void testIsSimilar_PartialMatchShouldBeFalse() {
		assertFalse(LabelMatcher.isSimilar("Order Dispatched", "Order"));
	}

	@Test
	public void testIsSimilar_DifferentPluralizationShouldBeTrue() {
		assertTrue(LabelMatcher.isSimilar("Order Processed", "Orders Processed"));
	}

	@Test
	public void testIsSimilar_DifferentTenseShouldBeTrue() {
		assertTrue(LabelMatcher.isSimilar("Order Processing", "Order Processed"));
	}

	@Test
	public void testIsSimilar_WhitespacesShouldBeIgnored() {
		assertTrue(LabelMatcher.isSimilar("Order Dispatched", "Order    Dispatched"));
	}

	@Test
	public void testIsSimilar_SpecialCharactersShouldBeIgnored() {
		assertTrue(LabelMatcher.isSimilar("Order Dispatched", "Order Dispatched!"));
	}
}
