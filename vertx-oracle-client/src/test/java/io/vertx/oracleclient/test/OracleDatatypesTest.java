/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.oracleclient.test;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.oracleclient.OraclePool;
import io.vertx.oracleclient.test.junit.OracleRule;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;

@RunWith(VertxUnitRunner.class)
public class OracleDatatypesTest extends OracleTestBase {

  @ClassRule
  public static OracleRule oracle = OracleRule.SHARED_INSTANCE;

  OraclePool pool;

  private static Future<RowSet<Row>> insertRawValue(SqlConnection conn) {
    final Tuple params = Tuple.of( 5, Buffer.buffer( "See you space cowboy..." ) );
    return conn
            .preparedQuery( "INSERT INTO basicdatatype (id, rawValue) VALUES (?,?)" ).execute( params )
            .onSuccess( rows -> conn
                    .preparedQuery( "SELECT rawValue FROM basicDataType WHERE rawValue = ?" )
                    .execute( Tuple.of( params.getValue( 1 ) ) ) );
  }

  @Before
  public void setUp() throws Exception {
    pool = OraclePool.pool( vertx, oracle.options(), new PoolOptions() );
  }

  @Test
  public void testRaw(TestContext ctx) {
    pool.withConnection( OracleDatatypesTest::insertRawValue, ctx
            .asyncAssertSuccess( rows -> ctx.assertEquals( 1, rows.size() ) )
    );
  }

  @After
  public void tearDown(TestContext ctx) throws Exception {
    pool.close( ctx.asyncAssertSuccess() );
  }
}
