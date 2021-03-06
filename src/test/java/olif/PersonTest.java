package olif;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xmlunit.assertj.XmlAssert;


class PersonTest {

	MappingEngine mapper;
	ModelCache modelCache = ModelCache.getInstance();

	@BeforeEach
	void setUp() throws Exception {
		this.mapper = new MappingEngine();
	}

	// Should get the source model correctly
	// @Test
	void shouldGetSource() throws URISyntaxException {
		Path path = Paths.get("src", "test", "resources", "persons", "mapping.ttl");
		Model model = this.modelCache.getModel(path);

		String queryString = "PREFIX rml: <http://semweb.mmlab.be/ns/rml#>" + "SELECT ?source WHERE {" + "?mapping rml:logicalSource ?logicalSource."
				+ "?logicalSource rml:source ?source." + "}";

		Query sourceFileQuery = QueryFactory.create(queryString);

		QueryExecution qexec = QueryExecutionFactory.create(sourceFileQuery, model);

		ResultSet results = qexec.execSelect();

		// get the first source result
		String source = (String) results.nextBinding().get("source").getLiteralValue();
		assertEquals("persons.ttl", source);
	}

	
	// @Test
	void shouldGiveTwoMappings() {
		Path path = Paths.get("src", "test", "resources", "persons", "mapping.ttl");
		Model model = this.modelCache.getModel(path);
		List<DataMap> mappings = this.mapper.getAllMappingDefinitions(model);
		assertEquals(2, mappings.size());
	}

	
	@Test
	void shouldMapPersons() throws ParserConfigurationException, SAXException, IOException, TransformerException {
		Path path = Paths.get("src", "test", "resources", "persons", "mapping.ttl").toAbsolutePath();
		Document actualDoc = this.mapper.map(path);

		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream("persons/persons.xml");

		File expectedFile = File.createTempFile("temp", null);
		try (OutputStream outputStream = new FileOutputStream(expectedFile)) {
			IOUtils.copy(is, outputStream);
		} catch (FileNotFoundException e) {
			// handle exception here
		} catch (IOException e) {
			// handle exception here
		}

		// Path expectedXmlPath = Paths.get("src","test","resources", "persons", "persons.xml");
		//File expectedFile = expectedXmlPath.toFile();
		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document expectedDoc = documentBuilder.parse(expectedFile);

		DOMSource source = new DOMSource(actualDoc);
	    FileWriter writer = new FileWriter(new File("C:/Users/K?cher/Desktop/output.xml"));
	    StreamResult result = new StreamResult(writer);

	    TransformerFactory transformerFactory = TransformerFactory.newInstance();
	    Transformer transformer = transformerFactory.newTransformer();
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
	    transformer.transform(source, result);
		
	    XmlAssert.assertThat(expectedDoc).and(actualDoc)
	    	.ignoreWhitespace()
	    	.areSimilar();
	}

}
