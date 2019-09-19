import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;


import java.net.MalformedURLException;

import org.junit.Test;


public class unitTest {
	

	@Test
	public void testReindexValidity() throws MalformedURLException, IOException {
		
		String invalidIndex = "null";
		String testIndex = "bm25";
		assertFalse(reindex.reindexDocuments(invalidIndex, testIndex));
	}
	
	@Test
	public void baosTest() throws Exception 
	{
		InputStream mockInputStream = new ByteArrayInputStream("test dataUsage:  Most options can be ignored.  The only one most folks will needis the -q flag, to indicate whether to output official results for individual queries as well as the averages over all queries.  Official TREC usagemight be something like trec_eval -q -c -M1000 official_qrels submitted_results ".getBytes());
	    String acceptString = "test dataUsage:  Most options can be ignored.  The only one most folks will needis the -q flag, to indicate whether to output official results for individual queries as well as the averages over all queries.  Official TREC usagemight be something like trec_eval -q -c -M1000 official_qrels submitted_results ";
	    assertEquals(acceptString, queries.readFully(mockInputStream).toString());
	}
	
	@Test
	public void testParse() throws Exception 
	{
	    String testString = "<>0(*&f,+[<0-9>],)?@";
	    String acceptString = "0(*&f,+[],)?@";
	    assertEquals(acceptString, parseDocuments.parse(testString));
	}
	
	
	@Test
	public void testParseDocument() throws Exception 
	{
	    String testString = "#UID:attra0843_12_000001#DATE:201209#URL:http://www.attract.wales.nhs.uk/answer.aspx?criteria=&qid=1005&src=0#CONTENT:<!DOCTYPE html PUBLIC -//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\"><head id=\"ctl00_ctl00_Head1><title>ATTRACT | What is the evidence for";
	    String acceptString = "#UID:attra0843_12_000001#DATE:201209#URL:http://www.attract.wales.nhs.uk/answer.aspx?criteria=&qid=1005&src=0#CONTENT:ATTRACT | What is the evidence for";
	    assertEquals(acceptString, queries.parseDocument(testString));
	}
	
	@Test
	public void testIndexDocuments() throws MalformedURLException, IOException {
		parseDocuments pdInstance = new parseDocuments();
		String specialCharIndex = "#1:bm335";
		assertTrue(pdInstance.indexDocuments(specialCharIndex));
	}

}
