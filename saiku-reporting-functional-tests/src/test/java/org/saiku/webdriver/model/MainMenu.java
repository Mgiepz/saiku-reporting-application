package org.saiku.webdriver.model;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.CacheLookup;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;

public class MainMenu {

	  @FindBy(how=How.ID, using="filemenu")  
	  @CacheLookup
	  private WebElement fileMenu;
	  @FindBy(how=How.ID, using="viewmenu")
	  @CacheLookup
	  private WebElement viewMenu;

}
