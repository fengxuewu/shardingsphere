/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.executor;

import io.shardingsphere.core.event.ShardingEventType;
import io.shardingsphere.core.jdbc.core.connection.ShardingConnection;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class BatchPreparedStatementExecutorTest extends AbstractBaseExecutorTest {
    
    private static final String SQL = "DELETE FROM table_x WHERE id=?";
    
    private static final ShardingConnection CONNECTION = Mockito.mock(ShardingConnection.class);
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertNoPreparedStatement() throws SQLException {
        BatchPreparedStatementExecutor actual = new BatchPreparedStatementExecutor(1, 1, 1, false, CONNECTION);
        assertThat(actual.executeBatch(), is(new int[] {0, 0}));
    }
    
    @Test
    public void assertExecuteBatchForSinglePreparedStatementSuccess() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeBatch()).thenReturn(new int[] {10, 20});
        when(preparedStatement.getConnection()).thenReturn(mock(Connection.class));
        BatchPreparedStatementExecutor actual = new BatchPreparedStatementExecutor(1, 1, 1, false, CONNECTION);
    
        assertThat(actual.executeBatch(), is(new int[] {10, 20}));
        verify(preparedStatement).executeBatch();
        verify(getEventCaller(), times(4)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(4)).verifySQL(SQL);
        verify(getEventCaller(), times(2)).verifyParameters(Collections.<Object>singletonList(1));
        verify(getEventCaller(), times(2)).verifyParameters(Collections.<Object>singletonList(2));
        verify(getEventCaller(), times(2)).verifyEventExecutionType(ShardingEventType.BEFORE_EXECUTE);
        verify(getEventCaller(), times(2)).verifyEventExecutionType(ShardingEventType.EXECUTE_SUCCESS);
        verify(getEventCaller(), times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteBatchForMultiplePreparedStatementsSuccess() throws SQLException {
        PreparedStatement preparedStatement1 = mock(PreparedStatement.class);
        PreparedStatement preparedStatement2 = mock(PreparedStatement.class);
        when(preparedStatement1.executeBatch()).thenReturn(new int[] {10, 20});
        when(preparedStatement2.executeBatch()).thenReturn(new int[] {20, 40});
        when(preparedStatement1.getConnection()).thenReturn(mock(Connection.class));
        when(preparedStatement2.getConnection()).thenReturn(mock(Connection.class));
        BatchPreparedStatementExecutor actual = new BatchPreparedStatementExecutor(1, 1, 1, false, CONNECTION);
        assertThat(actual.executeBatch(), is(new int[] {30, 60}));
        verify(preparedStatement1).executeBatch();
        verify(preparedStatement2).executeBatch();
        verify(getEventCaller(), times(4)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(4)).verifyDataSource("ds_1");
        verify(getEventCaller(), times(8)).verifySQL(SQL);
        verify(getEventCaller(), times(4)).verifyParameters(Collections.<Object>singletonList(1));
        verify(getEventCaller(), times(4)).verifyParameters(Collections.<Object>singletonList(2));
        verify(getEventCaller(), times(4)).verifyEventExecutionType(ShardingEventType.BEFORE_EXECUTE);
        verify(getEventCaller(), times(4)).verifyEventExecutionType(ShardingEventType.EXECUTE_SUCCESS);
        verify(getEventCaller(), times(0)).verifyException(null);
    }
    
    @Test
    public void assertExecuteBatchForSinglePreparedStatementFailure() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        SQLException exp = new SQLException();
        when(preparedStatement.executeBatch()).thenThrow(exp);
        when(preparedStatement.getConnection()).thenReturn(mock(Connection.class));
        BatchPreparedStatementExecutor actual = new BatchPreparedStatementExecutor(1, 1, 1, false, CONNECTION);
        assertThat(actual.executeBatch(), is(new int[] {0, 0}));
        verify(preparedStatement).executeBatch();
        verify(getEventCaller(), times(4)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(4)).verifySQL(SQL);
        verify(getEventCaller(), times(2)).verifyParameters(Collections.<Object>singletonList(1));
        verify(getEventCaller(), times(2)).verifyParameters(Collections.<Object>singletonList(2));
        verify(getEventCaller(), times(2)).verifyEventExecutionType(ShardingEventType.BEFORE_EXECUTE);
        verify(getEventCaller(), times(2)).verifyEventExecutionType(ShardingEventType.EXECUTE_FAILURE);
        verify(getEventCaller(), times(2)).verifyException(exp);
    }
    
    @Test
    public void assertExecuteBatchForMultiplePreparedStatementsFailure() throws SQLException {
        PreparedStatement preparedStatement1 = mock(PreparedStatement.class);
        PreparedStatement preparedStatement2 = mock(PreparedStatement.class);
        SQLException exp = new SQLException();
        when(preparedStatement1.executeBatch()).thenThrow(exp);
        when(preparedStatement2.executeBatch()).thenThrow(exp);
        when(preparedStatement1.getConnection()).thenReturn(mock(Connection.class));
        when(preparedStatement2.getConnection()).thenReturn(mock(Connection.class));
        BatchPreparedStatementExecutor actual = new BatchPreparedStatementExecutor(1, 1, 1, false, CONNECTION);
        assertThat(actual.executeBatch(), is(new int[] {0, 0}));
        verify(preparedStatement1).executeBatch();
        verify(preparedStatement2).executeBatch();
        verify(getEventCaller(), times(4)).verifyDataSource("ds_0");
        verify(getEventCaller(), times(4)).verifyDataSource("ds_1");
        verify(getEventCaller(), times(8)).verifySQL(SQL);
        verify(getEventCaller(), times(4)).verifyParameters(Collections.<Object>singletonList(1));
        verify(getEventCaller(), times(4)).verifyParameters(Collections.<Object>singletonList(2));
        verify(getEventCaller(), times(4)).verifyEventExecutionType(ShardingEventType.BEFORE_EXECUTE);
        verify(getEventCaller(), times(4)).verifyEventExecutionType(ShardingEventType.EXECUTE_FAILURE);
        verify(getEventCaller(), times(4)).verifyException(exp);
    }
}
