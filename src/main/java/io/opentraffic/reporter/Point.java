package io.opentraffic.reporter;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.log4j.Logger;

public class Point {
  float lat, lon;
  int accuracy;
  long time;
  public static final int SIZE = 4 + 4 + 4 + 8; //keep this up to date
  private final static Logger logger = Logger.getLogger(Point.class);
  
  public Point(float lat, float lon, int accuracy, long time) {
    this.lat = lat;
    this.lon = lon;
    this.accuracy = accuracy;
    this.time = time;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Point other = (Point) obj;
    if (accuracy != other.accuracy)
      return false;
    if (Float.floatToIntBits(lat) != Float.floatToIntBits(other.lat))
      return false;
    if (Float.floatToIntBits(lon) != Float.floatToIntBits(other.lon))
      return false;
    if (time != other.time)
      return false;
    return true;
  }

  public static class Serder implements Serde<Point> {
    public static final DecimalFormat floatFormatter = new DecimalFormat("###.######", new DecimalFormatSymbols(Locale.US));
    public static void put(Point p, ByteBuffer buffer) {
      buffer.putFloat(p.lat);
      buffer.putFloat(p.lon);
      buffer.putInt(p.accuracy);
      buffer.putLong(p.time);
    }
    public static Point get(ByteBuffer buffer) {
      return new Point(buffer.getFloat(), buffer.getFloat(), buffer.getInt(), buffer.getLong());
    }
    public static void put_json(Point p, StringBuilder sb) {
      sb.append("{\"lat\":");
      sb.append(floatFormatter.format(p.lat)).append(",\"lon\":");
      sb.append(floatFormatter.format(p.lon)).append(",\"time\":");
      sb.append(Long.toString(p.time)).append(",\"accuracy\":");
      sb.append(Integer.toString(p.accuracy)).append("}");
    }
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) { }
    @Override
    public void close() { }
    @Override
    public Serializer<Point> serializer() {
      return new Serializer<Point>() {
        @Override
        public void configure(Map<String, ?> configs, boolean isKey) { }
        @Override
        public byte[] serialize(String topic, Point p) {
          ByteBuffer buffer = ByteBuffer.allocate(SIZE);
          Serder.put(p, buffer);
          return buffer.array();
        }
        @Override
        public void close() { }        
      };
    }
    @Override
    public Deserializer<Point> deserializer() {
      return new Deserializer<Point>() {
        @Override
        public void configure(Map<String, ?> configs, boolean isKey) { }
        @Override
        public Point deserialize(String topic, byte[] bytes) {
          ByteBuffer buffer = ByteBuffer.wrap(bytes);
          return Serder.get(buffer);
        }
        @Override
        public void close() { }
      };
    }    
  }
}
