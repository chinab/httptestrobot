package kr.hayarobee.httptest.robot.model;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import kr.hayarobee.httptest.robot.job.CodeBreaker;
import kr.hayarobee.httptest.robot.model.Codebook;
import kr.hayarobee.httptest.robot.model.ItemPair;
import kr.hayarobee.httptest.robot.model.ItemSingle;
import kr.hayarobee.httptest.robot.model.Robot;

import org.junit.Before;
import org.junit.Test;

public class ItemPairTest implements CodeBreaker {
	private Codebook codebook = new Codebook();

	private ItemSingle item1;
	private ItemSingle item2;
	private ItemPair pair;

	@Before
	public void setUp() {
		this.item1 = new ItemSingle("key1", "default1");
		this.item2 = new ItemSingle("key2", "default2");
		this.pair = new ItemPair();
	}
	
	@Test
	public void testAssociateValueItem() {
		this.pair.link(this.item1, this.item2);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testDifferentSizeItemLink() {
		this.item1.add("value1", "value2");
		this.pair.link(this.item1, this.item2);
	}
	
	@Test
	public void testGetValue() {
		this.item1.add("value11", "value12", "value13");
		this.item2.add("value21", "value22", "value23");
		this.pair.link(this.item1, this.item2);
		Map<String, String> defaultValue = this.pair.getDefaultValue(this);
		assertThat(defaultValue.get("key1"), is("default1"));
		assertThat(defaultValue.get("key2"), is("default2"));
		
		List<String> values1 = this.item1.getValues();
		List<String> values2 = this.item2.getValues();
		Map<String, String> value = this.pair.getValue(this);
		String value1 = value.get("key1");
		String value2 = value.get("key2");
		assertTrue(values1.contains(value1));
		assertTrue(values2.contains(value2));
		assertThat(values1.indexOf(value1), is(values2.indexOf(value2)));
	}
	
	@Test
	public void testManyLinkedValue() {
		this.item1.add("value11", "value12");
		this.item2.add("value21", "value22");
		ItemSingle item3 = new ItemSingle("key3", "default3");
		item3.add("value31", "value32");
		this.pair.link(this.item1, this.item2, item3);
		List<String> values1 = this.item1.getValues();
		List<String> values2 = this.item2.getValues();
		List<String> values3 = item3.getValues();
		Map<String, String> value = this.pair.getValue(this);
		String value1 = value.get("key1");
		String value2 = value.get("key2");
		String value3 = value.get("key3");
		assertTrue(values1.contains(value1));
		assertTrue(values2.contains(value2));
		assertTrue(values3.contains(value3));
		assertThat(values1.indexOf(value1), is(values2.indexOf(value2)));
		assertThat(values2.indexOf(value2), is(values3.indexOf(value3)));
	}

	@Override
	public String readMacroCode(String code) {
		Robot robot = mock(Robot.class);
		when(robot.getName()).thenReturn("robot1");
		when(robot.getCurrentJobRepeatCounter()).thenReturn("1/100");
		return this.codebook.read(code, robot);
	}
}
