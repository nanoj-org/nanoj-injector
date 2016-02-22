package org.nanoj.injector.tools;

import org.nanoj.injector.tools.ClassTools;

import junit.framework.TestCase;

public class TestClassTools extends TestCase {

	public void cutLastLevel(String s, String expectedResult) {
		String result = ClassTools.cutLastLevel(s);
		System.out.println(" . '" + s + "' --> '" + result + "'");		
		assertEquals( expectedResult, result ) ;
	}
	public void testCutLastLevel() {
		System.out.println("--- ClassTools.cutLastLevel()");
//		String s ;
		
//		s = ClassTools.cutLastLevel("aaa.bbb.ccc");
//		assertTrue( s.equals("aaa.bbb") ) ;
		cutLastLevel("aaa.bbb.ccc", "aaa.bbb");

//		s = ClassTools.cutLastLevel("aaa.bbb");
//		assertTrue( s.equals("aaa") ) ;
		cutLastLevel("aaa.bbb", "aaa");

//		s = ClassTools.cutLastLevel("aaa");
//		assertTrue( s.equals("") ) ;
		cutLastLevel("aaa", "");

//		s = ClassTools.cutLastLevel("");
//		assertTrue( s.equals("") ) ;
		cutLastLevel("", "");
	}

	public void testGetPackageLevel() {
		int n ;

		n = ClassTools.getPackageLevel("${package-1}.ccc" );
		assertEquals(1, n);
		n = ClassTools.getPackageLevel("aa.${package-1}.ccc" );
		assertEquals(1, n);
		n = ClassTools.getPackageLevel("aa.bbb${package-1}.ccc" );
		assertEquals(1, n);
		n = ClassTools.getPackageLevel("aa.bbb${package-1 }.ccc" );
		assertEquals(1, n);
		n = ClassTools.getPackageLevel("aa.bbb.ccc${package-1}" );
		assertEquals(1, n);
		
		n = ClassTools.getPackageLevel("${package-2}.ccc" );
		assertEquals(2, n);
		n = ClassTools.getPackageLevel("aa.bbb${package-2}.ccc" );
		assertEquals(2, n);
		
		n = ClassTools.getPackageLevel("${package-10}.ccc" );
		assertEquals(10, n);
		n = ClassTools.getPackageLevel("aa.bbb${package-12}.ccc" );
		assertEquals(12, n);
	}

	public void print(String s) {
		System.out.println("'" + s + "'");
	}
	
	public void testGetPackageVar() {
		String s ;
		s = ClassTools.getPackageVar("${package-1}.ccc" );
		print(s);
		assertTrue( s.equals("${package-1}") ) ;

		s = ClassTools.getPackageVar("aa.${package-1}.ccc" );
		print(s);
		assertTrue( s.equals("${package-1}") ) ;
		
		s = ClassTools.getPackageVar("aa.bbb${package-1}.ccc" );
		print(s);
		assertTrue( s.equals("${package-1}") ) ;
		
		s = ClassTools.getPackageVar("aa.bbb${package-1 }.ccc" );
		print(s);
		assertTrue( s.equals("${package-1 }") ) ;
		
		s = ClassTools.getPackageVar("aa.bbb.ccc${package-1}" );
		print(s);
		assertTrue( s.equals("${package-1}") ) ;
		
		s = ClassTools.getPackageVar("${package-2}.ccc" );
		print(s);
		assertTrue( s.equals("${package-2}") ) ;

		s = ClassTools.getPackageVar("aa.bbb${package-2}.ccc" );
		print(s);
		assertTrue( s.equals("${package-2}") ) ;
		
		s = ClassTools.getPackageVar("${package-10}.ccc" );
		print(s);
		assertTrue( s.equals("${package-10}") ) ;
		
		s = ClassTools.getPackageVar("aa.bbb${package-12}.ccc" );
		print(s);
		assertTrue( s.equals("${package-12}") ) ;
	}
	
	public void testApplyClassOnPattern() {
		String s ;
		
		s = ClassTools.applyClassOnPattern(String.class, "aaa.bbb.${class}");
		print(s);
		assertTrue( s.equals("aaa.bbb.String") ) ;

		s = ClassTools.applyClassOnPattern(String.class, "aaa.bbb.${class}Impl");
		print(s);
		assertTrue( s.equals("aaa.bbb.StringImpl") ) ;

		s = ClassTools.applyClassOnPattern(String.class, "${package}.${class}Impl");
		print(s);
		assertTrue( s.equals("java.lang.StringImpl") ) ;

		s = ClassTools.applyClassOnPattern(String.class, "${package}.impl.${class}");
		print(s);
		assertTrue( s.equals("java.lang.impl.String") ) ;

		s = ClassTools.applyClassOnPattern(String.class, "${package}.impl.${class}Impl");
		print(s);
		assertTrue( s.equals("java.lang.impl.StringImpl") ) ;

		
		s = ClassTools.applyClassOnPattern(String.class, "${package-1}.${class}");
		print(s);
		assertTrue( s.equals("java.String") ) ;

		s = ClassTools.applyClassOnPattern(String.class, "${package-1}.${class}Impl");
		print(s);
		assertTrue( s.equals("java.StringImpl") ) ;

		s = ClassTools.applyClassOnPattern(String.class, "${package-2}.${class}Impl");
		print(s);
		assertTrue( s.equals("StringImpl") ) ;
		
		s = ClassTools.applyClassOnPattern(ClassTools.class, "${package-3}.${class}Impl");
		print(s);
		assertTrue( s.equals("org.ClassToolsImpl") ) ;
		
		
	}
	
}
