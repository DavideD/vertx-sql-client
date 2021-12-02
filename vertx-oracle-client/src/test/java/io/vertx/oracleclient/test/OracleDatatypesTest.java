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

import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Future;
import io.vertx.core.Handler;
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

  @Before
  public void setUp() {
    pool = OraclePool.pool( vertx, oracle.options(), new PoolOptions() );
  }

  @Test
  public void testRaw(TestContext ctx) {
    final Buffer rawValue = Buffer.buffer( "See you space cowboy..." );
    final Tuple params = Tuple.of( 11, rawValue );
    testField( ctx, params, "rawValue",
               row -> ctx.assertEquals( row.getBuffer( "rawValue" ), rawValue ) );
  }

  @Test
  public void testLocalTime(TestContext ctx) {
    final LocalTime localTimeValue = LocalTime.MAX.truncatedTo( ChronoUnit.SECONDS );
    final Tuple params = Tuple.of( 22, localTimeValue );
    testField( ctx, params, "localTime",
               row -> ctx.assertEquals( row.getLocalTime( "localTime" ), localTimeValue ) );
  }

  @Test
  public void testOffsetTime(TestContext ctx) {
    final OffsetTime offsetTimeValue = OffsetTime.now( ZoneOffset.ofHours( 7 ) )
            .truncatedTo( ChronoUnit.SECONDS );

    final Tuple params = Tuple.of( 33, offsetTimeValue );
    testField( ctx, params, "offsetTime",
               row -> ctx.assertEquals( row.getOffsetTime( "offsetTime" ), offsetTimeValue ) );
  }

  private void testField(TestContext ctx, Tuple params, String column, Consumer<Row> assertion) {
    pool.withConnection( conn -> conn
            .preparedQuery( "INSERT INTO basicdatatype (id, " + column + ") VALUES (?,?)" )
            .execute( params )
            .compose( rows -> conn
                .preparedQuery( "SELECT " + column + " FROM basicDataType WHERE " + column + " = ?" )
                .execute( Tuple.of( params.getValue( 1 ) ) ) ),
                         ctx.asyncAssertSuccess( rows -> assertion.accept( rows.iterator().next() ) )
    );
  }

  @After
  public void tearDown(TestContext ctx) {
    pool.close( ctx.asyncAssertSuccess() );
  }
}
