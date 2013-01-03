package test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GuavaTableTest extends TestCase {
	
	public void testTable() {
		
		ObjectMapper mapper = new ObjectMapper();
		
		Map<String,Map<String,Integer>> t = new HashMap<String,Map<String,Integer>>();
		
//		Map<Map<String,String>,String> t2 = new HashMap<Map<String,String>,String>();
//		
//		Pair<String,String> test = new ImmutablePair<String, String>("a", "b");
//		
//		class Key{
//			
//			public Key(String row, String col) {
//				super();
//				this.row = row;
//				this.col = col;
//			}
//			private String row;
//			private String col;
//			
//			public String getRow() {
//				return row;
//			}
//			public void setRow(String row) {
//				this.row = row;
//			}
//			public String getCol() {
//				return col;
//			}
//			public void setCol(String col) {
//				this.col = col;
//			}
//			
//		}
		
		
		HashMap<String, Integer> row = new HashMap<String, Integer>();
		row.put("col1", 3);
		row.put("col2", 2);
		row.put("col3", 0);
		
		t.put("row1", row);
		t.put("row2", row);
		
//		Table<Integer, String, String> table = HashBasedTable.create();
//		table.put(1, "a", "1a");
//		table.put(1, "b", "1b");
//		table.put(2, "a", "2a");
//		table.put(2, "b", "2b");
//		
//		Key key = new Key("row1", "col2");
		
		try {

			String value = mapper.writeValueAsString(t);
			System.out.println(value);
						
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    assertTrue(true);
	  }

}
