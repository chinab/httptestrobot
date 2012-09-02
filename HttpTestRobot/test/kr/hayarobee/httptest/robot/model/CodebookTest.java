package kr.hayarobee.httptest.robot.model;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import kr.hayarobee.httptest.robot.model.Codebook;
import kr.hayarobee.httptest.robot.model.Robot;

import org.junit.Test;

public class CodebookTest {

	@Test
	public void testSimpleRead() {
		String code = "$ADMIN$";
		Robot robot = mock(Robot.class);
		Codebook book = new Codebook();
		assertThat(book.read(code, robot), is("admin"));
	}
	
	@Test
	public void testDuplicateCodeRead() {
		String code = "$ADMIN$ $ADMIN$ $ADMIN$ $ADMIN$";
		Robot robot = mock(Robot.class);
		when(robot.getName()).thenReturn("robot-1");
		Codebook book = new Codebook();
		assertThat(book.read(code, robot), is("admin admin admin admin"));
	}

	@Test
	public void testMultiComplexCodeRead() {
		String code = "$ADMIN$ is not Robot $ROBOT$";
		Robot robot = mock(Robot.class);
		when(robot.getName()).thenReturn("robot-1");
		Codebook book = new Codebook();
		assertThat(book.read(code, robot), is("admin is not Robot robot-1"));
	}
}
