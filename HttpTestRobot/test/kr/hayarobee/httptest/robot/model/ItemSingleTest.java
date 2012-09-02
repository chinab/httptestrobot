package kr.hayarobee.httptest.robot.model;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.List;

import kr.hayarobee.httptest.robot.job.CodeBreaker;
import kr.hayarobee.httptest.robot.model.Codebook;
import kr.hayarobee.httptest.robot.model.ItemSingle;
import kr.hayarobee.httptest.robot.model.Robot;

import org.junit.Test;

public class ItemSingleTest implements CodeBreaker {
	private Codebook codebook = new Codebook();
	
	@Test
	public void testSimpleItem() {
		ItemSingle item = new ItemSingle("key", "value");
		assertThat(item.getKey(), is("key"));
		assertThat(item.getDefaultValue(this), is("value"));
	}

	@Test
	public void testHasListValueItem() {
		ItemSingle item = new ItemSingle("key", "default");
		item.add("value1", "value2", "value3");
		List<String> values = item.getValues();
		assertThat(values.size(), is(4));
		assertThat(item.getDefaultValue(this), is("default"));
		assertTrue(values.contains(item.getValue(this)));
	}

	@Override
	public String readMacroCode(String code) {
		Robot robot = mock(Robot.class);
		when(robot.getName()).thenReturn("robot1");
		when(robot.getCurrentJobRepeatCounter()).thenReturn("1/100");
		return this.codebook.read(code, robot);
	}

}
