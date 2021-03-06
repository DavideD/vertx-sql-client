/*
 * Copyright (C) 2017 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.vertx.pgclient.impl.codec;

import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.data.Box;
import io.vertx.pgclient.data.Circle;
import io.vertx.pgclient.data.Line;
import io.vertx.pgclient.data.LineSegment;
import io.vertx.sqlclient.data.Numeric;
import io.vertx.pgclient.data.Interval;
import io.vertx.pgclient.data.Path;
import io.vertx.pgclient.data.Point;
import io.vertx.pgclient.data.Polygon;
import io.vertx.core.buffer.Buffer;

import java.time.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PostgreSQL <a href="https://github.com/postgres/postgres/blob/master/src/include/catalog/pg_type.h">object
 * identifiers (OIDs)</a> for data types
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
enum DataType {

  BOOL(16, true, Boolean.class),
  BOOL_ARRAY(1000, true, Boolean[].class),
  INT2(21, true, Short.class, Number.class),
  INT2_ARRAY(1005, true, Short[].class, Number[].class),
  INT4(23, true, Integer.class, Number.class),
  INT4_ARRAY(1007, true, Integer[].class, Number[].class),
  INT8(20, true, Long.class, Number.class),
  INT8_ARRAY(1016, true, Long[].class, Number[].class),
  FLOAT4(700, true, Float.class, Number.class),
  FLOAT4_ARRAY(1021, true, Float[].class, Number[].class),
  FLOAT8(701, true, Double.class, Number.class),
  FLOAT8_ARRAY(1022, true, Double[].class, Number[].class),
  NUMERIC(1700, false, Numeric.class, Number.class),
  NUMERIC_ARRAY(1231, false, Numeric[].class, Number[].class),
  MONEY(790, true, Object.class),
  MONEY_ARRAY(791, true, Object[].class),
  BIT(1560, true, Object.class),
  BIT_ARRAY(1561, true, Object[].class),
  VARBIT(1562, true, Object.class),
  VARBIT_ARRAY(1563, true, Object[].class),
  CHAR(18, true, String.class),
  CHAR_ARRAY(1002, true, String[].class),
  VARCHAR(1043, true, String.class),
  VARCHAR_ARRAY(1015, true, String[].class),
  BPCHAR(1042, true, String.class),
  BPCHAR_ARRAY(1014, true, String[].class),
  TEXT(25, true, String.class),
  TEXT_ARRAY(1009, true, String[].class),
  NAME(19, true, String.class),
  NAME_ARRAY(1003, true, String[].class),
  DATE(1082, true, LocalDate.class),
  DATE_ARRAY(1182, true, LocalDate[].class),
  TIME(1083, true, LocalTime.class),
  TIME_ARRAY(1183, true, LocalTime[].class),
  TIMETZ(1266, true, OffsetTime.class),
  TIMETZ_ARRAY(1270, true, OffsetTime[].class),
  TIMESTAMP(1114, true, LocalDateTime.class),
  TIMESTAMP_ARRAY(1115, true, LocalDateTime[].class),
  TIMESTAMPTZ(1184, true, OffsetDateTime.class),
  TIMESTAMPTZ_ARRAY(1185, true, OffsetDateTime[].class),
  INTERVAL(1186, true, Interval.class),
  INTERVAL_ARRAY(1187, true, Interval[].class),
  BYTEA(17, true, Buffer.class),
  BYTEA_ARRAY(1001, true, Buffer[].class),
  MACADDR(829, true, Object.class),
  INET(869, true, Object[].class),
  CIDR(650, true, Object.class),
  MACADDR8(774, true, Object[].class),
  UUID(2950, true, UUID.class),
  UUID_ARRAY(2951, true, UUID[].class),
  JSON(114, true, Object.class),
  JSON_ARRAY(199, true, Object[].class),
  JSONB(3802, true, Object.class),
  JSONB_ARRAY(3807, true, Object[].class),
  XML(142, true, Object.class),
  XML_ARRAY(143, true, Object[].class),
  POINT(600, true, Point.class),
  POINT_ARRAY(1017, true, Point[].class),
  LINE(628, true, Line.class),
  LINE_ARRAY(629, true, Line[].class),
  LSEG(601, true, LineSegment.class),
  LSEG_ARRAY(1018, true, LineSegment[].class),
  BOX(603, true, Box.class),
  BOX_ARRAY(1020, true, Box[].class),
  PATH(602, true, Path.class),
  PATH_ARRAY(1019, true, Path[].class),
  POLYGON(604, true, Polygon.class),
  POLYGON_ARRAY(1027, true, Polygon[].class),
  CIRCLE(718, true, Circle.class),
  CIRCLE_ARRAY(719, true, Circle[].class),
  HSTORE(33670, true, Object.class),
  OID(26, true, Object.class),
  OID_ARRAY(1028, true, Object[].class),
  VOID(2278, true, Object.class),
  UNKNOWN(705, false, String.class),
  TS_VECTOR(3614, false, String.class),
  TS_VECTOR_ARRAY(3643, false, String[].class),
  TS_QUERY(3615, false,  String.class),
  TS_QUERY_ARRAY(3645, false,  String[].class);

  private static final Logger logger = LoggerFactory.getLogger(DataType.class);
  private static final IntObjectMap<DataType> oidToDataType = new IntObjectHashMap<>();
  private static final Map<Class<?>, DataType> encodingTypeToDataType = new HashMap<>();

  final int id;
  final boolean supportsBinary;
  final Class<?> encodingType; // Not really used for now
  final Class<?> decodingType;

  DataType(int id, boolean supportsBinary, Class<?> type) {
    this(id, supportsBinary, type, type);
  }

  DataType(int id, boolean supportsBinary, Class<?> encodingType, Class<?> decodingType) {
    this.id = id;
    this.supportsBinary = supportsBinary;
    this.encodingType = encodingType;
    this.decodingType = decodingType;
  }

  static DataType valueOf(int oid) {
    DataType value = oidToDataType.get(oid);
    if (value == null) {
      logger.debug("Postgres type OID=" + oid + " not handled - using unknown type instead");
      return UNKNOWN;
    } else {
      return value;
    }
  }

  static DataType lookup(Class<?> type) {
    DataType dataType = encodingTypeToDataType.get(type);
    if (dataType == null) {
      if (Buffer.class.isAssignableFrom(type)) {
        return BYTEA;
      }
      dataType = DataType.UNKNOWN;
    }
    return dataType;
  }

  static {
    for (DataType dataType : values()) {
      oidToDataType.put(dataType.id, dataType);
    }
    encodingTypeToDataType.put(String.class, VARCHAR);
    encodingTypeToDataType.put(String[].class, VARCHAR_ARRAY);
    encodingTypeToDataType.put(Boolean.class, BOOL);
    encodingTypeToDataType.put(Boolean[].class, BOOL_ARRAY);
    encodingTypeToDataType.put(Short.class, INT2);
    encodingTypeToDataType.put(Short[].class, INT2_ARRAY);
    encodingTypeToDataType.put(Integer.class, INT4);
    encodingTypeToDataType.put(Integer[].class, INT4_ARRAY);
    encodingTypeToDataType.put(Long.class, INT8);
    encodingTypeToDataType.put(Long[].class, INT8_ARRAY);
    encodingTypeToDataType.put(Float.class, FLOAT4);
    encodingTypeToDataType.put(Float[].class, FLOAT4_ARRAY);
    encodingTypeToDataType.put(Double.class, FLOAT8);
    encodingTypeToDataType.put(Double[].class, FLOAT8_ARRAY);
    encodingTypeToDataType.put(LocalDate.class, DATE);
    encodingTypeToDataType.put(LocalDate[].class, DATE_ARRAY);
    encodingTypeToDataType.put(LocalDateTime.class, TIMESTAMP);
    encodingTypeToDataType.put(LocalDateTime[].class, TIMESTAMP_ARRAY);
    encodingTypeToDataType.put(OffsetDateTime.class, TIMESTAMPTZ);
    encodingTypeToDataType.put(OffsetDateTime[].class, TIMESTAMPTZ_ARRAY);
    encodingTypeToDataType.put(Interval.class, INTERVAL);
    encodingTypeToDataType.put(Interval[].class, INTERVAL_ARRAY);
    encodingTypeToDataType.put(Buffer[].class, BYTEA_ARRAY);
    encodingTypeToDataType.put(UUID.class, UUID);
    encodingTypeToDataType.put(UUID[].class, UUID_ARRAY);
    encodingTypeToDataType.put(JsonObject.class, JSON);
    encodingTypeToDataType.put(JsonObject[].class, JSON_ARRAY);
    encodingTypeToDataType.put(JsonArray.class, JSON);
    encodingTypeToDataType.put(JsonArray[].class, JSON_ARRAY);
    encodingTypeToDataType.put(Point.class, POINT);
    encodingTypeToDataType.put(Point[].class, POINT_ARRAY);
    encodingTypeToDataType.put(Line.class, LINE);
    encodingTypeToDataType.put(Line[].class, LINE_ARRAY);
    encodingTypeToDataType.put(LineSegment.class, LSEG);
    encodingTypeToDataType.put(LineSegment[].class, LSEG_ARRAY);
    encodingTypeToDataType.put(Box.class, BOX);
    encodingTypeToDataType.put(Box[].class, BOX_ARRAY);
    encodingTypeToDataType.put(Path.class, PATH);
    encodingTypeToDataType.put(Path[].class, PATH_ARRAY);
    encodingTypeToDataType.put(Polygon.class, POLYGON);
    encodingTypeToDataType.put(Polygon[].class, POLYGON_ARRAY);
    encodingTypeToDataType.put(Circle.class, CIRCLE);
    encodingTypeToDataType.put(Circle[].class, CIRCLE_ARRAY);
  }
}
