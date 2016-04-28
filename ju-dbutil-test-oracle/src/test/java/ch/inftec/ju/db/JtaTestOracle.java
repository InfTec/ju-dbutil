package ch.inftec.ju.db;

import ch.inftec.ju.dbutil.test.jta.JtaTest;

/**
 * Helper class to run JtaTest for Oracle DB.
 * <p>
 * Note for using Oracle XE: We might have to configure the DB test user to
 * be able to use XA transactions:
 * <ol>
 *   <li>Login as sysdba (https://inftec.atlassian.net/wiki/display/TEC/CentOS#CentOS-Connectassysdba)
 *   <li>Execute the following commands (http://stackoverflow.com/questions/16028409/spring-transactions-doesnt-work-with-oracle-express):
 *     <ul>
 *       <li>grant select on sys.dba_pending_transactions to user_test;</li>
 *       <li>grant select on sys.pending_trans$ to user_test;</li>
 *       <li>grant select on sys.dba_2pc_pending to user_test;</li>
 *       <li>grant execute on sys.dbms_system to user_test;</li>
 *     </ul>
 *   </li>
 * </ol>
 * Also checkout http://docs.codehaus.org/display/BTM/FAQ#FAQ-WhyisOraclethrowingaXAExceptionduringinitializationofmydatasource?
 *   
 * @author Martin Meyer <martin.meyer@inftec.ch>
 *
 */
public class JtaTestOracle extends JtaTest {
}
