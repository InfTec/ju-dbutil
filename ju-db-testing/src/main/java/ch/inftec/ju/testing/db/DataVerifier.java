package ch.inftec.ju.testing.db;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import ch.inftec.ju.db.JuEmUtil;
import ch.inftec.ju.util.xml.XPathGetter;

/**
 * Base class for data verifiers, i.e. a container for code that will run
 * after a DB test case has completed to verify that the data has been
 * persisted correctly. 
 * <p>
 * Extending classes must provide a public default constructor.
 * @author Martin
 *
 */
public abstract class DataVerifier {
	private Logger logger = LoggerFactory.getLogger(DataVerifier.class);
	
	protected EntityManager em;
	protected JuEmUtil emUtil;
	
	private XPathGetter xg;
	
	/**
	 * Initializes the DataVerifier. Needs to be called from the testing
	 * framework before the verify method is invoked.
	 * @param em EntityManager instance of the current persistence context
	 * @param doc XML Document of an (optional) data export. If no data was exported, the
	 * document will be null
	 */
	public final void init(EntityManager em, Document doc) {
		this.em = em;
		this.emUtil = new JuEmUtil(em);
		
		if (doc != null) this.xg = new XPathGetter(doc);
	}
	
	/**
	 * Method that will be called by the testing framework after
	 * the data test method has completed and the transaction has been
	 * either committed or rolled back.
	 * @throws Exception If verification fails
	 */
	public abstract void verify() throws Exception;
	
	/**
	 * Gets an XPathGetter to verify the XML document.
	 * <p>
	 * We can only get an XPathGetter when an XML was exported using the @DataSetExport annotation.
	 * <p>
	 * This method returns an XPathGetter relative to /dataset, so we don't need to explicitly
	 * specify the /dataset root to query the XML.
	 * @return XPathGetter on the exported XML
	 */
	protected final XPathGetter getXg() {
		if (this.xg == null) {
			logger.warn("XML verifying is only possible when using @DataSetExport with exportType != NONE");
			return null;
		} else {
			return this.xg.getGetter("/dataset");
		}
	}
}
