package ch.inftec.ju.util.change;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;

import ch.inftec.ju.db.DbConnection;
import ch.inftec.ju.db.DbRow;
import ch.inftec.ju.util.JuStringUtils;
import ch.inftec.ju.util.XString;
import ch.inftec.ju.util.general.Descriptor;
import ch.inftec.ju.util.general.DescriptorUtils;
import ch.inftec.ju.util.persistable.DbPersistenceStorage;
import ch.inftec.ju.util.persistable.GenericMemento;
import ch.inftec.ju.util.persistable.GenericMementoUtils;
import ch.inftec.ju.util.persistable.GenericMementoUtils.GenericMementoBuilder;

/**
 * Abstract base class for classes that want to implement the DbAction interface
 * based on a DbRow.
 * 
 * @author tgdmemae
 *
 */
abstract class AbstractDbRowDbAction implements DbAction {
	/**
	 * The connection this action works with.
	 */
	private final DbConnection dbConnection;
	
	/**
	 * The DbRow of this DbAction.
	 */
	private final DbRow row;
	
	/**
	 * Name of the row's table.
	 */
	private final String tableName;
	
	/**
	 * HashMap containing the changed values of the row as set
	 * with setValue. Keys are converted to upper case.
	 */
	private final HashMap<String, Object> changedValues = new HashMap<>();
	
	/**
	 * Creates a new DbRowDbActio with the specified base DbRow.
	 * @param dbConnection DbConnection
	 * @param row DbRow
	 * @param tableName Name of the row's table
	 */
	protected AbstractDbRowDbAction(DbConnection dbConnection, DbRow row, String tableName) {
		this.dbConnection = dbConnection;
		this.row = row;
		this.tableName = tableName.toUpperCase();
	}
	
	/**
	 * Extending classes must implement this method that will perform the
	 * DB changes.
	 */
	abstract protected void execute();
	
	@Override
	public final Descriptor getDescriptor() {
		// TODO: Make dynamic
		return DescriptorUtils.newInstance(
				String.format("%s(%s.%s)", 
						DbChangeUtils.TYPE_HANDLER.getTypeName(this),
						this.getTableName(),
						this.getPrimaryKeyValue())); 
	}

	@Override
	public final List<ChangeItem> getChildItems() {
		return Collections.emptyList();
	}

	@Override
	public final ChangeItemHandler getHandler() {
		return new ChangeItemHandler() {
			@Override
			public ChangeItem createUndoItem() {
				return AbstractDbRowDbAction.this.createUndoAction();
			}

			@Override
			public void execute() {
				AbstractDbRowDbAction.this.execute();
			}
			
			@Override
			public String toString() {
				return JuStringUtils.toString(this, "action", AbstractDbRowDbAction.this);
			}
		};
	}
	
	/**
	 * Evaluates the DbConnection from the current context.
	 * @return DbConnection instance
	 */
	protected final DbConnection getDbConnection() {
		return this.dbConnection;
	}
	
	/**
	 * Gets the DbRow of this DbAction.
	 * @return DbRow instance
	 */
	protected final DbRow getRow() {
		return this.row;
	}
	
	/**
	 * Gets the name of the row's table.
	 * @return Table name
	 */
	protected final String getTableName() {
		return this.tableName;
	}
	
	@Override
	public final DbAction setValue(String columnName, Object value) {
		// TODO: Maybe check for column name / value type compliance
		this.changedValues.put(columnName.toUpperCase(), value);
		
		return this;
	}
	
	/**
	 * Gets the Val instance for the specified column that can be used to easily get
	 * the original and the changed value for a column.
	 * @param columnName Column name
	 * @return Val instance
	 */
	public final Val getVal(String columnName) {
		return new Val(columnName);
	}
	
	/**
	 * Gets the primary key value for this action's table.
	 * @param dbConn DbConnection used to evaluate the primary key column name
	 */
	protected final Val getPrimaryKeyValue() {
		return this.getVal(this.getDbConnection().getPrimaryColumnName(this.getTableName()));
	}
	
	/**
	 * Gets Val instances for all changed columns, i.e. columns that have non equal
	 * original and changed values.
	 * @return Array of Val instances that have changed values
	 */
	protected final Val[] getChangedColumns() {
		ArrayList<Val> changedVals = new ArrayList<Val>();
		
		for (int i = 0; i < this.getRow().getColumnCount(); i++) {
			Val val = this.getVal(this.getRow().getColumnName(i));
			if (val.hasChanged()) changedVals.add(val);
		}
		
		return (Val[])changedVals.toArray(new Val[0]);
	}
	
	@Override
	public final GenericMemento createMemento() {
		GenericMementoBuilder builder = GenericMementoUtils.builder();
		
		builder
			.add(DbPersistenceStorage.ATTR_CONNECTION_NAME, this.dbConnection.getName())
			.add(".table", this.getTableName())
			.add(".id", this.getPrimaryKeyValue().getValuePrioOriginal());
		
		for (Val val : this.getChangedColumns()) {
			builder.add(val.getColumnName() + ".orig", val.getOriginalValue());
			builder.add(val.getColumnName() + ".new", val.getChangedValue());
		}
		
		return builder.build();
	}
	
	@Override
	public final void setMemento(GenericMemento memento) {
		throw new UnsupportedOperationException("Not implemented yet"); 
	}
	
	@Override
	public final String toString() {
		return JuStringUtils.toString(this, "tableName", this.tableName, "primaryKeyValue", this.getPrimaryKeyValue(), "dbConnection", this.dbConnection);
	}
	
	/**
	 * Helper class for a value, containing the column name, original and changed value.
	 * @author Martin
	 *
	 */
	class Val implements DbActionValue {
		private String columnName;
		
		/**
		 * Creates a new Value instance for the specified column.
		 * @param columnName Column name
		 */
		private Val(String columnName) {
			this.columnName = columnName;
		}
		
		@Override
		public String getColumnName() {
			return this.columnName;
		}
		
		@Override
		public Object getOriginalValue() {
			return AbstractDbRowDbAction.this.getRow().getValue(this.getColumnName());
		}
		
		@Override
		public Object getChangedValue() {
			return AbstractDbRowDbAction.this.changedValues.get(this.getColumnName());
		}
		
		@Override
		public Object getValue() {
			// We have to be careful with null values, therefore we need to check whether the
			// HashMap contains the key rather than just get the value (which might be null because
			// it was set to null or because it wasn't set at all).
			if (AbstractDbRowDbAction.this.changedValues.containsKey(this.getColumnName())) {
				return this.getChangedValue();
			} else {
				return this.getOriginalValue();
			}
		}
				
		/**
		 * Gets the original value (if set), otherwise returns the changed value.
		 * @return Original value or changed value of the original value is null
		 */
		private Object getValuePrioOriginal() {
			if (AbstractDbRowDbAction.this.getRow().getValue(this.getColumnName()) != null) {
				return this.getOriginalValue();
			} else {
				return this.getChangedValue();
			}
		}
		
		@Override
		public boolean hasChanged() {
			return AbstractDbRowDbAction.this.changedValues.containsKey(this.getColumnName())
				&& ObjectUtils.notEqual(this.getOriginalValue(), this.getChangedValue());
		}
		
		@Override
		public String toString() {
			XString val = new XString();
			val.addFormatted("%s(%s", this.getColumnName(), this.getOriginalValue());
			if (this.hasChanged()) val.addText(">", this.getChangedValue());
			val.addText(")");
			
			return val.toString();
		}
	}
}