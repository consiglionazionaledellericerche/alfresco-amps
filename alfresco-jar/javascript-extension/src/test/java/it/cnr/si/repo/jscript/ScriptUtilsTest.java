package it.cnr.si.repo.jscript;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import it.cnr.si.repo.jscript.exception.TransformationException;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class ScriptUtilsTest {

	@Test
	public void testExecuteStatic() throws Exception {
		assertEquals(2, ScriptUtils.executeStatic("java.lang.Math.abs",
				new Integer(-2)));
	}

	@Test
	public void testExecuteStaticPrimitive() throws Exception {
		assertEquals(2, ScriptUtils.executeStatic("java.lang.Math.abs", -2));
	}

	@Test
	public void testExecuteStaticPrimitiveDouble() throws Exception {
		assertEquals(2.0, ScriptUtils.executeStatic("java.lang.Math.sqrt", 4.0));
	}

	@Test
	public void testExecuteStaticPrimitiveBoolean() throws Exception {
		assertEquals(true,
				ScriptUtils.executeStatic("java.lang.Boolean.valueOf", true));
	}

	@Test
	public void testConstant() throws Exception {
		assertEquals(Math.PI, ScriptUtils.constant("java.lang.Math.PI"));
	}

	@Test
	public void testTransformMap() throws Exception {

		Map<Integer, String> m = new HashMap<Integer, String>();

		m.put(1, "gennaio");
		m.put(2, "febbraio");
		m.put(3, "marzo");

		assertTrue(ScriptUtils.transformMap(m).values().containsAll(m.values()));
	}

	@Test(expected = TransformationException.class)
	public void testTransformMapWithSameToString() throws Exception {

		Map<EvilClass, String> m = new HashMap<EvilClass, String>();

		m.put(new EvilClass("gennaio"), "gennaio");
		m.put(new EvilClass("febbraio"), "febbraio");
		m.put(new EvilClass("marzo"), "marzo");
		ScriptUtils.transformMap(m);
	}

	class EvilClass {

		@SuppressWarnings("unused")
		private String s;

		public EvilClass(String s) {
			this.s = s;
		}

		public String toString() {
			return "ALWAYS THE SAME toString()";
		}
	}
}
