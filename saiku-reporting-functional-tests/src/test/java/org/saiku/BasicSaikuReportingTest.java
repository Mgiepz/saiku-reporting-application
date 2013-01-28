package org.saiku;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class BasicSaikuReportingTest {

	private WebDriver driver;

    @BeforeClass
    public void setUp()
    {
    	driver = new FirefoxDriver();
    	
    	driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(1L,TimeUnit.SECONDS);
        driver.get("http://localhost:8080/?mode=relational");
    	
    }
    
    @Test
    public void testSaikuStartup()
    {
    	driver.get("http://localhost:8080/?mode=relational");
    }

    @Test
    public void testSelectModel()
    {
    	
    }
    
    @Test
    public void testAddColumn()
    {
        WebElement draggable = driver.findElement(By.id("draggable"));
        WebElement droppable = driver.findElement(By.id("droppable"));
        new Actions(driver).dragAndDrop(draggable, droppable).build().perform();
    }
    
    @AfterClass
    public void tearDown()
    {
        driver.quit();
    }
    
    
}
