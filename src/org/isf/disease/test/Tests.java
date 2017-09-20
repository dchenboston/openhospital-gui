package org.isf.disease.test;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.isf.utils.db.DbJpaUtil;
import org.isf.utils.exception.OHException;
import org.isf.disease.model.Disease;
import org.isf.disease.service.DiseaseIoOperations;
import org.isf.distype.model.DiseaseType;
import org.isf.distype.test.TestDiseaseType;
import org.isf.distype.test.TestDiseaseTypeContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringApplicationConfiguration(org.isf.utils.db.SpringDataConfiguration.class)
public class Tests  
{
	private static DbJpaUtil jpa;
	private static TestDisease testDisease;
	private static TestDiseaseType testDiseaseType;
	private static TestDiseaseContext testDiseaseContext;
	private static TestDiseaseTypeContext testDiseaseTypeContext;

    @Autowired
    DiseaseIoOperations diseaseIoOperation;
    
	
	@BeforeClass
    public static void setUpClass()  
    {
    	jpa = new DbJpaUtil();
    	testDisease = new TestDisease();
    	testDiseaseType = new TestDiseaseType();
    	testDiseaseContext = new TestDiseaseContext();
    	testDiseaseTypeContext = new TestDiseaseTypeContext();
    	
        return;
    }

    @Before
    public void setUp() throws OHException
    {
        jpa.open();
        
        _saveContext();
		
		return;
    }
        
    @After
    public void tearDown() throws Exception 
    {
        _restoreContext();   
        
        jpa.flush();
        jpa.close();
                
        return;
    }
    
    @AfterClass
    public static void tearDownClass() throws OHException 
    {
    	testDisease = null;
    	testDiseaseType = null;
    	testDiseaseContext = null;
    	testDiseaseTypeContext = null;

    	return;
    }
	
		
	@Test
	public void testDiseaseGets() 
	{
		String code = "";
			

		try 
		{		
			code = _setupTestDisease(false);
			_checkDiseaseIntoDb(code);
		} 
		catch (Exception e) 
		{
			System.out.println("==> Test Exception: " + e);		
			assertEquals(true, false);
		}
				
		return;
	}
	
	@Test
	public void testDiseaseSets() 
	{
		String code = "";
			

		try 
		{		
			code = _setupTestDisease(true);
			_checkDiseaseIntoDb(code);
		} 
		catch (Exception e) 
		{
			System.out.println("==> Test Exception: " + e);		
			assertEquals(true, false);
		}
		
		return;
	}
	
	@Test
	public void testIoGetDiseaseByCode()  
	{
		String code = "";
		
		
		try 
		{		
			code = _setupTestDisease(false);
			Disease foundDisease = diseaseIoOperation.getDiseaseByCode(Integer.parseInt(code));
			
			testDisease.check(foundDisease);
		} 
		catch (Exception e) 
		{
			System.out.println("==> Test Exception: " + e);		
			assertEquals(true, false);
		}
		
		return;
	}
	
	@Test
	public void testIoGetDiseases() 
	{
		String code = "";
		
		
		try 
		{		
			code = _setupTestDisease(false);
			Disease foundDisease = (Disease)jpa.find(Disease.class, code); 
			ArrayList<Disease> diseases = diseaseIoOperation.getDiseases(foundDisease.getType().getCode(), false, false, false);
			
			assertEquals(foundDisease.getDescription(), diseases.get(0).getDescription());
		} 
		catch (Exception e) 
		{
			System.out.println("==> Test Exception: " + e);		
			assertEquals(true, false);
		}
		
		return;
	}
	
	@Test
	public void testIoNewDisease() 
	{
		boolean result = false; 
		
		
		try 
		{		
			DiseaseType diseaseType = testDiseaseType.setup(false);

			
			jpa.beginTransaction();	
			Disease disease = testDisease.setup(diseaseType, true);
			jpa.persist(diseaseType);
			jpa.commitTransaction();
			result = diseaseIoOperation.newDisease(disease);
			
			assertEquals(true, result);
			_checkDiseaseIntoDb(disease.getCode());
		} 
		catch (Exception e) 
		{
			System.out.println("==> Test Exception: " + e);		
			assertEquals(true, false);
		}
		
		return;
	}
	
