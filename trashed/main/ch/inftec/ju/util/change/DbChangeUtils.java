package ch.inftec.ju.util.change;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.inftec.ju.db.DbConnection;
import ch.inftec.ju.util.JuBeanUtils;
import ch.inftec.ju.util.JuStringUtils;
import ch.inftec.ju.util.change.DbActionUtils.AbstractActionBuilder;
import ch.inftec.ju.util.general.Descriptor;
import ch.inftec.ju.util.general.DescriptorUtils;
import ch.inftec.ju.util.persistable.DbPersistenceStorage;
import ch.inftec.ju.util.persistable.GenericMemento;
import ch.inftec.ju.util.persistable.GenericMementoUtils;
import ch.inftec.ju.util.persistable.GenericMementoUtils.GenericMementoBuilder;
import ch.inftec.ju.util.persistable.TypeHandler;

public class DbChangeUtils {	
	private static final String UNDO_SUFFIX = ".undo";
	
	/**
	 * TypeHandler used to get type name for DbActions when creating
	 * memento.
	 */
	static final TypeHandler TYPE_HANDLER;
	static {
		TYPE_HANDLER = GenericMementoUtils.newTypeHandler()
				.addMapping(DeleteDbRowDbAction.class)
				.addMapping(InsertDbRowDbAction.class)
				.addMapping(UpdateDbRowDbAction.class)
				.getHandler();
	}
	
	/**
	 * Creates a new builder to build DbChangeSets.
	 * <p>
	 * Name and description of the set must be set.
	 * <p>
	 * DbActions are added to groups. You must start a group using
	 * newGroup in order to be able to add actions. endGroup ends
	 * the current group.
	 * @param dbConn DbConnection instance to be used by the change set
	 * @return DbChangeSetBuilder instance
	 */
	public static DbChangeSetBuilder buildChangeSet(DbConnection dbConn) {
		return new DbChangeSetBuilder(dbConn);
	}
	
	public static class DbChangeSetBuilder {
		private final DbConnection dbConn;
		
		private String name;
		private String description;
		
		private List<DbChangeGroupBuilder> groupBuilders = new ArrayList<>();
		
		private DbChangeSetBuilder(DbConnection dbConn) {
			this.dbConn = dbConn;
		}
		
		/**
		 * Sets the name of the change set.
		 * @return This builder to allow for chaining
		 */
		public DbChangeSetBuilder name(String name) {
			this.name = name;			
			return this;
		}
		
		/**
		 * Sets the description of the change set
		 * @return This builder to allow for chaining
		 */
		public DbChangeSetBuilder description(String description) {
			this.description = description;
			return this;
		}
		
		/**
		 * Returns a GroupBuilder for a new change group.
		 * <p>
		 * Use endGroup to finish the group and return to the change set builder
		 * @param name Name of the group
		 * @param description Description of the group
		 * @return Group builder to add actions to the group
		 */
		public DbChangeGroupBuilder newGroup(String name, String description) {
			DbChangeGroupBuilder groupBuilder =  new DbChangeGroupBuilder(this, name, description);
			this.groupBuilders.add(groupBuilder);
			return groupBuilder;
		}
		
		/**
		 * Builds the change set.
		 * @return DbChangeSet instance
		 * @throws IllegalStateException If not all mandatory fields have been set
		 */
		public PersistableChangeItem build() {
			JuBeanUtils.checkFieldsNotNull(this, "name", "description");
			
			DbChangeSet changeSet = new DbChangeSet(this.dbConn, this.name, this.description);
			for (DbChangeGroupBuilder groupBuilder : this.groupBuilders) {
				changeSet.groups.add(groupBuilder.group);
			}
			
			return changeSet;
		}
	}
	
	/**
	 * Builder to add actions to a group.
	 * @author tgdmemae
	 *
	 */
	public static class DbChangeGroupBuilder {
		private final DbChangeSetBuilder parentBuilder;
		private final DbChangeGroup group;
		
		private DbChangeGroupBuilder(DbChangeSetBuilder parentBuilder, String name, String description) {
			this.parentBuilder = parentBuilder;
			this.group = new DbChangeGroup(name, description);
		}
		
		/**
		 * Adds the specified DbAction to the group.
		 * @param action DbAction
		 * @return This builder to allow for chaining.
		 */
		public DbChangeGroupBuilder addAction(DbAction action) {
			this.group.actions.add(action);
			return this;
		}
		
		/**
		 * Returns a builder to configure a new update action to be added to the group.
		 * @param tableName TableName of the row to be updated
		 * @param primaryKeyValue Primary key value of the row to be updated
		 * @return Builder to define the updates to be performed
		 */
		public DbChangeGroupActionBuilder newUpdateAction(String tableName, Object primaryKeyValue) {
			AbstractActionBuilder actionBuilder = DbActionUtils.newUpdateAction(this.parentBuilder.dbConn, tableName, primaryKeyValue);
			return new DbChangeGroupActionBuilder(this, actionBuilder);
		}
		
		/**
		 * Returns a builder to configure a new insert action to be added to the group.
		 * @param tableName TableName of the row to be inserted
		 * @return Builder to define the values of the new row
		 */
		public DbChangeGroupActionBuilder newInsertAction(String tableName) {
			AbstractActionBuilder actionBuilder = DbActionUtils.newInsertAction(this.parentBuilder.dbConn, tableName);
			return new DbChangeGroupActionBuilder(this, actionBuilder);
		}
		
