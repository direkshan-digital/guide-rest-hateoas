package it.io.openliberty.guides.hateoas;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.json.JsonArray;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.provider.jsrjsonp.JsrJsonpProvider;

public class EndpointTest {
  private String port;
  private String baseUrl;

  private Client client;

  private final String SYSTEM_PROPERTIES = "system/properties";
  private final String INVENTORY_HOSTS = "inventory/hosts";

  @Before
  public void setup() {
    port = System.getProperty("liberty.test.port");
    baseUrl = "http://localhost:" + port + "/"; //DevSkim: ignore DS137138 

    client = ClientBuilder.newClient();
    client.register(JsrJsonpProvider.class);
  }

  @After
  public void teardown() {
    client.close();
  }

  @Test
  public void testSuite() {
    this.testLinkForInventoryContents();
    this.testLinksForSystem();
  }

  /**
   * Checks if the HATEOAS link for the inventory contents (hostname=*) is as
   * expected.
   */
  public void testLinkForInventoryContents() {
    Response response = this.getResponse(baseUrl + INVENTORY_HOSTS);
    this.assertResponse(baseUrl, response);

    JsonArray sysArray = response.readEntity(JsonArray.class);

    String expected, actual;

    JsonArray links = sysArray.getJsonObject(0).getJsonArray("_links");

    expected = baseUrl + INVENTORY_HOSTS + "/*";
    actual = links.getJsonObject(0).getString("href");
    assertEquals("Incorrect href", expected, actual);

    // asserting that rel was correct
    expected = "self";
    actual = links.getJsonObject(0).getString("rel");
    assertEquals("Incorrect rel", expected, actual);

    response.close();
  }

  /**
   * Checks that the HATEOAS links, with relationships 'self' and 'properties' for
   * a simple localhost system is as expected.
   */
  public void testLinksForSystem() {
    this.visitLocalhost();

    Response response = this.getResponse(baseUrl + INVENTORY_HOSTS);
    this.assertResponse(baseUrl, response);

    JsonArray sysArray = response.readEntity(JsonArray.class);

    String expected, actual;

    JsonArray links = sysArray.getJsonObject(0).getJsonArray("_links");

    // testing the 'self' link

    expected = baseUrl + INVENTORY_HOSTS + "/localhost";
    actual = links.getJsonObject(0).getString("href");
    assertEquals("Incorrect href", expected, actual);

    expected = "self";
    actual = links.getJsonObject(0).getString("rel");
    assertEquals("Incorrect rel", expected, actual);

    // testing the 'properties' link

    expected = baseUrl + SYSTEM_PROPERTIES;
    actual = links.getJsonObject(1).getString("href");
    assertEquals("Incorrect href", expected, actual);

    expected = "properties";
    actual = links.getJsonObject(1).getString("rel");
    assertEquals("Incorrect rel", expected, actual);
  }

  /**
   * Returns a Response object for the specified URL.
   */
  private Response getResponse(String url) {
    return client.target(url).request().get();
  }

  /**
   * Asserts that the given URL has the correct (200) response code.
   */
  private void assertResponse(String url, Response response) {
    assertEquals("Incorrect response code from " + url, 200, response.getStatus());
    ;
  }

  /**
   * Makes a GET request to localhost at the Inventory service.
   */
  private void visitLocalhost() {
    Response response = this.getResponse(baseUrl + SYSTEM_PROPERTIES);
    this.assertResponse(baseUrl, response);
    response.close();

    Response targetResponse = client.target(baseUrl + INVENTORY_HOSTS + "/localhost").request().get();
    targetResponse.close();
  }
}