	@Test
	public void testIoUpdateDisease()
	{
		String code = "";
		boolean result = false;
		
		
		try 
		{		
			code = _setupTestDisease(false);
			Disease foundDisease = (Disease)jpa.find(Disease.class, code); 
			int lock = foundDisease.getLock().intValue();
			foundDisease.setDescription("Update");
			result = diseaseIoOperation.updateDisease(foundDisease);
			Disease updateDisease = (Disease)jpa.find(Disease.class, code); 
			
			assertEquals(true, result);
			assertEquals("Update", updateDisease.getDescription());
			assertEquals(lock + 1, updateDisease.getLock().intValue());
		} 
		catch (Exception e) 
		{
			System.out.println("==> Test Exception: " + e);		
			assertEquals(true, false);
		}
		
		return;
	}
	
	@Test
	public void testIoHasDiseaseModified() 
	{
		String code = "";
		boolean result = false;
		
		
		try 
		{		
			code = _setupTestDisease(false);
			Disease foundDisease = (Disease)jpa.find(Disease.class, code);
			result = diseaseIoOperation.hasDiseaseModified(foundDisease);
			
			assertEquals(false, result);
		} 
		catch (Exception e) 
		{
			System.out.println("==> Test Exception: " + e);		
			assertEquals(true, false);
		}
		
		return;
	}
	
	@Test
	public void testIoDeleteDisease() 
	{
		String code = "";
		boolean result = false;
		
		
		try 
		{		
			code = _setupTestDisease(false);
			Disease foundDisease = (Disease)jpa.find(Disease.class, code); 
			result = diseaseIoOperation.deleteDisease(foundDisease);
			
			assertEquals(true, result);
			assertEquals(false, foundDisease.getIpdInInclude());
			assertEquals(false, foundDisease.getIpdOutInclude());
			assertEquals(false, foundDisease.getOpdInclude());
		} 
		catch (Exception e) 
		{
			System.out.println("==> Test Exception: " + e);		
			assertEquals(true, false);
		}
		
		return;
	}

	@Test
	public void testIoIsCodePresent() 
	{
		String code = "";
		boolean result = false;
		

		try 
		{		
			code = _setupTestDisease(false);
			result = diseaseIoOperation.isCodePresent(code);
			
			assertEquals(true, result);
		} 
		catch (Exception e) 
		{
			System.out.println("==> Test Exception: " + e);		
			assertEquals(true, false);
		}
		
		return;
	}

	@Test
	public void testIoIsDescriptionPresent() 
	{
		String code = "";
		boolean result = false;
		

		try 
		{		
			code = _setupTestDisease(false);
			Disease foundDisease = (Disease)jpa.find(Disease.class, code); 
			result = diseaseIoOperation.isDescriptionPresent(foundDisease.getDescription(), foundDisease.getType().getCode());
			
			assertEquals(true, result);
		} 
		catch (Exception e) 
		{
			System.out.println("==> Test Exception: " + e);		
			assertEquals(true, false);
		}
		
		return;
	}
	
	
	private void _saveContext() throws OHException 
    {	
		testDiseaseContext.saveAll(jpa);
		testDiseaseTypeContext.saveAll(jpa);
		testDiseaseContext.addMissingKey(jpa);
        		
        return;
    }
	
    private void _restoreContext() throws OHException 
    {
		System.out.println(testDiseaseContext.getAllSaved());
		System.out.println(testDiseaseTypeContext.getAllSaved());
		testDiseaseContext.deleteNews(jpa);
		testDiseaseTypeContext.deleteNews(jpa);
        
        return;
    }
        
	private String _setupTestDisease(
			boolean usingSet) throws OHException 
	{
		Disease disease;
		DiseaseType diseaseType = testDiseaseType.setup(false);
		

    	jpa.beginTransaction();	
    	disease = testDisease.setup(diseaseType, usingSet);
    	jpa.persist(diseaseType);
		jpa.persist(disease);
    	jpa.commitTransaction();
    	
		return disease.getCode();
	}
		
	private void  _checkDiseaseIntoDb(
			String code) throws OHException 
	{
		Disease foundDisease;
		

		foundDisease = (Disease)jpa.find(Disease.class, code); 
		testDisease.check(foundDisease);
		
		return;
	}	
}