		/**
		 * Returns a builder to configure a new insert action to be added to the group.
		 * @param tableName TableName of the row to be inserted
		 * @param primaryKeyValue Primary Key value of the row to be deleted
		 * @return This builder to allow for chaining
		 */
		public DbChangeGroupBuilder newDeleteAction(String tableName, Object primaryKeyValue) {
			this.addAction(DbActionUtils.newDeleteAction(this.parentBuilder.dbConn, tableName, primaryKeyValue));
			return this;
		}
		
		/**
		 * Ends this group and returns the change set builder.
		 * @return DbChangeSetBuilder to continue building
		 */
		public DbChangeSetBuilder endGroup() {
			return this.parentBuilder;
		}
	}
	
	/**
	 * Builder to configure actions to be added to a change group.
	 * @author tgdmemae
	 *
	 */
	public static class DbChangeGroupActionBuilder {
		private final DbChangeGroupBuilder groupBuilder;
		private final AbstractActionBuilder actionBuilder;
		
		private DbChangeGroupActionBuilder(DbChangeGroupBuilder groupBuilder, AbstractActionBuilder actionBuilder) {
			this.groupBuilder = groupBuilder;
			this.actionBuilder = actionBuilder;
		}
		
		/**
		 * Sets the value of the specified column for the action.
		 * @param columnName Column name
		 * @param value New value
		 * @return This builder to allow for chaining
		 */
		public DbChangeGroupActionBuilder setValue(String columnName, Object value) {
			this.actionBuilder.setValue(columnName, value);
			return this;
		}
		
		/**
		 * Ends the action and adds it the the group.
		 * @return Group builder to continue building the group
		 */
		public DbChangeGroupBuilder endAction() {
			this.groupBuilder.addAction(this.actionBuilder.getAction());
			return this.groupBuilder;
		}
	}
	
	private static class DbChangeSet implements PersistableChangeItem {
		private final DbConnection dbConn;
		private final Descriptor descriptor;
		
		private List<DbChangeGroup> groups = new ArrayList<DbChangeGroup>();
		
		private DbChangeSet(DbConnection dbConn, String name, String description) {
			this.dbConn = dbConn;
			this.descriptor = DescriptorUtils.newInstance(name, description);
		}
		
		@Override
		public Descriptor getDescriptor() {
			return this.descriptor;
		}

		@Override
		public List<ChangeItem> getChildItems() {
			return Collections.unmodifiableList(new ArrayList<ChangeItem>(this.groups));
		}

		@Override
		public ChangeItemHandler getHandler() {
			return new DbChangeSetHandler(this);
		}		
		
		private static final class DbChangeSetHandler implements ChangeItemHandler {
			private final DbChangeSet set;
			
			private DbChangeSetHandler(DbChangeSet set) {
				this.set = set;
			}
			
			@Override
			public ChangeItem createUndoItem() {
				DbChangeSetBuilder setBuilder = DbChangeUtils.buildChangeSet(set.dbConn)
					.name(set.getDescriptor().getName() + DbChangeUtils.UNDO_SUFFIX)
					.description(set.getDescriptor().getDescription() + DbChangeUtils.UNDO_SUFFIX);
				
				for (int i = set.groups.size() - 1; i >= 0; i--) {
					DbChangeGroup group = set.groups.get(i);
					DbChangeGroupBuilder groupBuilder = setBuilder.newGroup(group.getDescriptor().getName(), group.getDescriptor().getDescription());
					
					for (int j = group.actions.size() - 1; j >= 0; j--) {
						DbAction action = group.actions.get(j);
						groupBuilder.addAction(action.createUndoAction());
					}
					
					groupBuilder.endGroup();
				}
				
				return setBuilder.build();
			}

			@Override
			public void execute() {
				for (DbChangeGroup group : set.groups) {
					for (DbAction action : group.actions) {
						action.getHandler().execute();
					}
				}
			}
		}
		
		@Override
		public String toString() {
			return JuStringUtils.toString(this, "descriptor", this.getDescriptor(), "groupCount", this.groups.size());
		}

		@Override
		public GenericMemento createMemento() {
			GenericMementoBuilder setBuilder = GenericMementoUtils.builder()
				.add(DbPersistenceStorage.ATTR_CONNECTION_NAME, this.dbConn.getName());
				
			for (DbChangeGroup group : this.groups) {
				GenericMementoBuilder groupBuilder = setBuilder.newChild()
					.add("groupName", group.getDescriptor().getName())
					.add("groupDescription", group.getDescriptor().getDescription());
					
				for (DbAction action : group.actions) {
					groupBuilder.newChild()
						.add("@type", DbChangeUtils.TYPE_HANDLER.getTypeName(action))
						.add(action.createMemento())
					.childDone();					
				}
				
				groupBuilder.childDone();
			}
			
			return setBuilder.build();
		}

		@Override
		public void setMemento(GenericMemento memento) {
			throw new UnsupportedOperationException("Not yet implemented");
		}
	}
	
	private static final class DbChangeGroup implements ChangeItem {
		private Descriptor descriptor;
		
		private List<DbAction> actions = new ArrayList<>();
		
		private DbChangeGroup(String name, String description) {
			this.descriptor = DescriptorUtils.newInstance(name, description);
		}
		
		@Override
		public Descriptor getDescriptor() {
			return this.descriptor;
		}

		@Override
		public List<ChangeItem> getChildItems() {
			return Collections.unmodifiableList(new ArrayList<ChangeItem>(this.actions));
		}

		@Override
		public ChangeItemHandler getHandler() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String toString() {
			return JuStringUtils.toString(this, "descriptor", this.getDescriptor(), "actionCount", this.actions.size());
		}
	}
}
