package ch.inftec.ju.util.persistable;


public class EclipseLinkTest {
	// TODO: Refactor or remove
//	Logger log = LoggerFactory.getLogger(EclipseLinkTest.class);
//	
//	private static DbConnectionFactory dcf = DbConnectionFactoryLoader.createInstance();
//	private static EntityManagerFactory emf;
//	
//	@BeforeClass
//	public static void initDb() { 
//		emf = Persistence.createEntityManagerFactory("PU_mementoObject");
//		EntityManager em = emf.createEntityManager();
//		em.close();
//	}
//	
//	@AfterClass
//	public static void tearDown() {
//		if (EclipseLinkTest.emf.isOpen()) {
//			EclipseLinkTest.emf.close();
//		}
//	}
//	
//	@Before
//	public void cleanupDb() {
//		EntityManager em = EclipseLinkTest.emf.createEntityManager();
//		em.getTransaction().begin();
//		em.createQuery("delete from AttributeObject").executeUpdate();
//		em.createQuery("delete from MementoObject").executeUpdate();		
//		em.createQuery("delete from TestObject").executeUpdate();
//		em.getTransaction().commit();
//		em.close();
//	}
//	
//	@Test
//	public void mementoObject() throws Exception {
//		EntityManager em = EclipseLinkTest.emf.createEntityManager();
//
//		MementoObject o1 = new MementoObject();
//		o1.setDbName("Test");
//		
//		AttributeObject a1 = new AttributeObject();
//		a1.setKey("TestKey");
//		AttributeObject a2 = new AttributeObject();
//		
//		o1.addAttribute(a1);
//		o1.addAttribute(a2);
//		
//		em.getTransaction().begin();
//		
//		em.persist(o1);
//		
//		Assert.assertEquals(2, o1.getAttributes().size());
//		Assert.assertEquals("TestKey", o1.getAttributes().get(0).getKey());
//		
//		Long id = o1.getId();
//		Assert.assertNotNull(id);
//		
//		MementoObject o2 = new MementoObject();
//		o2.setId(99999L);
//		o2.setType("99");
//		em.persist(o2);
//		
//		em.getTransaction().commit();
//		
//		em.close();
//		
//		// Verify using JDBC access
//		try (DbConnection dbConn = EclipseLinkTest.dcf.openDbConnection("PU_mementoObject")) {
//			DbQueryRunner qr = dbConn.getQueryRunner();
//			
//			DbRows dbRows = qr.query("SELECT * FROM MEMENTO_OBJECT WHERE ID=?", id);
//			Assert.assertEquals(1, dbRows.getRowCount());
//			Assert.assertEquals(id, dbRows.getRow(0).getValue("ID"));
//			
////			DbRows dbRowsAttrs = qr.query("SELECT * FROM MEMENTO_ATTRIBUTE ORDER BY ID");
////			Assert.assertEquals(2, dbRowsAttrs.getRowCount());
////			Assert.assertEquals("TestKey", dbRowsAttrs.getRow(0).getValue("KEYNAME"));
//			
//			DbRows dbRows99 = qr.query("SELECT * FROM MEMENTO_OBJECT WHERE ID=?", 99999L);
//			Assert.assertEquals(1, dbRows99.getRowCount());
//			Assert.assertEquals(new Long(99999L), dbRows99.getRow(0).getValue("ID"));
//		}
//	}
//	
//	@Test
//	public void mementoObjectParent() throws Exception {
//		EntityManager em = EclipseLinkTest.emf.createEntityManager();
//
//		MementoObject o1 = new MementoObject();
//		o1.setDbName("O1");
//		
//		AttributeObject a1 = new AttributeObject();
//		a1.setKey("A1");
//		o1.addAttribute(a1);
//		
//		MementoObject o2 = new MementoObject();
//		o2.setDbName("O2");
//		o1.addChild(o2);
//		
//		em.getTransaction().begin();
//		
//		em.persist(o1);
//		
////		MementoObject o2Loaded = em.find(MementoObject.class, o2.getId());
//		
////		Assert.assertEquals("O1", o2Loaded.getParent().getDbName());
////		Assert.assertEquals("O1", o2.getParent().getDbName());
////		Assert.assertEquals("O1", a1.getParent().getDbName());
//		
//		em.getTransaction().commit();
//		
//		em.getTransaction().begin();
//		
//		AttributeObject a1Loaded = em.find(AttributeObject.class, a1.getId());
//		Assert.assertEquals("O1", a1Loaded.getParent().getDbName());
//		
//		MementoObject o1Loaded = em.find(MementoObject.class, o1.getId());
//		MementoObject o2Loaded = em.find(MementoObject.class, o2.getId());
//		
//		Assert.assertEquals("O2", o1Loaded.getChildren().get(0).getDbName());
//		
//		Assert.assertEquals("O1", o2Loaded.getParent().getDbName());
////		Assert.assertEquals("O1", o2.getParent().getDbName());
////		Assert.assertEquals("O1", a1.getParent().getDbName());
//
//		
//		em.close();
//	}
//	
//	@Test
//	public void testObject() {
//		EntityManager em = EclipseLinkTest.emf.createEntityManager();
//		em.getTransaction().begin();
//		
//		Query q = em.createNativeQuery("TRUNCATE TABLE TESTOBJECT");
//		q.executeUpdate();
//		em.getTransaction().commit();
//		
//		EntityManager em2 = EclipseLinkTest.emf.createEntityManager();		
//		em2.getTransaction().begin();
//		
//		TestObject o1 = new TestObject("o1");
//		
//		em.getTransaction().begin();
//		em.persist(o1);
//		
//		CriteriaBuilder cb = em2.getCriteriaBuilder();
//		CriteriaQuery<TestObject> cq = cb.createQuery(TestObject.class);
//		
//		TypedQuery<TestObject> tq = em2.createQuery(cq);
//		List<TestObject> res = tq.getResultList();
//		
//		Assert.assertEquals(0, res.size());
//		
//		em.getTransaction().commit();
//		
//		em.getTransaction().begin();
//		
//		res = tq.getResultList();
//		Assert.assertEquals(1, res.size());
//		
//		TestObject o1Loaded2 = res.get(0);
//		Assert.assertEquals("o1", o1Loaded2.getText());
//		Assert.assertNotNull(o1Loaded2.getId());
//		o1Loaded2.setText("o1_new");
//		
//		TestObject o1Loaded1 = em.find(TestObject.class, o1Loaded2.getId());
//		Assert.assertEquals("o1", o1Loaded1.getText());
//		
//		em2.getTransaction().commit();
//		
//		o1Loaded1 = em.find(TestObject.class, o1Loaded2.getId());
//		Assert.assertEquals("o1", o1Loaded1.getText());
//		
//		em.clear();
//		
//		o1Loaded1 = em.find(TestObject.class, o1Loaded2.getId());
//		Assert.assertEquals("o1_new", o1Loaded1.getText());
//		
//		em.remove(o1Loaded1);
//		
//		em.getTransaction().commit();
//		em.clear();
//		
//		em2.clear();
//		em2.getTransaction().begin();
//		
//		res = tq.getResultList();
//		Assert.assertEquals(0, res.size());
//		
//		em2.getTransaction().commit();
//
//		em.getTransaction().begin();
//		
//		em.persist(o1);
//		Long idp1 = o1.getId();
//		
//		em.getTransaction().commit();
//		em.clear();
//		
//		o1.setText("o1_old");
//		em.getTransaction().begin();
//		
//		// Would result in an insert statement (and throw a duplicate key exception if the ID is set and already exists...)
//		//em.persist(o1);
//		em.merge(o1);
//		
//		em.getTransaction().commit();
//		
//		em.getTransaction().begin();
//		em.clear();
//		
//		o1.setId(null);		
//		// Will create an insert if ID is null 
//		em.merge(o1);
//		Assert.assertFalse(idp1.equals(o1.getId()));
//		
//		em.getTransaction().commit();
//		
//		// Test refresh
//		
//		em.getTransaction().begin();
//		em.clear();
//		
//		em2.getTransaction().begin();
//		em2.clear();
//		
//		o1 = new TestObject("o1");
//		em.persist(o1);
//		em.getTransaction().commit();
//		
//		em.getTransaction().begin();
//		o1.setText("o1_c1");
//		em.refresh(o1);
//		Assert.assertEquals("o1", o1.getText());
//		
//		o1Loaded2 = em2.find(TestObject.class, o1.getId());
//		o1Loaded2.setText("o1_c2");
//		em2.getTransaction().commit();
//		
//		em.refresh(o1);
//		Assert.assertEquals("o1_c2", o1.getText());
//		
//		em.close();
//		em2.close();
//	}
//	
//	@Test
//	public void performanceTest() throws Exception {
//		int insCount = 10;
//		
//		// Create connections and insert records
//		
//		Timer t1 = new Timer();
//		for (int i = 0; i < insCount; i++) {
//			EntityManager em = emf.createEntityManager();
//
//			MementoObject o1 = new MementoObject();
//			o1.setDbName("Test");
//		
//			em.getTransaction().begin();
//			em.persist(o1);
//			
//			em.getTransaction().commit();
//		
//			em.close();
//		}
//		log.info(insCount + " inserts using EntityManager:" + t1);
//		
//		Timer t2 = new Timer();		
//		for (int i = 0; i < insCount; i++) {
//			EntityManager em = emf.createEntityManager();
//
//			em.getTransaction().begin();
//			java.sql.Connection conn = em.unwrap(java.sql.Connection.class);
//			
//			QueryRunner qr = new QueryRunner();			
//			
//			qr.update(conn, "INSERT INTO MEMENTO_OBJECT (ID, DBNAME) VALUES (?,?)", i+10000, "test");
//			
//			em.getTransaction().commit();
//		
//			em.close();
//		}
//		log.info("1000 inserts using EntityManager's Connection:" + t2);
//
//		Timer t3 = new Timer();		
//		for (int i = 0; i < insCount; i++) {
//			EntityManager em = emf.createEntityManager();
//
//			em.getTransaction().begin();
//			java.sql.Connection conn = em.unwrap(java.sql.Connection.class);
//			
//			QueryRunner qr = new QueryRunner();			
//			
//			qr.query(conn, "SELECT COUNT(*) FROM MEMENTO_OBJECT", new ScalarHandler());
//			
//			em.getTransaction().commit();
//		
//			em.close();
//		}
//		log.info("1000 selects using EntityManager's Connection:" + t3);
//		
////		// Verify using JDBC access
////		DbConnection dbConn = .getDbConnection("Derby juPersTestDb");
////		
////		try (Connection conn = dbConn.getConnection()) {
////			DbQueryRunner qr = dbConn.getQueryRunner(conn);
//	}
}
