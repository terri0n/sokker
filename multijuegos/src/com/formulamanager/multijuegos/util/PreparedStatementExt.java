package com.formulamanager.multijuegos.util;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PreparedStatementExt implements PreparedStatement {
    private PreparedStatement preparedStatement;
    private List<Object> parameterValues;
    private String sql;

    public PreparedStatementExt(PreparedStatement preparedStatement, String sql) {
        this.preparedStatement = preparedStatement;
        this.sql = sql;
        parameterValues = new ArrayList<>();
    }
    
    private void log() {
    	System.out.println(sql + " " + parameterValues);
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
    	log();
    	return preparedStatement.executeQuery();
    }

    @Override
    public int executeUpdate() throws SQLException {
        return preparedStatement.executeUpdate();
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        parameterValues.add(null);
        preparedStatement.setNull(parameterIndex, sqlType);
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        parameterValues.add(x);
        preparedStatement.setBoolean(parameterIndex, x);
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        parameterValues.add(x);
        preparedStatement.setByte(parameterIndex, x);
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        parameterValues.add(x);
        preparedStatement.setShort(parameterIndex, x);
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        parameterValues.add(x);
        preparedStatement.setInt(parameterIndex, x);
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        parameterValues.add(x);
        preparedStatement.setLong(parameterIndex, x);
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        parameterValues.add(x);
        preparedStatement.setFloat(parameterIndex, x);
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        parameterValues.add(x);
        preparedStatement.setDouble(parameterIndex, x);
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        parameterValues.add(x);
        preparedStatement.setBigDecimal(parameterIndex, x);
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        parameterValues.add(x);
        preparedStatement.setString(parameterIndex, x);
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        parameterValues.add(x);
        preparedStatement.setBytes(parameterIndex, x);
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        parameterValues.add(x);
        preparedStatement.setDate(parameterIndex, x);
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        parameterValues.add(x);
        preparedStatement.setTime(parameterIndex, x);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        parameterValues.add(x);
        preparedStatement.setTimestamp(parameterIndex, x);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        parameterValues.add(x);
        preparedStatement.setAsciiStream(parameterIndex, x, length);
    }

    @Override
    @Deprecated
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        parameterValues.add(x);
        preparedStatement.setUnicodeStream(parameterIndex, x, length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        parameterValues.add(x);
        preparedStatement.setBinaryStream(parameterIndex, x, length);
    }

    @Override
    public void clearParameters() throws SQLException {
        parameterValues.clear();
        preparedStatement.clearParameters();
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        parameterValues.add(x);
        preparedStatement.setObject(parameterIndex, x, targetSqlType);
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        parameterValues.add(x);
        preparedStatement.setObject(parameterIndex, x);
    }

    @Override
    public boolean execute() throws SQLException {
        return preparedStatement.execute();
    }

    @Override
    public void addBatch() throws SQLException {
        preparedStatement.addBatch();
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        parameterValues.add(reader);
        preparedStatement.setCharacterStream(parameterIndex, reader, length);
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        parameterValues.add(x);
        preparedStatement.setRef(parameterIndex, x);
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        parameterValues.add(x);
        preparedStatement.setBlob(parameterIndex, x);
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        parameterValues.add(x);
        preparedStatement.setClob(parameterIndex, x);
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        parameterValues.add(x);
        preparedStatement.setArray(parameterIndex, x);
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return preparedStatement.getMetaData();
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        parameterValues.add(x);
        preparedStatement.setDate(parameterIndex, x, cal);
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        parameterValues.add(x);
        preparedStatement.setTime(parameterIndex, x, cal);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        parameterValues.add(x);
        preparedStatement.setTimestamp(parameterIndex, x, cal);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        parameterValues.add(null);
        preparedStatement.setNull(parameterIndex, sqlType, typeName);
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        parameterValues.add(x);
        preparedStatement.setURL(parameterIndex, x);
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return preparedStatement.getParameterMetaData();
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        parameterValues.add(x);
        preparedStatement.setRowId(parameterIndex, x);
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        parameterValues.add(value);
        preparedStatement.setNString(parameterIndex, value);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        parameterValues.add(value);
        preparedStatement.setNCharacterStream(parameterIndex, value, length);
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        parameterValues.add(value);
        preparedStatement.setNClob(parameterIndex, value);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        parameterValues.add(reader);
        preparedStatement.setClob(parameterIndex, reader, length);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        parameterValues.add(inputStream);
        preparedStatement.setBlob(parameterIndex, inputStream, length);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        parameterValues.add(reader);
        preparedStatement.setNClob(parameterIndex, reader, length);
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        parameterValues.add(xmlObject);
        preparedStatement.setSQLXML(parameterIndex, xmlObject);
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        parameterValues.add(x);
        preparedStatement.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        parameterValues.add(x);
        preparedStatement.setAsciiStream(parameterIndex, x, length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        parameterValues.add(x);
        preparedStatement.setBinaryStream(parameterIndex, x, length);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        parameterValues.add(reader);
        preparedStatement.setCharacterStream(parameterIndex, reader, length);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        parameterValues.add(x);
        preparedStatement.setAsciiStream(parameterIndex, x);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        parameterValues.add(x);
        preparedStatement.setBinaryStream(parameterIndex, x);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        parameterValues.add(reader);
        preparedStatement.setCharacterStream(parameterIndex, reader);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        parameterValues.add(value);
        preparedStatement.setNCharacterStream(parameterIndex, value);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        parameterValues.add(reader);
        preparedStatement.setClob(parameterIndex, reader);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        parameterValues.add(inputStream);
        preparedStatement.setBlob(parameterIndex, inputStream);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        parameterValues.add(reader);
        preparedStatement.setNClob(parameterIndex, reader);
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        return preparedStatement.executeQuery(sql);
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        return preparedStatement.executeUpdate(sql);
    }

    @Override
    public void close() throws SQLException {
        preparedStatement.close();
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return preparedStatement.getMaxFieldSize();
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        preparedStatement.setMaxFieldSize(max);
    }

    @Override
    public int getMaxRows() throws SQLException {
        return preparedStatement.getMaxRows();
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        preparedStatement.setMaxRows(max);
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        preparedStatement.setEscapeProcessing(enable);
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return preparedStatement.getQueryTimeout();
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        preparedStatement.setQueryTimeout(seconds);
    }

    @Override
    public void cancel() throws SQLException {
        preparedStatement.cancel();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return preparedStatement.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        preparedStatement.clearWarnings();
    }

    @Override
    public void setCursorName(String name) throws SQLException {
        preparedStatement.setCursorName(name);
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        return preparedStatement.execute(sql);
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return preparedStatement.getResultSet();
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return preparedStatement.getUpdateCount();
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return preparedStatement.getMoreResults();
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        preparedStatement.setFetchDirection(direction);
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return preparedStatement.getFetchDirection();
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        preparedStatement.setFetchSize(rows);
    }

    @Override
    public int getFetchSize() throws SQLException {
        return preparedStatement.getFetchSize();
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return preparedStatement.getResultSetConcurrency();
    }

    @Override
    public int getResultSetType() throws SQLException {
        return preparedStatement.getResultSetType();
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        preparedStatement.addBatch(sql);
    }

    @Override
    public void clearBatch() throws SQLException {
        preparedStatement.clearBatch();
    }

    @Override
    public int[] executeBatch() throws SQLException {
        return preparedStatement.executeBatch();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return preparedStatement.getConnection();
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return preparedStatement.getMoreResults(current);
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return preparedStatement.getGeneratedKeys();
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return preparedStatement.executeUpdate(sql, autoGeneratedKeys);
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return preparedStatement.executeUpdate(sql, columnIndexes);
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return preparedStatement.executeUpdate(sql, columnNames);
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return preparedStatement.execute(sql, autoGeneratedKeys);
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        return preparedStatement.execute(sql, columnIndexes);
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        return preparedStatement.execute(sql, columnNames);
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return preparedStatement.getResultSetHoldability();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return preparedStatement.isClosed();
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        preparedStatement.setPoolable(poolable);
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return preparedStatement.isPoolable();
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        preparedStatement.closeOnCompletion();
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return preparedStatement.isCloseOnCompletion();
    }

    @Override
    public long getLargeUpdateCount() throws SQLException {
        return preparedStatement.getLargeUpdateCount();
    }

    @Override
    public void setLargeMaxRows(long max) throws SQLException {
        preparedStatement.setLargeMaxRows(max);
    }

    @Override
    public long getLargeMaxRows() throws SQLException {
        return preparedStatement.getLargeMaxRows();
    }

    @Override
    public long[] executeLargeBatch() throws SQLException {
        return preparedStatement.executeLargeBatch();
    }

    @Override
    public long executeLargeUpdate(String sql) throws SQLException {
        return preparedStatement.executeLargeUpdate(sql);
    }

    @Override
    public long executeLargeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return preparedStatement.executeLargeUpdate(sql, autoGeneratedKeys);
    }

    @Override
    public long executeLargeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return preparedStatement.executeLargeUpdate(sql, columnIndexes);
    }

    @Override
    public long executeLargeUpdate(String sql, String[] columnNames) throws SQLException {
        return preparedStatement.executeLargeUpdate(sql, columnNames);
    }

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return preparedStatement.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return preparedStatement.isWrapperFor(iface);
	}
}
