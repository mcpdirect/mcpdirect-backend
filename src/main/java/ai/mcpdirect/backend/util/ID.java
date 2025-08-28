package ai.mcpdirect.backend.util;

import com.littlenb.snowflake.sequence.SnowFlakeGenerator;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ID {
    private static final long INITIAL_TIME_STAMP = 1483200000000L;
    private static final SnowFlakeGenerator generator = new SnowFlakeGenerator(TimeUnit.MILLISECONDS,
            50,3,10,INITIAL_TIME_STAMP,0);

    public static long nextId(){
        return generator.nextId();
    }
    public static String nextIdString(){
        return Long.toString(generator.nextId(),36);
    }
    public static String toIdString(long id){
        return Long.toString(id,36);
    }
    public static long create(Object one,Object other){
        return (one.hashCode() & 0x7FFFFFFFL) << 32L | (0xFFFFFFFFL&other.hashCode());
    }
}